package dev.sisby.dominoes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dominoes implements ModInitializer {
	public static final String ID = "dominoes";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	public static final DominoBlock DOMINO_BLOCK = Registry.register(Registries.BLOCK, Identifier.of(ID, "domino"), new DominoBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.CLEAR)
		.breakInstantly()
		.pistonBehavior(PistonBehavior.DESTROY)
		.sounds(BlockSoundGroup.STONE)
	));
	public static final Item UNFIRED_DOMINO_ITEM = Registry.register(Registries.ITEM, Identifier.of(ID, "unfired_domino"), new Item(new Item.Settings()
	));
	public static final BlockItem DOMINO_ITEM = Registry.register(Registries.ITEM, Identifier.of(ID, "domino"), new BlockItem(DOMINO_BLOCK, new Item.Settings()
	));
	public static final ItemGroup ITEM_GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(ID, "items"), FabricItemGroup.builder()
		.displayName(Text.translatable("itemGroup.%s.%s".formatted(ID, "items")))
		.icon(() -> new ItemStack(DOMINO_ITEM))
		.entries((c, e) -> {
			e.add(UNFIRED_DOMINO_ITEM);
			e.add(DOMINO_ITEM);
		})
		.build()
	);
	public static final TagKey<EntityType<?>> COLLAPSING = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(ID, "collapsing"));

	@Override
	public void onInitialize() {
		LOGGER.info("[Dominoes] i uhhh prefer the madagascar themed ones actually. with the little car");
	}
}
