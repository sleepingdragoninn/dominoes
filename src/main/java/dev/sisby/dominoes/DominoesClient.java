package dev.sisby.dominoes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.render.BlockRenderLayer;

public class DominoesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.putBlock(Dominoes.DOMINO_BLOCK, BlockRenderLayer.TRANSLUCENT);
	}
}
