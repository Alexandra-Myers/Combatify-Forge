package net.alexandra.atlas.atlas_combat.item;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AtlasCombat.MODID);
	public static final RegistryObject<Item> WOODEN_KNIFE = registerItem(new ResourceLocation(AtlasCombat.MODID, "wooden_knife"), new KnifeItem(Tiers.WOOD, new Item.Properties()));
	public static final RegistryObject<Item> WOODEN_LONGSWORD = registerItem(new ResourceLocation(AtlasCombat.MODID, "wooden_longsword"), new LongSwordItem(Tiers.WOOD, new Item.Properties()));
	public static final RegistryObject<Item> STONE_KNIFE = registerItem(new ResourceLocation(AtlasCombat.MODID, "stone_knife"), new KnifeItem(Tiers.STONE, new Item.Properties()));
	public static final RegistryObject<Item> STONE_LONGSWORD = registerItem(new ResourceLocation(AtlasCombat.MODID, "stone_longsword"), new LongSwordItem(Tiers.STONE, new Item.Properties()));
	public static final RegistryObject<Item> IRON_KNIFE = registerItem(new ResourceLocation(AtlasCombat.MODID, "iron_knife"), new KnifeItem(Tiers.IRON, new Item.Properties()));
	public static final RegistryObject<Item> IRON_LONGSWORD = registerItem(new ResourceLocation(AtlasCombat.MODID, "iron_longsword"), new LongSwordItem(Tiers.IRON, new Item.Properties()));
	public static final RegistryObject<Item> GOLD_KNIFE = registerItem(new ResourceLocation(AtlasCombat.MODID, "golden_knife"), new KnifeItem(Tiers.GOLD, new Item.Properties()));
	public static final RegistryObject<Item> GOLD_LONGSWORD = registerItem(new ResourceLocation(AtlasCombat.MODID, "golden_longsword"), new LongSwordItem(Tiers.GOLD, new Item.Properties()));
	public static final RegistryObject<Item> DIAMOND_KNIFE = registerItem(new ResourceLocation(AtlasCombat.MODID, "diamond_knife"), new KnifeItem(Tiers.DIAMOND, new Item.Properties()));
	public static final RegistryObject<Item> DIAMOND_LONGSWORD = registerItem(new ResourceLocation(AtlasCombat.MODID, "diamond_longsword"), new LongSwordItem(Tiers.DIAMOND, new Item.Properties()));
	public static final RegistryObject<Item> NETHERITE_KNIFE = registerItem(new ResourceLocation(AtlasCombat.MODID, "netherite_knife"), new KnifeItem(Tiers.NETHERITE, new Item.Properties().fireResistant()));
	public static final RegistryObject<Item> NETHERITE_LONGSWORD = registerItem(new ResourceLocation(AtlasCombat.MODID, "netherite_longsword"), new LongSwordItem(Tiers.NETHERITE, new Item.Properties().fireResistant()));
	public static RegistryObject<Item> registerItem(ResourceLocation resourceLocation, Item item) {
		if (item instanceof BlockItem) {
			((BlockItem)item).registerBlocks(Item.BY_BLOCK, item);
		}

		return ITEMS.register(resourceLocation.getPath(), () -> item);
	}
	public static void registerWeapons(IEventBus bus) {
		ITEMS.register(bus);
	}
}