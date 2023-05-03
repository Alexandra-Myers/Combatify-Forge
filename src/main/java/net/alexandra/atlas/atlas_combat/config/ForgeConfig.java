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
    public ForgeConfigSpec.BooleanValue saturationHealing;
    public ForgeConfigSpec.BooleanValue axeReachBuff;
    public ForgeConfigSpec.BooleanValue blockReach;
    public ForgeConfigSpec.BooleanValue attackReach;
    public ForgeConfigSpec.BooleanValue attackSpeed;
    public ForgeConfigSpec.BooleanValue ctsAttackBalancing;
    public ForgeConfigSpec.BooleanValue eatingInterruption;
    public ForgeConfigSpec.IntValue swordProtectionEfficacy;
    public ForgeConfigSpec.IntValue potionUseDuration;
    public ForgeConfigSpec.IntValue honeyBottleUseDuration;
    public ForgeConfigSpec.IntValue milkBucketUseDuration;
    public ForgeConfigSpec.IntValue stewUseDuration;
    public ForgeConfigSpec.IntValue instantHealthBonus;
    public ForgeConfigSpec.IntValue eggItemCooldown;
    public ForgeConfigSpec.IntValue snowballItemCooldown;

    public ForgeConfigSpec.DoubleValue snowballDamage;
    public ForgeConfigSpec.DoubleValue eggDamage;
    public ForgeConfigSpec.DoubleValue bowUncertainty;
    public ForgeConfigSpec.DoubleValue swordAttackDamageBonus;
    public ForgeConfigSpec.DoubleValue axeAttackDamageBonus;
    public ForgeConfigSpec.DoubleValue tridentAttackDamageBonus;
    public ForgeConfigSpec.DoubleValue baseHoeAttackDamageBonus;
    public ForgeConfigSpec.DoubleValue ironDiaHoeAttackDamageBonus;
    public ForgeConfigSpec.DoubleValue netheriteHoeAttackDamageBonus;

    public ForgeConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Booleans");

        toolsAreWeapons = builder.define("toolsAreWeapons", false);
        bedrockBlockReach = builder.define("bedrockBlockReach", false);
        refinedCoyoteTime = builder.define("refinedCoyoteTime", false);
        midairKB = builder.define("midairKB",false);
        fishingHookKB = builder.define("fishingHookKB",false);
        swordBlocking = builder.define("swordBlocking",false);
        saturationHealing = builder.define("saturationHealing",false);
        axeReachBuff = builder.define("axeReachBuff",false);
        blockReach = builder.define("blockReach", true);
        attackReach = builder.define("attackReach", true);
        attackSpeed = builder.define("attackSpeed", true);
        ctsAttackBalancing = builder.define("ctsAttackBalancing", true);
        fistDamage = builder.define("fistDamage", false);
        eatingInterruption = builder.define("eatingInterruption", false);

        builder.comment("Integers");

        swordProtectionEfficacy = builder.defineInRange("potionUseDuration", 0,-3,4);
        potionUseDuration = builder.defineInRange("potionUseDuration", 20,1,1000);
        honeyBottleUseDuration = builder.defineInRange("honeyBottleUseDuration",20,1,1000);
        milkBucketUseDuration = builder.defineInRange("milkBucketUseDuration",20,1,1000);
        stewUseDuration = builder.defineInRange("stewUseDuration",20,1,1000);
        instantHealthBonus = builder.defineInRange("instantHealthBonus", 6, 1,1000);
        eggItemCooldown = builder.defineInRange("eggItemCooldown",4,1,1000);
        snowballItemCooldown = builder.defineInRange("snowballItemCooldown",4,1,1000);

        builder.comment("Doubles");

        snowballDamage = builder.defineInRange("snowballDamage",0F,0F,40F);

        eggDamage = builder.defineInRange("eggDamage",0F,0F,40F);

        bowUncertainty = builder.defineInRange("bowUncertainty",0.25F,0F,4F);

        swordAttackDamageBonus = builder.defineInRange("swordAttackDamageBonus",1F,0F,10F);

        axeAttackDamageBonus = builder.defineInRange("axeAttackDamageBonus",2F,0F,10F);

        tridentAttackDamageBonus = builder.defineInRange("tridentAttackDamageBonus",5F,0F,10F);

        baseHoeAttackDamageBonus= builder.defineInRange("baseHoeAttackDamageBonus",0F,0F,10F);

        ironDiaHoeAttackDamageBonus = builder.defineInRange("ironDiaHoeAttackDamageBonus",1F,0F,10F);

        netheriteHoeAttackDamageBonus = builder.defineInRange("netheriteHoeAttackDamageBonus",2F,0F,10F);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,builder.build());
    }

}
