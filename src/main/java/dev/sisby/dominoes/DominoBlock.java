package dev.sisby.dominoes;

import dev.sisby.dominoes.mixin.AbstractPressurePlateBlockAccessor;
import dev.sisby.dominoes.mixin.FallingBlockEntityAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static dev.sisby.dominoes.DominoShapes.*;

public class DominoBlock extends Block implements Fallable {
	public static final EnumProperty<Collapsed> COLLAPSED = EnumProperty.create("collapsed", Collapsed.class);
	public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
	public static final BooleanProperty COLLAPSING = BooleanProperty.create("collapsing");

	public static final double CORNER_TOLERANCE = 0.1875; // 3px
	public static final double EDGE_TOLERANCE = 0.0625; // 1px

	public DominoBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(COLLAPSED, Collapsed.NONE).setValue(SHAPE, Shape.NORTH_SOUTH).setValue(COLLAPSING, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(COLLAPSED, SHAPE, COLLAPSING);
	}

	protected static Shape getPlacementShape(BlockPlaceContext ctx, boolean canStack) {
		if (ctx.getClickedFace() == Direction.UP) {
			Vec3 offset = ctx.getClickLocation().subtract(Vec3.atLowerCornerOf(ctx.getClickedPos()));
			if (offset.x() < CORNER_TOLERANCE && offset.z() < CORNER_TOLERANCE) return Shape.WEST_NORTH;
			if (offset.x() > 1 - CORNER_TOLERANCE && offset.z() < CORNER_TOLERANCE) return Shape.NORTH_EAST;
			if (offset.x() > 1 - CORNER_TOLERANCE && offset.z() > 1 - CORNER_TOLERANCE) return Shape.EAST_SOUTH;
			if (offset.x() < CORNER_TOLERANCE && offset.z() > 1 - CORNER_TOLERANCE) return Shape.SOUTH_WEST;
			if (canStack) {
				if (offset.x() < EDGE_TOLERANCE || offset.x() > 1 - EDGE_TOLERANCE) return Shape.EAST_WEST_STACK;
				if (offset.z() < EDGE_TOLERANCE || offset.z() > 1 - EDGE_TOLERANCE) return Shape.NORTH_SOUTH_STACK;
			}
		}
		return switch (ctx.getHorizontalDirection()) {
			case DOWN, SOUTH, NORTH, UP -> Shape.NORTH_SOUTH;
			case WEST, EAST -> Shape.EAST_WEST;
		};
	}

	@Override
	public void onLand(Level world, BlockPos pos, BlockState fallingBlockState, BlockState currentStateInPos, FallingBlockEntity fallingBlockEntity) {
		collapse(fallingBlockState, world, pos, null, true, true);
	}

	@Override
	protected boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
		if (super.canBeReplaced(state, ctx)) return true;
		if (!(!ctx.isSecondaryUseActive() && ctx.getItemInHand().getItem() == this.asItem())) return false;
		Shape newShape = getPlacementShape(ctx, true);
		return (Shape.CORNERS.contains(state.getValue(SHAPE)) && Shape.CORNERS.get((Shape.CORNERS.indexOf(state.getValue(SHAPE)) - 1 + Shape.CORNERS.size()) % Shape.CORNERS.size()) == newShape)
				|| (Shape.STRAIGHTS.contains(state.getValue(SHAPE)) && Shape.STACKS.get(Shape.STRAIGHTS.indexOf(state.getValue(SHAPE))) == newShape);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockPos blockPos = pos.below();
		return world.getBlockState(blockPos).isFaceSturdy(world, blockPos, Direction.UP);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
		if (state.is(this)) {
			Shape newShape = getPlacementShape(ctx, true);
			if (Shape.CORNERS.contains(newShape)) newShape = Shape.DOUBLES.get(Shape.CORNERS.indexOf(state.getValue(SHAPE)));
			return state.setValue(SHAPE, newShape);
		}
		return this.defaultBlockState().setValue(SHAPE, getPlacementShape(ctx, false));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(COLLAPSED)) {
			case NONE -> state.getValue(SHAPE).getShapeStanding();
			case FORWARDS -> state.getValue(SHAPE).getShapeForwards();
			case BACKWARDS -> state.getValue(SHAPE).getShapeBackwards();
		};
	}

	@Override
	protected void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile entity) {
		if (state.getValue(COLLAPSED) == Collapsed.NONE && entity.getType().is(Dominoes.COLLAPSING)) {
			collapseFromHit(state, world, hit.getBlockPos(), null, hit.getDirection());
		}
	}

	@Override
	protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean bl) {
		if (!world.isClientSide() && state.getValue(COLLAPSED) == Collapsed.NONE && entity.getType().is(Dominoes.COLLAPSING) && !(entity instanceof Projectile)) {
			Vec3 vel = entity.getDeltaMovement();
			// minimum velocity to fall over
			if (vel.horizontalDistance() > 0.05) {
				// check if the entity is against collision. based on BlockCollisionSpliterator
				VoxelShape bounds = Shapes.create(entity.getBoundingBox().inflate(0.1));
				VoxelShape collisionShape = state.getCollisionShape(world, pos, CollisionContext.of(entity)).move(pos);
				if (Shapes.joinIsNotEmpty(bounds, collisionShape, BooleanOp.AND)) {
					// entity is right up against the block. use the velocity to determine the direction to fall.
					// discard the Y component because we only want horizontal directions
					Direction motion = Direction.getApproximateNearest(vel.x, 0, vel.z);
					if (motion.getAxis().isHorizontal()) {
						Direction hitSide = motion.getOpposite();
						collapseFromHit(state, world, pos, null, hitSide);
					}
				}
			}
		}
	}

	protected boolean collapseFromHit(BlockState state, Level world, BlockPos pos, Player player, Direction hitSide) {
		if (state.getValue(SHAPE).connections().contains(hitSide)) {
			collapse(state, world, pos, player, state.getValue(SHAPE).connections().getFirst() == hitSide, true);
			return true;
		} else if (state.getValue(SHAPE).connections().contains(hitSide.getOpposite()) && state.getValue(SHAPE).connections().size() == 2) { // handle corners nicer
			collapse(state, world, pos, player, state.getValue(SHAPE).connections().getFirst() != hitSide.getOpposite(), true);
			return true;
		}
		return false;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (state.getValue(COLLAPSED) != Collapsed.NONE) {
			world.setBlockAndUpdate(pos, state.setValue(COLLAPSED, Collapsed.NONE).setValue(COLLAPSING, false));
			world.playSound(player, pos, SoundEvents.STONE_STEP, SoundSource.BLOCKS);
			return InteractionResult.SUCCESS;
		} else if (collapseFromHit(state, world, pos, player, hit.getDirection())) {
			return InteractionResult.SUCCESS;
		}
		return super.useWithoutItem(state, world, pos, player, hit);
	}

	protected void collapse(BlockState state, Level world, BlockPos pos, Player player, boolean forwards, boolean initial) {
		if (state.getValue(COLLAPSED) != Collapsed.NONE) return;
		Shape shape = state.getValue(SHAPE);
		if (Shape.STACKS.contains(shape)) {
			BlockPos ahead = pos.relative(shape.connections().get(forwards ? 1 : 0));

			// spawn a falling domino ahead
			FallingBlockEntity fallingBlockEntity = FallingBlockEntityAccessor.invokeConstructor(
				world, ahead.getX() + 0.5, ahead.getY(), ahead.getZ() + 0.5, state.setValue(SHAPE, Shape.STRAIGHTS.get(Shape.STACKS.indexOf(shape))).setValue(COLLAPSING, true).setValue(COLLAPSED, forwards ? Collapsed.FORWARDS : Collapsed.BACKWARDS)
			);
			world.addFreshEntity(fallingBlockEntity);
		}
		world.playSound(player, pos, initial ? SoundEvents.STONE_PLACE : SoundEvents.STONE_FALL, SoundSource.BLOCKS);
		world.setBlock(pos, state.setValue(COLLAPSING, true).setValue(COLLAPSED, forwards ? Collapsed.FORWARDS : Collapsed.BACKWARDS).setValue(SHAPE, Shape.STACKS.contains(shape) ? Shape.STRAIGHTS.get(Shape.STACKS.indexOf(shape)) : shape), Block.UPDATE_CLIENTS, 0);
		world.scheduleTick(pos, state.getBlock(), initial ? 5 : 2);
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (state.getValue(COLLAPSING)) {
			for (Direction dir : state.getValue(SHAPE).connections()) {
				for (int i = -1; i <= 1; i++) {
					BlockPos neighbourPos = pos.relative(dir).relative(Direction.Axis.Y, i);
					if (world.getBlockState(neighbourPos).getBlock() instanceof PressurePlateBlock ppb && ppb instanceof AbstractPressurePlateBlockAccessor appba) {
						BlockState newState = world.getBlockState(neighbourPos).setValue(PressurePlateBlock.POWERED, true);
						world.setBlock(neighbourPos, newState, UPDATE_CLIENTS);
						appba.invokeUpdateNeighbours(world, neighbourPos);
						world.setBlocksDirty(neighbourPos, state, newState);
						world.playSound(null, pos, appba.getType().pressurePlateClickOn(), SoundSource.BLOCKS);
						world.scheduleTick(neighbourPos, ppb, 10);
					} else {
						world.neighborChanged(neighbourPos, state.getBlock(),  null);
					}
				}
			}
			world.setBlockAndUpdate(pos, state.setValue(COLLAPSING, false));
		}
	}

	protected void checkFall(BlockState state, ServerLevel world, BlockPos pos) {
		if (FallingBlock.isFree(world.getBlockState(pos.below())) && pos.getY() >= world.getMinY()) {
			FallingBlockEntity.fall(world, pos, state);
		}
	}

	@Override
	protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
		if (world instanceof ServerLevel sw) checkFall(state, sw, pos);
	}

	@Override
	protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify) {
		for (Direction dir : state.getValue(SHAPE).connections()) {
			boolean forwards = dir == state.getValue(SHAPE).connections().getFirst(); // whether this "hit" comes from the leading side
			for (int i = -1; i <= 1; i++) {
				BlockPos neighbourPos = pos.relative(dir).relative(Direction.Axis.Y, i);
				BlockState neighbour = world.getBlockState(neighbourPos);
				// if a connected collapse is happening
				if (neighbour.getBlock() instanceof DominoBlock && neighbour.getValue(COLLAPSING) && neighbour.getValue(SHAPE).connections().contains(dir.getOpposite())) {
					// if the collapse is in the direction that affects us
					if (neighbour.getValue(COLLAPSED) == (neighbour.getValue(SHAPE).connections().getFirst() != dir.getOpposite() ? Collapsed.FORWARDS : Collapsed.BACKWARDS)) {
						collapse(state, world, pos, null, forwards, false);
					}
				}
				// if a piston is being pushed
				if (i == 0 && world.getBlockEntity(neighbourPos) instanceof PistonMovingBlockEntity pbe && pbe.isExtending() && pbe.getDirection() == dir.getOpposite()) {
					collapse(state, world, pos, null, forwards, false);
				}
			}
		}
		if (world instanceof ServerLevel sw) checkFall(state, sw, pos);
	}

	public enum Collapsed implements StringRepresentable {
		NONE("none"),
		FORWARDS("forwards"),
		BACKWARDS("backwards");

		private final String name;

		Collapsed(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public enum Shape implements StringRepresentable {
		// falling "forwards" means falling from first direction towards second direction
		// straights
		NORTH_SOUTH("north_south", List.of(Direction.NORTH, Direction.SOUTH), VOXEL_NORTH_SOUTH_STANDING, VOXEL_NORTH_SOUTH_FORWARDS, VOXEL_NORTH_SOUTH_BACKWARDS),
		EAST_WEST("east_west", List.of(Direction.EAST, Direction.WEST), VOXEL_EAST_WEST_STANDING, VOXEL_EAST_WEST_FORWARDS, VOXEL_EAST_WEST_BACKWARDS),
		// straight stacks
		NORTH_SOUTH_STACK("north_south_stack", List.of(Direction.NORTH, Direction.SOUTH), VOXEL_NORTH_SOUTH_STACK_STANDING, VOXEL_NORTH_SOUTH_STACK_FORWARDS, VOXEL_NORTH_SOUTH_STACK_BACKWARDS),
		EAST_WEST_STACK("east_west_stack", List.of(Direction.EAST, Direction.WEST), VOXEL_EAST_WEST_STACK_STANDING, VOXEL_EAST_WEST_STACK_FORWARDS, VOXEL_EAST_WEST_STACK_BACKWARDS),
		// corners
		NORTH_EAST("north_east", List.of(Direction.NORTH, Direction.EAST), VOXEL_NORTH_EAST_STANDING, VOXEL_NORTH_EAST_FORWARDS, VOXEL_NORTH_EAST_BACKWARDS),
		EAST_SOUTH("east_south", List.of(Direction.EAST, Direction.SOUTH), VOXEL_EAST_SOUTH_STANDING, VOXEL_EAST_SOUTH_FORWARDS, VOXEL_EAST_SOUTH_BACKWARDS),
		SOUTH_WEST("south_west", List.of(Direction.SOUTH, Direction.WEST), VOXEL_SOUTH_WEST_STANDING, VOXEL_SOUTH_WEST_FORWARDS, VOXEL_SOUTH_WEST_BACKWARDS),
		WEST_NORTH("west_north", List.of(Direction.WEST, Direction.NORTH), VOXEL_WEST_NORTH_STANDING, VOXEL_WEST_NORTH_FORWARDS, VOXEL_WEST_NORTH_BACKWARDS),
		// doubles
		NORTH_EAST_WEST("north_east_west", List.of(Direction.NORTH, Direction.EAST, Direction.WEST), VOXEL_NORTH_EAST_WEST_STANDING, VOXEL_NORTH_EAST_WEST_FORWARDS, VOXEL_NORTH_EAST_WEST_BACKWARDS),
		EAST_SOUTH_NORTH("east_south_north", List.of(Direction.EAST, Direction.SOUTH, Direction.NORTH), VOXEL_EAST_SOUTH_NORTH_STANDING, VOXEL_EAST_SOUTH_NORTH_FORWARDS, VOXEL_EAST_SOUTH_NORTH_BACKWARDS),
		SOUTH_WEST_EAST("south_west_east", List.of(Direction.SOUTH, Direction.WEST, Direction.EAST), VOXEL_SOUTH_WEST_EAST_STANDING, VOXEL_SOUTH_WEST_EAST_FORWARDS, VOXEL_SOUTH_WEST_EAST_BACKWARDS),
		WEST_NORTH_SOUTH("west_north_south", List.of(Direction.WEST, Direction.NORTH, Direction.SOUTH), VOXEL_WEST_NORTH_SOUTH_STANDING, VOXEL_WEST_NORTH_SOUTH_FORWARDS, VOXEL_WEST_NORTH_SOUTH_BACKWARDS);

		public static final List<Shape> STRAIGHTS = List.of(NORTH_SOUTH, EAST_WEST);
		public static final List<Shape> STACKS = List.of(NORTH_SOUTH_STACK, EAST_WEST_STACK);
		public static final List<Shape> CORNERS = List.of(NORTH_EAST, EAST_SOUTH, SOUTH_WEST, WEST_NORTH);
		public static final List<Shape> DOUBLES = List.of(NORTH_EAST_WEST, EAST_SOUTH_NORTH, SOUTH_WEST_EAST, WEST_NORTH_SOUTH);

		private final String name;
		private final List<Direction> connections;
		private final VoxelShape shapeStanding;
		private final VoxelShape shapeForwards;
		private final VoxelShape shapeBackwards;

		Shape(String name, List<Direction> connections, VoxelShape shapeStanding, VoxelShape shapeForwards, VoxelShape shapeBackwards) {
			this.name = name;
			this.connections = connections;
			this.shapeStanding = shapeStanding;
			this.shapeForwards = shapeForwards;
			this.shapeBackwards = shapeBackwards;
		}

		@Override
		public String getSerializedName() {
			return name;
		}

		public List<Direction> connections() {
			return connections;
		}

		public VoxelShape getShapeStanding() {
			return shapeStanding;
		}

		public VoxelShape getShapeForwards() {
			return shapeForwards;
		}

		public VoxelShape getShapeBackwards() {
			return shapeBackwards;
		}
	}
}
