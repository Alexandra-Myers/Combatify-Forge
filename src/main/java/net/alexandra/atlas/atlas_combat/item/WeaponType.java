package net.alexandra.atlas.atlas_combat.item;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.serialization.Codec;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.IExtensibleEnum;

import java.util.UUID;

public enum WeaponType implements IExtensibleEnum {
    SWORD,
    LONGSWORD,
    AXE,
    PICKAXE,
    HOE,
    SHOVEL,
    KNIFE,
    TRIDENT;

    public static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    public static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    public static final UUID BASE_ATTACK_REACH_UUID = UUID.fromString("26cb07a3-209d-4110-8e10-1010243614c8");
    public static final UUID BASE_BLOCK_REACH_UUID = UUID.fromString("7f6fa63f-0fbd-4fa8-9acc-69c45c8f68ed");

    WeaponType() {
    }

    public void addCombatAttributes(Tier var1, ImmutableMultimap.Builder<Attribute, AttributeModifier> var2) {
        boolean attackReach = AtlasCombat.CONFIG.attackReach.get();
        boolean attackSpeed = AtlasCombat.CONFIG.attackSpeed.get();
        boolean blockReach = AtlasCombat.CONFIG.blockReach.get();
        float var3 = (float) this.getSpeed(var1);
        float var4 = this.getDamage(var1);
        float var5 = this.getReach();
        float var6 = this.getBlockReach();
        var2.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", var4, AttributeModifier.Operation.ADDITION));
        var2.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", var3, AttributeModifier.Operation.ADDITION));
        if (var5 != 0.0F && attackReach) {
            var2.put(ForgeMod.ATTACK_RANGE.get(), new AttributeModifier(BASE_ATTACK_REACH_UUID, "Weapon modifier", var5, AttributeModifier.Operation.ADDITION));
        }
        if (var6 != 0.0F && blockReach) {
            var2.put(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier(BASE_BLOCK_REACH_UUID, "Weapon modifier", var5, AttributeModifier.Operation.ADDITION));
        }
    }
    public float getDamage(Tier var1) {
        int modifier = AtlasCombat.CONFIG.fistDamage.get() ? 1 : 0;
        float var2 = var1.getAttackDamageBonus() + modifier;
        boolean isTier1 = var1 != Tiers.WOOD && var1 != Tiers.GOLD && var2 != 0;
        boolean bl = isTier1 && AtlasCombat.CONFIG.ctsAttackBalancing.get();
        switch (this) {
            case KNIFE, PICKAXE -> {
                if (bl) {
                    return var2;
                } else {
                    return var2 + 1.0F;
                }
            }
            case SWORD -> {
                if (bl) {
                    return (float) (var2 + AtlasCombat.CONFIG.swordAttackDamageBonus.get());
                } else {
                    return (float) (var2 + AtlasCombat.CONFIG.swordAttackDamageBonus.get() +1.0F);
                }
            }
            case AXE -> {
                if(!AtlasCombat.CONFIG.ctsAttackBalancing.get()) {
                    return !isTier1 ? var1 == Tiers.NETHERITE ? 10 : 9 : 7;
                } else if (bl) {
                    return (float) (var2 + AtlasCombat.CONFIG.axeAttackDamageBonus.get());
                } else {
                    return (float) (var2 + AtlasCombat.CONFIG.axeAttackDamageBonus.get() + 1.0F);
                }
            }
            case LONGSWORD, HOE -> {
                if (var1 != Tiers.IRON && var1 != Tiers.DIAMOND) {
                    if (var1 == Tiers.NETHERITE || var1.getLevel() >= 4) {
                        return (float) (var1 == Tiers.NETHERITE ? AtlasCombat.CONFIG.netheriteHoeAttackDamageBonus.get() + modifier : AtlasCombat.CONFIG.netheriteHoeAttackDamageBonus.get() + var2 - 4 + modifier);
                    }
                    return (float) (AtlasCombat.CONFIG.baseHoeAttackDamageBonus.get().doubleValue());
                }
                return (float) (AtlasCombat.CONFIG.ironDiaHoeAttackDamageBonus.get() + modifier);
            }
            case SHOVEL -> {
                return var2;
            }
            case TRIDENT -> {
                return (float) (AtlasCombat.CONFIG.tridentAttackDamageBonus.get() + modifier + (AtlasCombat.CONFIG.ctsAttackBalancing.get() ? 0 : 1));
            }
            default -> {
                return 0.0F + modifier;
            }
        }
    }

    public double getSpeed(Tier var1) {
        switch (this) {
            case KNIFE -> {
                return AtlasCombat.CONFIG.goldDiaNethHoeAttackSpeed.get();
            }
            case LONGSWORD, SWORD -> {
                return AtlasCombat.CONFIG.swordAttackSpeed.get();
            }
            case AXE, SHOVEL -> {
                return AtlasCombat.CONFIG.axeAttackSpeed.get();
            }
            case TRIDENT -> {
                return AtlasCombat.CONFIG.tridentAttackSpeed.get();
            }
            case HOE -> {
                if (var1 == Tiers.WOOD) {
                    return AtlasCombat.CONFIG.woodenHoeAttackSpeed.get();
                } else if (var1 == Tiers.IRON) {
                    return AtlasCombat.CONFIG.ironHoeAttackSpeed.get();
                } else if (var1 == Tiers.DIAMOND) {
                    return AtlasCombat.CONFIG.goldDiaNethHoeAttackSpeed.get();
                } else if (var1 == Tiers.GOLD) {
                    return AtlasCombat.CONFIG.goldDiaNethHoeAttackSpeed.get();
                } else {
                    if (var1 == Tiers.NETHERITE || var1.getLevel() >= 4) {
                        return AtlasCombat.CONFIG.goldDiaNethHoeAttackSpeed.get();
                    }

                    return AtlasCombat.CONFIG.stoneHoeAttackSpeed.get();
                }
            }
            default -> {
                return AtlasCombat.CONFIG.defaultAttackSpeed.get();
            }
        }
    }

    public float getReach() {
        return switch (this) {
            case KNIFE -> -0.5F;
            case SWORD -> 0.5F;
            case LONGSWORD, HOE, TRIDENT -> 1.0F;
            case AXE -> !AtlasCombat.CONFIG.axeReachBuff.get() ? 0.0F : 0.5F;
            default -> 0.0F;
        };
    }

    public float getBlockReach() {
        return switch (this) {
            case PICKAXE, SWORD, AXE -> 1.5F;
            case SHOVEL -> 1.0F;
            case LONGSWORD, HOE, TRIDENT -> 2.0F;
            default -> 0.0F;
        };
    }
    public static WeaponType create(String name) {
        throw new IllegalStateException("Enum not extended");
    }
}
