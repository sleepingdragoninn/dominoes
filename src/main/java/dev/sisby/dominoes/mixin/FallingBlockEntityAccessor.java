package dev.sisby.dominoes.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityAccessor {
	@Invoker("<init>")
	static FallingBlockEntity invokeConstructor(World world, double x, double y, double z, BlockState blockState) {
		throw new RuntimeException("aaa");
	}
}
