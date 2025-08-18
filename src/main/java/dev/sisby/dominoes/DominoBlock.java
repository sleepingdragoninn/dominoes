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
	protected static final VoxelShape VOXEL_SE = VoxelShapes.transform(VOXEL_NE, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_SW = VoxelShapes.transform(VOXEL_SE, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_NW = VoxelShapes.transform(VOXEL_SW, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_NE_COLLAPSED = VoxelShapes.cuboid(0.5, 0, 0, 1, 0.125, 0.875);
	protected static final VoxelShape VOXEL_SE_COLLAPSED = VoxelShapes.transform(VOXEL_NE_COLLAPSED, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_SW_COLLAPSED = VoxelShapes.transform(VOXEL_SE_COLLAPSED, DirectionTransformation.ROT_90_Y_NEG);
	protected static final VoxelShape VOXEL_NW_COLLAPSED = VoxelShapes.transform(VOXEL_SW_COLLAPSED, DirectionTransformation.ROT_90_Y_NEG);

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

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		Shape shape = switch (ctx.getHorizontalPlayerFacing()) {
			case DOWN, SOUTH, NORTH, UP -> Shape.NORTH_SOUTH;
			case WEST, EAST -> Shape.EAST_WEST;
		};
		if (ctx.getSide() == Direction.UP) {
			Vec3d offset = ctx.getHitPos().subtract(Vec3d.of(ctx.getBlockPos()));
			if (offset.getX() < 0.125 && offset.getZ() < 0.125) shape = Shape.NORTH_WEST;
			if (offset.getX() > 0.875 && offset.getZ() < 0.125) shape = Shape.NORTH_EAST;
			if (offset.getX() > 0.875 && offset.getZ() > 0.875) shape = Shape.SOUTH_EAST;
			if (offset.getX() < 0.125 && offset.getZ() > 0.875) shape = Shape.SOUTH_WEST;
		}
		return this.getDefaultState().with(SHAPE, shape);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return switch (state.get(SHAPE)) {
			case NORTH_SOUTH -> switch (state.get(COLLAPSED)) {
				case NONE -> VOXEL_NS;
				case FORWARDS -> VOXEL_NS_FORWARDS;
				case BACKWARDS -> VOXEL_NS_BACKWARDS;
			};
			case EAST_WEST -> switch (state.get(COLLAPSED)) {
				case NONE -> VOXEL_EW;
				case FORWARDS -> VOXEL_EW_FORWARDS;
				case BACKWARDS -> VOXEL_EW_BACKWARDS;
			};
			case SOUTH_EAST -> switch (state.get(COLLAPSED)) {
				case NONE -> VOXEL_SE;
				case FORWARDS, BACKWARDS -> VOXEL_SE_COLLAPSED;
			};
			case SOUTH_WEST -> switch (state.get(COLLAPSED)) {
				case NONE -> VOXEL_SW;
				case FORWARDS, BACKWARDS -> VOXEL_SW_COLLAPSED;
			};
			case NORTH_WEST -> switch (state.get(COLLAPSED)) {
				case NONE -> VOXEL_NW;
				case FORWARDS, BACKWARDS -> VOXEL_NW_COLLAPSED;
			};
			case NORTH_EAST -> switch (state.get(COLLAPSED)) {
				case NONE -> VOXEL_NE;
				case FORWARDS, BACKWARDS -> VOXEL_NE_COLLAPSED;
			};
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
				boolean forwards = dir == state.get(SHAPE).connections().getFirst();
				if (neighbour.isOf(state.getBlock()) && neighbour.get(COLLAPSING) && neighbour.get(SHAPE) == state.get(SHAPE) && neighbour.get(COLLAPSED) == (forwards ? Collapsed.FORWARDS : Collapsed.BACKWARDS)) {
					collapse(state, world, pos, null, forwards, false);
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
		NORTH_SOUTH("north_south", List.of(Direction.NORTH, Direction.SOUTH)),
		EAST_WEST("east_west", List.of(Direction.EAST, Direction.WEST)),
		SOUTH_EAST("south_east", List.of(Direction.SOUTH, Direction.EAST)),
		SOUTH_WEST("south_west", List.of(Direction.SOUTH, Direction.WEST)),
		NORTH_WEST("north_west", List.of(Direction.NORTH, Direction.WEST)),
		NORTH_EAST("north_east", List.of(Direction.NORTH, Direction.EAST));

		private final String name;
		private final List<Direction> connections;

		Shape(String name, List<Direction> connections) {
			this.name = name;
			this.connections = connections;
		}

		@Override
		public String asString() {
			return name;
		}

		public List<Direction> connections() {
			return connections;
		}
	}
}
