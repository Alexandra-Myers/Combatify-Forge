package net.alexandra.atlas.atlas_combat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ForgeConfig {

    public ForgeConfigSpec.BooleanValue toolsAreWeapons;
    public ForgeConfigSpec.BooleanValue bedrockBlockReach;
    public ForgeConfigSpec.BooleanValue refinedCoyoteTime;
    public ForgeConfigSpec.BooleanValue midairKB;
    public ForgeConfigSpec.BooleanValue fishingHookKB;
    public ForgeConfigSpec.BooleanValue fistDamage;
    public ForgeConfigSpec.BooleanValue swordBlocking;
    public ForgeConfigSpec.BooleanValue blockReach;
    public ForgeConfigSpec.BooleanValue attackReach;
    public ForgeConfigSpec.IntValue potionUseDuration;
    public ForgeConfigSpec.IntValue honeyBottleUseDuration;
    public ForgeConfigSpec.IntValue milkBucketUseDuration;
    public ForgeConfigSpec.IntValue stewUseDuration;
    public ForgeConfigSpec.IntValue instantHealthBonus;
    public ForgeConfigSpec.IntValue eggItemCooldown;
    public ForgeConfigSpec.IntValue snowballItemCooldown;

    public ForgeConfigSpec.DoubleValue snowballDamage;
    public ForgeConfigSpec.DoubleValue bowUncertainty;

    public ForgeConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Booleans");

        toolsAreWeapons = builder.define("toolsAreWeapons", false);
        bedrockBlockReach = builder.define("bedrockBlockReach", false);
        refinedCoyoteTime = builder.define("refinedCoyoteTime", false);
        midairKB = builder.define("midairKB",false);
        fishingHookKB = builder.define("fishingHookKB",false);
        swordBlocking = builder.define("swordBlocking",false);
        blockReach = builder.define("blockReach", true);
        attackReach = builder.define("attackReach", true);
        fistDamage = builder.define("fistDamage", false);

        builder.comment("Integers");

        potionUseDuration = builder.defineInRange("potionUseDuration", 20,1,1000);
        honeyBottleUseDuration = builder.defineInRange("honeyBottleUseDuration",20,1,1000);
        milkBucketUseDuration = builder.defineInRange("milkBucketUseDuration",20,1,1000);
        stewUseDuration = builder.defineInRange("stewUseDuration",20,1,1000);
        instantHealthBonus = builder.defineInRange("instantHealthBonus", 6, 1,1000);
        eggItemCooldown = builder.defineInRange("eggItemCooldown",4,1,1000);
        snowballItemCooldown = builder.defineInRange("snowballItemCooldown",4,1,1000);

        builder.comment("Doubles");

        snowballDamage = builder.defineInRange("snowballDamage",0F,0F,40F);

        bowUncertainty = builder.defineInRange("bowUncertainty",0.25F,0F,4F);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,builder.build());
    }

}
