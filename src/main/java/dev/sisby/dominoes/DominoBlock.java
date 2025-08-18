package dev.sisby.dominoes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.math.DirectionTransformation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DominoBlock extends Block {
	protected static final VoxelShape VOXEL_NS = VoxelShapes.cuboid(0.25, 0, 0.4375, 0.75, 0.875, 0.5625);
	protected static final VoxelShape VOXEL_NS_FORWARDS = VoxelShapes.cuboid(0.25, 0, 0.125, 0.75, 0.125, 1);
	protected static final VoxelShape VOXEL_NS_BACKWARDS = VoxelShapes.transform(VOXEL_NS_FORWARDS, DirectionTransformation.ROT_180_FACE_XZ);
	protected static final VoxelShape VOXEL_EW = VoxelShapes.transform(VOXEL_NS, DirectionTransformation.SWAP_XZ);
	protected static final VoxelShape VOXEL_EW_BACKWARDS = VoxelShapes.transform(VOXEL_NS_FORWARDS, DirectionTransformation.SWAP_XZ);
	protected static final VoxelShape VOXEL_EW_FORWARDS = VoxelShapes.transform(VOXEL_NS_BACKWARDS, DirectionTransformation.SWAP_XZ);
	protected static final VoxelShape VOXEL_NE = VoxelShapes.cuboid(0.5, 0, 0.125, 1, 0.875, 0.25);
	protected static final VoxelShape VOXEL_ES = VoxelShapes.transform(VOXEL_NE, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_SW = VoxelShapes.transform(VOXEL_ES, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_WN = VoxelShapes.transform(VOXEL_SW, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_NE_COLLAPSED = VoxelShapes.cuboid(0.5, 0, 0, 1, 0.125, 0.875);
	protected static final VoxelShape VOXEL_ES_COLLAPSED = VoxelShapes.transform(VOXEL_NE_COLLAPSED, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_SW_COLLAPSED = VoxelShapes.transform(VOXEL_ES_COLLAPSED, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_WN_COLLAPSED = VoxelShapes.transform(VOXEL_SW_COLLAPSED, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_NEW = VoxelShapes.union(VOXEL_NE, VoxelShapes.transform(VOXEL_NE, DirectionTransformation.INVERT_X));
	protected static final VoxelShape VOXEL_ESN = VoxelShapes.transform(VOXEL_NEW, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_SWE = VoxelShapes.transform(VOXEL_ESN, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_WNS = VoxelShapes.transform(VOXEL_SWE, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_NEW_COLLAPSED = VoxelShapes.union(VOXEL_NE_COLLAPSED, VoxelShapes.transform(VOXEL_NE_COLLAPSED, DirectionTransformation.INVERT_X));
	protected static final VoxelShape VOXEL_ESN_COLLAPSED = VoxelShapes.transform(VOXEL_NEW_COLLAPSED, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_SWE_COLLAPSED = VoxelShapes.transform(VOXEL_ESN_COLLAPSED, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_WNS_COLLAPSED = VoxelShapes.transform(VOXEL_SWE_COLLAPSED, DirectionTransformation.ROT_90_Y_NEG);

	public static final EnumProperty<Collapsed> COLLAPSED = EnumProperty.of("collapsed", Collapsed.class);
	public static final EnumProperty<Shape> SHAPE = EnumProperty.of("shape", Shape.class);
	public static final BooleanProperty COLLAPSING = BooleanProperty.of("collapsing");

	public DominoBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(COLLAPSED, Collapsed.NONE).with(SHAPE, Shape.NORTH_SOUTH).with(COLLAPSING, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(COLLAPSED, SHAPE, COLLAPSING);
	}

	private static Shape getPlacementShape(ItemPlacementContext ctx) {
		if (ctx.getSide() == Direction.UP) {
			Vec3d offset = ctx.getHitPos().subtract(Vec3d.of(ctx.getBlockPos()));
			if (offset.getX() < 0.125 && offset.getZ() < 0.125) return Shape.WEST_NORTH;
			if (offset.getX() > 0.875 && offset.getZ() < 0.125) return Shape.NORTH_EAST;
			if (offset.getX() > 0.875 && offset.getZ() > 0.875) return Shape.EAST_SOUTH;
			if (offset.getX() < 0.125 && offset.getZ() > 0.875) return Shape.SOUTH_WEST;
		}
		return switch (ctx.getHorizontalPlayerFacing()) {
			case DOWN, SOUTH, NORTH, UP -> Shape.NORTH_SOUTH;
			case WEST, EAST -> Shape.EAST_WEST;
		};
	}

	@Override
	protected boolean canReplace(BlockState state, ItemPlacementContext ctx) {
		return !ctx.shouldCancelInteraction() && ctx.getStack().getItem() == this.asItem() && Shape.CORNERS.contains(state.get(SHAPE)) && Shape.CORNERS.get((Shape.CORNERS.indexOf(state.get(SHAPE)) - 1 + Shape.CORNERS.size()) % Shape.CORNERS.size()) == getPlacementShape(ctx) || super.canReplace(state, ctx);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState state = ctx.getWorld().getBlockState(ctx.getBlockPos());
		if (state.isOf(this)) {
			return state.with(SHAPE, Shape.DOUBLES.get(Shape.CORNERS.indexOf(state.get(SHAPE))));
		}
		return this.getDefaultState().with(SHAPE, getPlacementShape(ctx));
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
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (state.get(COLLAPSED) != Collapsed.NONE) {
			world.setBlockState(pos, state.with(COLLAPSED, Collapsed.NONE));
			world.playSound(player, pos, SoundEvents.BLOCK_STONE_STEP, SoundCategory.BLOCKS);
			return ActionResult.SUCCESS;
		} else if (state.get(SHAPE).connections().contains(hit.getSide())) {
			collapse(state, world, pos, player, state.get(SHAPE).connections().getFirst() == hit.getSide(), true);
			return ActionResult.SUCCESS;
		}
		return super.onUse(state, world, pos, player, hit);
	}

	private void collapse(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean forwards, boolean initial) {
		world.setBlockState(pos, state.with(COLLAPSED, forwards ? Collapsed.FORWARDS : Collapsed.BACKWARDS));
		world.playSound(player, pos, initial ? SoundEvents.BLOCK_STONE_PLACE : SoundEvents.BLOCK_STONE_FALL, SoundCategory.BLOCKS);
		world.scheduleBlockTick(pos, state.getBlock(), initial ? 5 : 2);
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (state.get(COLLAPSING)) { // collapsing done!
			world.setBlockState(pos, state.with(COLLAPSING, false));
		} else if (state.get(COLLAPSED) == Collapsed.NONE) { // righted before update!
			world.setBlockState(pos, state.with(COLLAPSING, false));
		} else {
			world.setBlockState(pos, state.with(COLLAPSING, true));
			world.updateNeighbors(pos, state.getBlock());
			world.scheduleBlockTick(pos, state.getBlock(), 1);
		}
	}

	@Override
	protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
		if (state.get(COLLAPSED) == Collapsed.NONE) {
			for (Direction dir : state.get(SHAPE).connections()) {
				BlockState neighbour = world.getBlockState(pos.offset(dir));
				// if a connected collapse is happening
				if (neighbour.isOf(state.getBlock()) && neighbour.get(COLLAPSING) && neighbour.get(SHAPE).connections().contains(dir.getOpposite())) {
					// if the collapse is in the direction that affects us
					if (neighbour.get(COLLAPSED) == (neighbour.get(SHAPE).connections().getFirst() != dir.getOpposite() ? Collapsed.FORWARDS : Collapsed.BACKWARDS)) {
						boolean forwards = dir == state.get(SHAPE).connections().getFirst(); // whether we've been "hit" from the leading side
						collapse(state, world, pos, null, forwards, false);
					}
				}
			}
		}
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
		NORTH_SOUTH("north_south", List.of(Direction.NORTH, Direction.SOUTH), VOXEL_NS, VOXEL_NS_FORWARDS, VOXEL_NS_BACKWARDS),
		EAST_WEST("east_west", List.of(Direction.EAST, Direction.WEST), VOXEL_EW, VOXEL_EW_FORWARDS, VOXEL_EW_BACKWARDS),
		// corners
		NORTH_EAST("north_east", List.of(Direction.NORTH, Direction.EAST), VOXEL_NE, VOXEL_NE_COLLAPSED, VOXEL_NE_COLLAPSED),
		EAST_SOUTH("east_south", List.of(Direction.EAST, Direction.SOUTH), VOXEL_ES, VOXEL_ES_COLLAPSED, VOXEL_ES_COLLAPSED),
		SOUTH_WEST("south_west", List.of(Direction.SOUTH, Direction.WEST), VOXEL_SW, VOXEL_SW_COLLAPSED, VOXEL_SW_COLLAPSED),
		WEST_NORTH("west_north", List.of(Direction.WEST, Direction.NORTH), VOXEL_WN, VOXEL_WN_COLLAPSED, VOXEL_WN_COLLAPSED),
		// doubles
		NORTH_EAST_WEST("north_east_west", List.of(Direction.NORTH, Direction.EAST, Direction.WEST), VOXEL_NEW, VOXEL_NEW_COLLAPSED, VOXEL_NEW_COLLAPSED),
		EAST_SOUTH_NORTH("east_south_north", List.of(Direction.EAST, Direction.SOUTH, Direction.NORTH), VOXEL_ESN, VOXEL_ESN_COLLAPSED, VOXEL_ESN_COLLAPSED),
		SOUTH_WEST_EAST("south_west_east", List.of(Direction.SOUTH, Direction.WEST, Direction.EAST), VOXEL_SWE, VOXEL_SWE_COLLAPSED, VOXEL_SWE_COLLAPSED),
		WEST_NORTH_SOUTH("west_north_south", List.of(Direction.WEST, Direction.NORTH, Direction.SOUTH), VOXEL_WNS, VOXEL_WNS_COLLAPSED, VOXEL_WNS_COLLAPSED);

		public static final List<Shape> CORNERS = List.of(Shape.NORTH_EAST, Shape.EAST_SOUTH, Shape.SOUTH_WEST, Shape.WEST_NORTH);
		public static final List<Shape> DOUBLES = List.of(Shape.NORTH_EAST_WEST, Shape.EAST_SOUTH_NORTH, Shape.SOUTH_WEST_EAST, Shape.WEST_NORTH_SOUTH);

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
