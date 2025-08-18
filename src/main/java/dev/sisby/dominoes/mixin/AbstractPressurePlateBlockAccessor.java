package dev.sisby.dominoes.mixin;

import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.BlockSetType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractPressurePlateBlock.class)
public interface AbstractPressurePlateBlockAccessor {
	@Invoker
	void invokeUpdateNeighbors(World world, BlockPos pos);

	@Accessor
	BlockSetType getBlockSetType();
}
