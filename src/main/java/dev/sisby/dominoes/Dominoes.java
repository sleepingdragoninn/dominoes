package dev.sisby.dominoes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dominoes implements ModInitializer {
	public static final String ID = "dominoes";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	public static final DominoBlock DOMINO_BLOCK = Registry.register(Registries.BLOCK, Identifier.of(ID, "dominoes"), new DominoBlock(AbstractBlock.Settings.create()
		.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(ID, "dominoes")))
		.mapColor(MapColor.CLEAR)
		.strength(0.5F)
		.sounds(BlockSoundGroup.STONE)
	));
	public static final BlockItem DOMINO_ITEM = Registry.register(Registries.ITEM, Identifier.of(ID, "dominoes"), new BlockItem(DOMINO_BLOCK, new Item.Settings()
		.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ID, "dominoes")))
	));
	public static final ItemGroup ITEM_GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(ID, "items"), FabricItemGroup.builder()
		.displayName(Text.translatable("itemGroup.%s.%s".formatted(ID, "items")))
		.icon(() -> new ItemStack(DOMINO_ITEM))
		.entries((c, e) -> e.add(DOMINO_ITEM))
		.build()
	);

	@Override
	public void onInitialize() {
		LOGGER.info("[Dominoes] i uhhh prefer the madagascar themed ones actually. with the little car");
	}
}
