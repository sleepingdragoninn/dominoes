package dev.sisby.dominoes;

import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class DominoShapes {
	public static final VoxelShape VOXEL_NORTH_SOUTH_STANDING = VoxelShapes.union(
		VoxelShapes.cuboid(0.25, 0.0, 0.4375, 0.75, 0.875, 0.5625)
	);
	public static final VoxelShape VOXEL_NORTH_SOUTH_FORWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.25, 0.0, 0.125, 0.75, 0.125, 1.0)
	);
	public static final VoxelShape VOXEL_NORTH_SOUTH_BACKWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.25, 0.0, 0.0, 0.75, 0.125, 0.875)
	);
	public static final VoxelShape VOXEL_EAST_WEST_STANDING = VoxelShapes.union(
		VoxelShapes.cuboid(0.4375, 0.0, 0.25, 0.5625, 0.875, 0.75)
	);
	public static final VoxelShape VOXEL_EAST_WEST_FORWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.0, 0.0, 0.25, 0.875, 0.125, 0.75)
	);
	public static final VoxelShape VOXEL_EAST_WEST_BACKWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.125, 0.0, 0.25, 1.0, 0.125, 0.75)
	);
	public static final VoxelShape VOXEL_NORTH_SOUTH_STACK_STANDING = VoxelShapes.union(
		VoxelShapes.cuboid(0.25, 0.0, 0.125, 0.75, 0.875, 0.25),
		VoxelShapes.cuboid(0.25, 0.0, 0.75, 0.75, 0.875, 0.875)
	);
	public static final VoxelShape VOXEL_NORTH_SOUTH_STACK_FORWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.25, 0.0, 0.125, 0.75, 0.125, 1.0)
	);
	public static final VoxelShape VOXEL_NORTH_SOUTH_STACK_BACKWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.25, 0.0, 0.0, 0.75, 0.125, 0.875)
	);
	public static final VoxelShape VOXEL_EAST_WEST_STACK_STANDING = VoxelShapes.union(
		VoxelShapes.cuboid(0.125, 0.0, 0.25, 0.25, 0.875, 0.75),
		VoxelShapes.cuboid(0.75, 0.0, 0.25, 0.875, 0.875, 0.75)
	);
	public static final VoxelShape VOXEL_EAST_WEST_STACK_FORWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.0, 0.0, 0.25, 0.875, 0.125, 0.75)
	);
	public static final VoxelShape VOXEL_EAST_WEST_STACK_BACKWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.125, 0.0, 0.25, 1.0, 0.125, 0.75)
	);
	public static final VoxelShape VOXEL_NORTH_EAST_STANDING = VoxelShapes.union(
		VoxelShapes.cuboid(0.53125, 0.0, 0.375, 0.65625, 0.875, 0.5),
		VoxelShapes.cuboid(0.59375, 0.0, 0.3125, 0.78125, 0.875, 0.375),
		VoxelShapes.cuboid(0.65625, 0.0, 0.25, 0.84375, 0.875, 0.3125),
		VoxelShapes.cuboid(0.65625, 0.0, 0.375, 0.71875, 0.875, 0.4375),
		VoxelShapes.cuboid(0.71875, 0.0, 0.1875, 0.90625, 0.875, 0.25),
		VoxelShapes.cuboid(0.78125, 0.0, 0.125, 0.90625, 0.875, 0.1875)
	);
	public static final VoxelShape VOXEL_NORTH_EAST_FORWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.5, 0.0, 0.0, 1.0, 0.125, 0.875)
	);
	public static final VoxelShape VOXEL_NORTH_EAST_BACKWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.5, 0.0, 0.0, 1.0, 0.125, 0.875)
	);
	public static final VoxelShape VOXEL_EAST_SOUTH_STANDING = VoxelShapes.union(
		VoxelShapes.cuboid(0.5, 0.0, 0.53125, 0.625, 0.875, 0.65625),
		VoxelShapes.cuboid(0.5625, 0.0, 0.65625, 0.75, 0.875, 0.71875),
		VoxelShapes.cuboid(0.625, 0.0, 0.59375, 0.6875, 0.875, 0.65625),
		VoxelShapes.cuboid(0.625, 0.0, 0.71875, 0.8125, 0.875, 0.78125),
		VoxelShapes.cuboid(0.6875, 0.0, 0.78125, 0.875, 0.875, 0.84375),
		VoxelShapes.cuboid(0.75, 0.0, 0.84375, 0.875, 0.875, 0.90625)
	);
	public static final VoxelShape VOXEL_EAST_SOUTH_FORWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.125, 0.0, 0.5, 1.0, 0.125, 1.0)
	);
	public static final VoxelShape VOXEL_EAST_SOUTH_BACKWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.125, 0.0, 0.5, 1.0, 0.125, 1.0)
	);
	public static final VoxelShape VOXEL_SOUTH_WEST_STANDING = VoxelShapes.union(
		VoxelShapes.cuboid(0.09375, 0.0, 0.75, 0.21875, 0.875, 0.875),
		VoxelShapes.cuboid(0.15625, 0.0, 0.6875, 0.34375, 0.875, 0.75),
		VoxelShapes.cuboid(0.21875, 0.0, 0.625, 0.40625, 0.875, 0.6875),
		VoxelShapes.cuboid(0.21875, 0.0, 0.75, 0.28125, 0.875, 0.8125),
		VoxelShapes.cuboid(0.28125, 0.0, 0.5625, 0.46875, 0.875, 0.625),
		VoxelShapes.cuboid(0.34375, 0.0, 0.5, 0.46875, 0.875, 0.5625)
	);
	public static final VoxelShape VOXEL_SOUTH_WEST_FORWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.0, 0.0, 0.125, 0.5, 0.125, 1.0)
	);
	public static final VoxelShape VOXEL_SOUTH_WEST_BACKWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.0, 0.0, 0.125, 0.5, 0.125, 1.0)
	);
	public static final VoxelShape VOXEL_WEST_NORTH_STANDING = VoxelShapes.union(
		VoxelShapes.cuboid(0.125, 0.0, 0.09375, 0.25, 0.875, 0.21875),
		VoxelShapes.cuboid(0.1875, 0.0, 0.21875, 0.375, 0.875, 0.28125),
		VoxelShapes.cuboid(0.25, 0.0, 0.15625, 0.3125, 0.875, 0.21875),
		VoxelShapes.cuboid(0.25, 0.0, 0.28125, 0.4375, 0.875, 0.34375),
		VoxelShapes.cuboid(0.3125, 0.0, 0.34375, 0.5, 0.875, 0.40625),
		VoxelShapes.cuboid(0.375, 0.0, 0.40625, 0.5, 0.875, 0.46875)
	);
	public static final VoxelShape VOXEL_WEST_NORTH_FORWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.875, 0.125, 0.5)
	);
	public static final VoxelShape VOXEL_WEST_NORTH_BACKWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.875, 0.125, 0.5)
	);
	public static final VoxelShape VOXEL_NORTH_EAST_WEST_STANDING = VoxelShapes.union(
		VoxelShapes.cuboid(0.09375, 0.0, 0.125, 0.21875, 0.875, 0.25),
		VoxelShapes.cuboid(0.15625, 0.0, 0.25, 0.34375, 0.875, 0.3125),
		VoxelShapes.cuboid(0.21875, 0.0, 0.1875, 0.28125, 0.875, 0.25),
		VoxelShapes.cuboid(0.21875, 0.0, 0.3125, 0.40625, 0.875, 0.375),
		VoxelShapes.cuboid(0.28125, 0.0, 0.375, 0.46875, 0.875, 0.4375),
		VoxelShapes.cuboid(0.34375, 0.0, 0.4375, 0.46875, 0.875, 0.5),
		VoxelShapes.cuboid(0.53125, 0.0, 0.375, 0.65625, 0.875, 0.5),
		VoxelShapes.cuboid(0.59375, 0.0, 0.3125, 0.78125, 0.875, 0.375),
		VoxelShapes.cuboid(0.65625, 0.0, 0.25, 0.84375, 0.875, 0.3125),
		VoxelShapes.cuboid(0.65625, 0.0, 0.375, 0.71875, 0.875, 0.4375),
		VoxelShapes.cuboid(0.71875, 0.0, 0.1875, 0.90625, 0.875, 0.25),
		VoxelShapes.cuboid(0.78125, 0.0, 0.125, 0.90625, 0.875, 0.1875)
	);
	public static final VoxelShape VOXEL_NORTH_EAST_WEST_FORWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.125, 0.875)
	);
	public static final VoxelShape VOXEL_NORTH_EAST_WEST_BACKWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.125, 0.875)
	);
	public static final VoxelShape VOXEL_EAST_SOUTH_NORTH_STANDING = VoxelShapes.union(
		VoxelShapes.cuboid(0.5, 0.0, 0.34375, 0.625, 0.875, 0.46875),
		VoxelShapes.cuboid(0.5, 0.0, 0.53125, 0.625, 0.875, 0.65625),
		VoxelShapes.cuboid(0.5625, 0.0, 0.28125, 0.75, 0.875, 0.34375),
		VoxelShapes.cuboid(0.5625, 0.0, 0.65625, 0.75, 0.875, 0.71875),
		VoxelShapes.cuboid(0.625, 0.0, 0.21875, 0.8125, 0.875, 0.28125),
		VoxelShapes.cuboid(0.625, 0.0, 0.34375, 0.6875, 0.875, 0.40625),
		VoxelShapes.cuboid(0.625, 0.0, 0.59375, 0.6875, 0.875, 0.65625),
		VoxelShapes.cuboid(0.625, 0.0, 0.71875, 0.8125, 0.875, 0.78125),
		VoxelShapes.cuboid(0.6875, 0.0, 0.15625, 0.875, 0.875, 0.21875),
		VoxelShapes.cuboid(0.6875, 0.0, 0.78125, 0.875, 0.875, 0.84375),
		VoxelShapes.cuboid(0.75, 0.0, 0.09375, 0.875, 0.875, 0.15625),
		VoxelShapes.cuboid(0.75, 0.0, 0.84375, 0.875, 0.875, 0.90625)
	);
	public static final VoxelShape VOXEL_EAST_SOUTH_NORTH_FORWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.125, 0.0, 0.0, 1.0, 0.125, 1.0)
	);
	public static final VoxelShape VOXEL_EAST_SOUTH_NORTH_BACKWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.125, 0.0, 0.0, 1.0, 0.125, 1.0)
	);
	public static final VoxelShape VOXEL_SOUTH_WEST_EAST_STANDING = VoxelShapes.union(
		VoxelShapes.cuboid(0.09375, 0.0, 0.75, 0.21875, 0.875, 0.875),
		VoxelShapes.cuboid(0.15625, 0.0, 0.6875, 0.34375, 0.875, 0.75),
		VoxelShapes.cuboid(0.21875, 0.0, 0.625, 0.40625, 0.875, 0.6875),
		VoxelShapes.cuboid(0.21875, 0.0, 0.75, 0.28125, 0.875, 0.8125),
		VoxelShapes.cuboid(0.28125, 0.0, 0.5625, 0.46875, 0.875, 0.625),
		VoxelShapes.cuboid(0.34375, 0.0, 0.5, 0.46875, 0.875, 0.5625),
		VoxelShapes.cuboid(0.53125, 0.0, 0.5, 0.65625, 0.875, 0.625),
		VoxelShapes.cuboid(0.59375, 0.0, 0.625, 0.78125, 0.875, 0.6875),
		VoxelShapes.cuboid(0.65625, 0.0, 0.5625, 0.71875, 0.875, 0.625),
		VoxelShapes.cuboid(0.65625, 0.0, 0.6875, 0.84375, 0.875, 0.75),
		VoxelShapes.cuboid(0.71875, 0.0, 0.75, 0.90625, 0.875, 0.8125),
		VoxelShapes.cuboid(0.78125, 0.0, 0.8125, 0.90625, 0.875, 0.875)
	);
	public static final VoxelShape VOXEL_SOUTH_WEST_EAST_FORWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.0, 0.0, 0.125, 1.0, 0.125, 1.0)
	);
	public static final VoxelShape VOXEL_SOUTH_WEST_EAST_BACKWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.0, 0.0, 0.125, 1.0, 0.125, 1.0)
	);
	public static final VoxelShape VOXEL_WEST_NORTH_SOUTH_STANDING = VoxelShapes.union(
		VoxelShapes.cuboid(0.125, 0.0, 0.09375, 0.25, 0.875, 0.21875),
		VoxelShapes.cuboid(0.125, 0.0, 0.78125, 0.25, 0.875, 0.90625),
		VoxelShapes.cuboid(0.1875, 0.0, 0.21875, 0.375, 0.875, 0.28125),
		VoxelShapes.cuboid(0.1875, 0.0, 0.71875, 0.375, 0.875, 0.78125),
		VoxelShapes.cuboid(0.25, 0.0, 0.15625, 0.3125, 0.875, 0.21875),
		VoxelShapes.cuboid(0.25, 0.0, 0.28125, 0.4375, 0.875, 0.34375),
		VoxelShapes.cuboid(0.25, 0.0, 0.65625, 0.4375, 0.875, 0.71875),
		VoxelShapes.cuboid(0.25, 0.0, 0.78125, 0.3125, 0.875, 0.84375),
		VoxelShapes.cuboid(0.3125, 0.0, 0.34375, 0.5, 0.875, 0.40625),
		VoxelShapes.cuboid(0.3125, 0.0, 0.59375, 0.5, 0.875, 0.65625),
		VoxelShapes.cuboid(0.375, 0.0, 0.40625, 0.5, 0.875, 0.46875),
		VoxelShapes.cuboid(0.375, 0.0, 0.53125, 0.5, 0.875, 0.59375)
	);
	public static final VoxelShape VOXEL_WEST_NORTH_SOUTH_FORWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.875, 0.125, 1.0)
	);
	public static final VoxelShape VOXEL_WEST_NORTH_SOUTH_BACKWARDS = VoxelShapes.union(
		VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.875, 0.125, 1.0)
	);
}
