package dev.sisby.dominoes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dominoes implements ModInitializer {
	public static final String ID = "dominoes";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	public static final DominoBlock DOMINO_BLOCK = Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(ID, "domino"), new DominoBlock(BlockBehaviour.Properties.of()
		.setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ID, "domino")))
		.mapColor(MapColor.NONE)
		.instabreak()
		.pushReaction(PushReaction.DESTROY)
		.sound(SoundType.STONE)
	));
	public static final Item UNFIRED_DOMINO_ITEM = Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(ID, "unfired_domino"), new Item(new Item.Properties()
		.setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ID, "unfired_domino")))
	));
	public static final BlockItem DOMINO_ITEM = Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(ID, "domino"), new BlockItem(DOMINO_BLOCK, new Item.Properties()
		.setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ID, "domino")))
	));
	public static final CreativeModeTab ITEM_GROUP = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(ID, "items"), FabricItemGroup.builder()
		.title(Component.translatable("itemGroup.%s.%s".formatted(ID, "items")))
		.icon(() -> new ItemStack(DOMINO_ITEM))
		.displayItems((c, e) -> {
			e.accept(UNFIRED_DOMINO_ITEM);
			e.accept(DOMINO_ITEM);
		})
		.build()
	);
	public static final TagKey<EntityType<?>> COLLAPSING = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(ID, "collapsing"));

	@Override
	public void onInitialize() {
		LOGGER.info("[Dominoes] i uhhh prefer the madagascar themed ones actually. with the little car");
	}
}
