package dev.sisby.dominoes.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BasePressurePlateBlock.class)
public interface AbstractPressurePlateBlockAccessor {
	@Invoker
	void invokeUpdateNeighbours(Level world, BlockPos pos);

	@Accessor
	BlockSetType getType();
}
