package dev.sisby.dominoes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DominoBlock extends Block {
	protected static final VoxelShape VOXEL_NS = VoxelShapes.cuboid(0.25, 0, 0.4375, 0.75, 0.875, 0.5625);
	protected static final VoxelShape VOXEL_NS_BACKWARDS = VoxelShapes.cuboid(0.25, 0, 0, 0.75, 0.125, 0.875);
	protected static final VoxelShape VOXEL_NS_FORWARDS = VoxelShapes.cuboid(0.25, 0, 0.125, 0.75, 0.125, 1);
	protected static final VoxelShape VOXEL_EW = VoxelShapes.cuboid(0.4375, 0, 0.25, 0.5625, 0.875, 0.75);
	protected static final VoxelShape VOXEL_EW_BACKWARDS = VoxelShapes.cuboid(0.125, 0, 0.25, 1, 0.125, 0.75);
	protected static final VoxelShape VOXEL_EW_FORWARDS = VoxelShapes.cuboid(0, 0, 0.25, 0.875, 0.125, 0.75);
	public static final EnumProperty<Collapsed> COLLAPSED = EnumProperty.of("collapsed", Collapsed.class);
	public static final EnumProperty<Shape> SHAPE = EnumProperty.of("shape", Shape.class);

	public DominoBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(COLLAPSED, Collapsed.NONE).with(SHAPE, Shape.NORTH_SOUTH));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(COLLAPSED, SHAPE);
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
			world.playSound(player, pos, SoundEvents.BLOCK_STONE_FALL, SoundCategory.BLOCKS);
			return ActionResult.SUCCESS;
		} else if (state.get(SHAPE).forwards() == hit.getSide()) {
			world.setBlockState(pos, state.with(COLLAPSED, Collapsed.FORWARDS));
			world.playSound(player, pos, SoundEvents.BLOCK_STONE_FALL, SoundCategory.BLOCKS);
			return ActionResult.SUCCESS;
		} else if (state.get(SHAPE).forwards() == hit.getSide().getOpposite()) {
			world.setBlockState(pos, state.with(COLLAPSED, Collapsed.BACKWARDS));
			world.playSound(player, pos, SoundEvents.BLOCK_STONE_FALL, SoundCategory.BLOCKS);
			return ActionResult.SUCCESS;
		}
		return super.onUse(state, world, pos, player, hit);
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
