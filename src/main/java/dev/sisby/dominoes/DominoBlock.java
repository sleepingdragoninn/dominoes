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
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class DominoBlock extends Block {
	protected static final VoxelShape VOXEL_NS = VoxelShapes.cuboid(0.25, 0, 0.4375, 0.75, 0.875, 0.5625);
	protected static final VoxelShape VOXEL_NS_BACKWARDS = VoxelShapes.cuboid(0.25, 0, 0, 0.75, 0.125, 0.875);
	protected static final VoxelShape VOXEL_NS_FORWARDS = VoxelShapes.cuboid(0.25, 0, 0.125, 0.75, 0.125, 1);
	protected static final VoxelShape VOXEL_EW = VoxelShapes.cuboid(0.4375, 0, 0.25, 0.5625, 0.875, 0.75);
	protected static final VoxelShape VOXEL_EW_BACKWARDS = VoxelShapes.cuboid(0.125, 0, 0.25, 1, 0.125, 0.75);
	protected static final VoxelShape VOXEL_EW_FORWARDS = VoxelShapes.cuboid(0, 0, 0.25, 0.875, 0.125, 0.75);
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
		return this.getDefaultState().with(SHAPE, switch (ctx.getHorizontalPlayerFacing()) {
			case DOWN, SOUTH, NORTH, UP -> Shape.NORTH_SOUTH;
			case WEST, EAST -> Shape.EAST_WEST;
		});
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
		};
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (state.get(COLLAPSED) != Collapsed.NONE) {
			world.setBlockState(pos, state.with(COLLAPSED, Collapsed.NONE));
			world.playSound(player, pos, SoundEvents.BLOCK_STONE_STEP, SoundCategory.BLOCKS);
			return ActionResult.SUCCESS;
		} else if (state.get(SHAPE).forwards() == hit.getSide()) {
			collapse(state, world, pos, player, true, true);
			return ActionResult.SUCCESS;
		} else if (state.get(SHAPE).forwards() == hit.getSide().getOpposite()) {
			collapse(state, world, pos, player, false, true);
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
			BlockState back = world.getBlockState(pos.offset(state.get(SHAPE).forwards().getOpposite()));
			BlockState front = world.getBlockState(pos.offset(state.get(SHAPE).forwards()));
			if (back.isOf(state.getBlock()) && back.get(COLLAPSING) && back.get(SHAPE) == state.get(SHAPE) && back.get(COLLAPSED) == Collapsed.BACKWARDS) {
				collapse(state, world, pos, null, false, false);
			} else if (front.isOf(state.getBlock()) && front.get(COLLAPSING) && front.get(SHAPE) == state.get(SHAPE) && front.get(COLLAPSED) == Collapsed.FORWARDS) {
				collapse(state, world, pos, null, true, false);
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
		NORTH_SOUTH("north_south", Direction.NORTH),
		EAST_WEST("east_west", Direction.EAST);

		private final String name;
		private final Direction forwards;

		Shape(String name, Direction forwards) {
			this.name = name;
			this.forwards = forwards;
		}

		@Override
		public String asString() {
			return name;
		}

		public Direction forwards() {
			return forwards;
		}
	}
}
