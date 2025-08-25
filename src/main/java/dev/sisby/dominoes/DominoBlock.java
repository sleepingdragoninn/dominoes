package dev.sisby.dominoes;

import dev.sisby.dominoes.mixin.AbstractPressurePlateBlockAccessor;
import dev.sisby.dominoes.mixin.FallingBlockEntityAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Falling;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.sisby.dominoes.DominoShapes.*;

public class DominoBlock extends Block implements Falling {
	public static final EnumProperty<Collapsed> COLLAPSED = EnumProperty.of("collapsed", Collapsed.class);
	public static final EnumProperty<Shape> SHAPE = EnumProperty.of("shape", Shape.class);
	public static final BooleanProperty COLLAPSING = BooleanProperty.of("collapsing");

	public static final double CORNER_TOLERANCE = 0.1875; // 3px
	public static final double EDGE_TOLERANCE = 0.0625; // 1px

	public DominoBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(COLLAPSED, Collapsed.NONE).with(SHAPE, Shape.NORTH_SOUTH).with(COLLAPSING, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(COLLAPSED, SHAPE, COLLAPSING);
	}

	private static Shape getPlacementShape(ItemPlacementContext ctx, boolean canStack) {
		if (ctx.getSide() == Direction.UP) {
			Vec3d offset = ctx.getHitPos().subtract(Vec3d.of(ctx.getBlockPos()));
			if (offset.getX() < CORNER_TOLERANCE && offset.getZ() < CORNER_TOLERANCE) return Shape.WEST_NORTH;
			if (offset.getX() > 1 - CORNER_TOLERANCE && offset.getZ() < CORNER_TOLERANCE) return Shape.NORTH_EAST;
			if (offset.getX() > 1 - CORNER_TOLERANCE && offset.getZ() > 1 - CORNER_TOLERANCE) return Shape.EAST_SOUTH;
			if (offset.getX() < CORNER_TOLERANCE && offset.getZ() > 1 - CORNER_TOLERANCE) return Shape.SOUTH_WEST;
			if (canStack) {
				if (offset.getX() < EDGE_TOLERANCE || offset.getX() > 1 - EDGE_TOLERANCE) return Shape.EAST_WEST_STACK;
				if (offset.getZ() < EDGE_TOLERANCE || offset.getZ() > 1 - EDGE_TOLERANCE) return Shape.NORTH_SOUTH_STACK;
			}
		}
		return switch (ctx.getHorizontalPlayerFacing()) {
			case DOWN, SOUTH, NORTH, UP -> Shape.NORTH_SOUTH;
			case WEST, EAST -> Shape.EAST_WEST;
		};
	}

	@Override
	public void onLanding(World world, BlockPos pos, BlockState fallingBlockState, BlockState currentStateInPos, FallingBlockEntity fallingBlockEntity) {
		collapse(fallingBlockState, world, pos, null, true, true);
	}

	@Override
	protected boolean canReplace(BlockState state, ItemPlacementContext ctx) {
		if (super.canReplace(state, ctx)) return true;
		if (!(!ctx.shouldCancelInteraction() && ctx.getStack().getItem() == this.asItem())) return false;
		Shape newShape = getPlacementShape(ctx, true);
		return (Shape.CORNERS.contains(state.get(SHAPE)) && Shape.CORNERS.get((Shape.CORNERS.indexOf(state.get(SHAPE)) - 1 + Shape.CORNERS.size()) % Shape.CORNERS.size()) == newShape)
				|| (Shape.STRAIGHTS.contains(state.get(SHAPE)) && Shape.STACKS.get(Shape.STRAIGHTS.indexOf(state.get(SHAPE))) == newShape);
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos blockPos = pos.down();
		return world.getBlockState(blockPos).isSideSolidFullSquare(world, blockPos, Direction.UP);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState state = ctx.getWorld().getBlockState(ctx.getBlockPos());
		if (state.isOf(this)) {
			Shape newShape = getPlacementShape(ctx, true);
			if (Shape.CORNERS.contains(newShape)) newShape = Shape.DOUBLES.get(Shape.CORNERS.indexOf(state.get(SHAPE)));
			return state.with(SHAPE, newShape);
		}
		return this.getDefaultState().with(SHAPE, getPlacementShape(ctx, false));
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return switch (state.get(COLLAPSED)) {
			case NONE -> state.get(SHAPE).getShapeStanding();
			case FORWARDS -> state.get(SHAPE).getShapeForwards();
			case BACKWARDS -> state.get(SHAPE).getShapeBackwards();
		};
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler) {
		if (!world.isClient() && state.get(COLLAPSED) == Collapsed.NONE && entity.getType().isIn(Dominoes.COLLAPSING) && !(entity instanceof ProjectileEntity) && entity.getVelocity().horizontalLengthSquared() > 0.06) {
			if (entity.getBoundingBox().expand(0.2).contains(Vec3d.of(pos).add(getCollisionShape(state, world, pos, ShapeContext.of(entity)).getBoundingBox().getCenter()))) {
				collapseFromHit(state, world, pos, null, entity.getMovementDirection().getOpposite());
			}
		}
	}

	@Override
	protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity entity) {
		if (state.get(COLLAPSED) == Collapsed.NONE && entity.getType().isIn(Dominoes.COLLAPSING)) {
			collapseFromHit(state, world, hit.getBlockPos(), null, hit.getSide());
		}
	}

	private boolean collapseFromHit(BlockState state, World world, BlockPos pos, PlayerEntity player, Direction hitSide) {
		if (state.get(SHAPE).connections().contains(hitSide)) {
			collapse(state, world, pos, player, state.get(SHAPE).connections().getFirst() == hitSide, true);
			return true;
		} else if (state.get(SHAPE).connections().contains(hitSide.getOpposite()) && state.get(SHAPE).connections().size() == 2) { // handle corners nicer
			collapse(state, world, pos, player, state.get(SHAPE).connections().getFirst() != hitSide.getOpposite(), true);
			return true;
		}
		return false;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (state.get(COLLAPSED) != Collapsed.NONE) {
			world.setBlockState(pos, state.with(COLLAPSED, Collapsed.NONE).with(COLLAPSING, false));
			world.playSound(player, pos, SoundEvents.BLOCK_STONE_STEP, SoundCategory.BLOCKS);
			return ActionResult.SUCCESS;
		} else if (collapseFromHit(state, world, pos, player, hit.getSide())) {
			return ActionResult.SUCCESS;
		}
		return super.onUse(state, world, pos, player, hit);
	}

	private void collapse(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean forwards, boolean initial) {
		Shape shape = state.get(SHAPE);
		if (Shape.STACKS.contains(shape)) {
			BlockPos ahead = pos.offset(shape.connections().get(forwards ? 1 : 0));

			// spawn a falling domino ahead
			FallingBlockEntity fallingBlockEntity = FallingBlockEntityAccessor.invokeConstructor(
				world, ahead.getX() + 0.5, ahead.getY(), ahead.getZ() + 0.5, state.with(SHAPE, Shape.STRAIGHTS.get(Shape.STACKS.indexOf(shape))).with(COLLAPSING, true).with(COLLAPSED, forwards ? Collapsed.FORWARDS : Collapsed.BACKWARDS)
			);
			world.spawnEntity(fallingBlockEntity);
		}
		world.playSound(player, pos, initial ? SoundEvents.BLOCK_STONE_PLACE : SoundEvents.BLOCK_STONE_FALL, SoundCategory.BLOCKS);
		world.setBlockState(pos, state.with(COLLAPSING, true).with(COLLAPSED, forwards ? Collapsed.FORWARDS : Collapsed.BACKWARDS).with(SHAPE, Shape.STACKS.contains(shape) ? Shape.STRAIGHTS.get(Shape.STACKS.indexOf(shape)) : shape), Block.NOTIFY_LISTENERS, 0);
		world.scheduleBlockTick(pos, state.getBlock(), initial ? 5 : 2);
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (state.get(COLLAPSING)) {
			for (Direction dir : state.get(SHAPE).connections()) {
				for (int i = -1; i <= 1; i++) {
					BlockPos neighbourPos = pos.offset(dir).offset(Direction.Axis.Y, i);
					if (world.getBlockState(neighbourPos).getBlock() instanceof PressurePlateBlock ppb && ppb instanceof AbstractPressurePlateBlockAccessor appba) {
						BlockState newState = world.getBlockState(neighbourPos).with(PressurePlateBlock.POWERED, true);
						world.setBlockState(neighbourPos, newState, NOTIFY_LISTENERS);
						appba.invokeUpdateNeighbors(world, neighbourPos);
						world.scheduleBlockRerenderIfNeeded(neighbourPos, state, newState);
						world.playSound(null, pos, appba.getBlockSetType().pressurePlateClickOn(), SoundCategory.BLOCKS);
						world.scheduleBlockTick(neighbourPos, ppb, 10);
					} else {
						world.updateNeighbor(neighbourPos, state.getBlock(),  null);
					}
				}
			}
			world.setBlockState(pos, state.with(COLLAPSING, false));
		}
	}

	private void checkFall(BlockState state, ServerWorld world, BlockPos pos) {
		if (FallingBlock.canFallThrough(world.getBlockState(pos.down())) && pos.getY() >= world.getBottomY()) {
			FallingBlockEntity.spawnFromBlock(world, pos, state);
		}
	}

	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (world instanceof ServerWorld sw) checkFall(state, sw, pos);
	}

	@Override
	protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
		if (state.get(COLLAPSED) == Collapsed.NONE) {
			for (Direction dir : state.get(SHAPE).connections()) {
				boolean forwards = dir == state.get(SHAPE).connections().getFirst(); // whether this "hit" comes from the leading side
				for (int i = -1; i <= 1; i++) {
					BlockPos neighbourPos = pos.offset(dir).offset(Direction.Axis.Y, i);
					BlockState neighbour = world.getBlockState(neighbourPos);
					// if a connected collapse is happening
					if (neighbour.isOf(state.getBlock()) && neighbour.get(COLLAPSING) && neighbour.get(SHAPE).connections().contains(dir.getOpposite())) {
						// if the collapse is in the direction that affects us
						if (neighbour.get(COLLAPSED) == (neighbour.get(SHAPE).connections().getFirst() != dir.getOpposite() ? Collapsed.FORWARDS : Collapsed.BACKWARDS)) {
							collapse(state, world, pos, null, forwards, false);
						}
					}
					// if a piston is being pushed
					if (i == 0 && world.getBlockEntity(neighbourPos) instanceof PistonBlockEntity pbe && pbe.isExtending() && pbe.getFacing() == dir.getOpposite()) {
						collapse(state, world, pos, null, forwards, false);
					}
				}
			}
		}
		if (world instanceof ServerWorld sw) checkFall(state, sw, pos);
	}

	public enum Collapsed implements StringIdentifiable {
		NONE("none"),
		FORWARDS("forwards"),
		BACKWARDS("backwards");

		private final String name;

		Collapsed(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return name;
		}
	}

	public enum Shape implements StringIdentifiable {
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
		public String asString() {
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
