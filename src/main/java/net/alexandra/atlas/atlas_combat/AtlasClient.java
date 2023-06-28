package net.alexandra.atlas.atlas_combat;

import com.mojang.serialization.Codec;
import net.alexandra.atlas.atlas_combat.config.ShieldIndicatorStatus;
import net.alexandra.atlas.atlas_combat.util.ArrayListExtensions;
import net.minecraft.client.OptionInstance;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;

import static net.alexandra.atlas.atlas_combat.AtlasCombat.CONFIG;
import static net.alexandra.atlas.atlas_combat.item.ItemRegistry.*;
import static net.alexandra.atlas.atlas_combat.item.ItemRegistry.NETHERITE_LONGSWORD;
import static net.minecraft.world.item.Items.NETHERITE_SWORD;

@Mod.EventBusSubscriber(modid = AtlasCombat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AtlasClient {
    public static final OptionInstance<Boolean> autoAttack = OptionInstance.createBoolean("options.autoAttack", true);
    public static final OptionInstance<Boolean> shieldCrouch = OptionInstance.createBoolean("options.shieldCrouch", true);
    public static final OptionInstance<Boolean> rhythmicAttacks = OptionInstance.createBoolean("options.rhythmicAttack",true);
    public static final OptionInstance<Boolean> protectionIndicator = OptionInstance.createBoolean("options.protIndicator",false);
    public static final OptionInstance<Boolean> fishingRodLegacy = OptionInstance.createBoolean("options.fishingRodLegacy",false);
    public static final OptionInstance<ShieldIndicatorStatus> shieldIndicator = new OptionInstance<>(
            "options.shieldIndicator",
            OptionInstance.noTooltip(),
            OptionInstance.forOptionEnum(),
            new OptionInstance.Enum<>(Arrays.asList(ShieldIndicatorStatus.values()), Codec.INT.xmap(ShieldIndicatorStatus::byId, ShieldIndicatorStatus::getId)),
            ShieldIndicatorStatus.CROSSHAIR,
            value -> {
            }
    );
    @SubscribeEvent
    public void onCreativeTabBuild(CreativeModeTabEvent.BuildContents event) {
        if(event.getTab() == CreativeModeTabs.COMBAT && CONFIG.configOnlyWeapons.get()){
            ArrayListExtensions<ItemLike> arrayListExtensions = new ArrayListExtensions<>();
            arrayListExtensions.addAll(NETHERITE_SWORD, WOODEN_KNIFE.get(), STONE_KNIFE.get(), IRON_KNIFE.get(), GOLD_KNIFE.get(), DIAMOND_KNIFE.get(), NETHERITE_KNIFE.get(), WOODEN_LONGSWORD.get(), STONE_LONGSWORD.get(), IRON_LONGSWORD.get(), GOLD_LONGSWORD.get(), DIAMOND_LONGSWORD.get(), NETHERITE_LONGSWORD.get());
            for (int i = 1; i < arrayListExtensions.size(); i++) {
                event.getEntries().putAfter(new ItemStack(arrayListExtensions.get(i - 1)), new ItemStack(arrayListExtensions.get(i)), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }
}
