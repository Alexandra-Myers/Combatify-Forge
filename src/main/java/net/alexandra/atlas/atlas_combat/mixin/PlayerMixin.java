package net.alexandra.atlas.atlas_combat.mixin;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerExtensions, LivingEntityExtensions {
    public PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    @Nullable
    public abstract ItemEntity drop(ItemStack itemStack, boolean b);

    @Shadow
    protected abstract void doAutoAttackOnTouch(@NotNull LivingEntity target);

    @Shadow
    public abstract void awardStat(Stat<?> stat);

    @Shadow
    public abstract void causeFoodExhaustion(float v);

    @Shadow
    public abstract void awardStat(ResourceLocation resourceLocation, int i);

    @Shadow
    protected abstract void removeEntitiesOnShoulder();

    @Shadow
    @Final
    private Abilities abilities;
    @Unique
    protected int attackStrengthStartValue;

    @Unique
    public boolean missedAttackRecovery;
    @Unique
    @Final
    public float baseValue = 1.0F;
    @Unique
    public Multimap additionalModifiers;

    @Unique
    public final Player player = ((Player) (Object)this);

    @Inject(method = "actuallyHurt", at = @At(value = "HEAD"), cancellable = true)
    public void addPiercing(DamageSource source, float amount, CallbackInfo ci) {
        if (!this.isInvulnerableTo(source)) {
            amount = net.minecraftforge.common.ForgeHooks.onLivingHurt((LivingEntity)(Object)this, source, amount);
            if (amount <= 0) return;
            if(source.getEntity() instanceof Player player) {
                Item item = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
                if(item instanceof IPiercingItem piercingItem) {
                    double d = piercingItem.getPiercingLevel();
                    amount = getNewDamageAfterArmorAbsorb(source, amount, d);
                    amount = getNewDamageAfterMagicAbsorb(source, amount, d);
                }else {
                    amount = getDamageAfterArmorAbsorb(source, amount);
                    amount = getDamageAfterMagicAbsorb(source, amount);
                }
            }else {
                amount = getDamageAfterArmorAbsorb(source, amount);
                amount = getDamageAfterMagicAbsorb(source, amount);
            }
            float var8 = Math.max(amount - this.getAbsorptionAmount(), 0.0F);
            this.setAbsorptionAmount(this.getAbsorptionAmount() - (amount - var8));
            float g = amount - var8;
            if (g > 0.0F && g < 3.4028235E37F) {
                awardStat(Stats.DAMAGE_ABSORBED, Math.round(g * 10.0F));
            }

            var8 = net.minecraftforge.common.ForgeHooks.onLivingDamage((LivingEntity)(Object)this, source, var8);
            if (var8 != 0.0F) {
                causeFoodExhaustion(source.getFoodExhaustion());
                float h = this.getHealth();
                this.getCombatTracker().recordDamage(source, h, var8);
                this.setHealth(this.getHealth() - var8);
                if (var8 < 3.4028235E37F) {
                    this.awardStat(Stats.DAMAGE_TAKEN, Math.round(var8 * 10.0F));
                }

            }
        }
        ci.cancel();
    }
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void injectSnowballKb(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!net.minecraftforge.common.ForgeHooks.onPlayerAttack((LivingEntity)(Object)this, source, amount)) cir.setReturnValue(false);
        if (this.isInvulnerableTo(source)) {
            cir.setReturnValue(false);
            cir.cancel();
        } else if (this.abilities.invulnerable && !source.isBypassInvul()) {
            cir.setReturnValue(false);
            cir.cancel();
        } else {
            this.noActionTime = 0;
            if (this.isDeadOrDying()) {
                cir.setReturnValue(false);
                cir.cancel();
            } else {
                if (!this.level.isClientSide) {
                    removeEntitiesOnShoulder();
                }
                float oldDamage = amount;

                if (source.scalesWithDifficulty()) {
                    if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
                        amount = 0.0F;
                    }

                    if (this.level.getDifficulty() == Difficulty.EASY) {
                        amount = Math.min(amount / 2.0F + 1.0F, amount);
                    }

                    if (this.level.getDifficulty() == Difficulty.HARD) {
                        amount = amount * 3.0F / 2.0F;
                    }
                }

                cir.setReturnValue(amount == 0.0F && oldDamage > 0.0F ? false : super.hurt(source, amount));
                cir.cancel();
            }
        }
    }
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
        player.getAttribute(ForgeMod.REACH_DISTANCE.get()).setBaseValue(!AtlasCombat.CONFIG.bedrockBlockReach.get() ? 0 : 2);
        player.getAttribute(ForgeMod.ATTACK_RANGE.get()).setBaseValue(0);
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(!AtlasCombat.CONFIG.fistDamage.get() ? 2 : 1);
    }

    /**
     * @author zOnlyKroks
     * @reason
     */
    @Overwrite()
    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_DAMAGE, !AtlasCombat.CONFIG.fistDamage.get() ? 2 : 1)
                .add(Attributes.MOVEMENT_SPEED, 0.1F)
                .add(Attributes.ATTACK_SPEED)
                .add(Attributes.LUCK)
                .add(ForgeMod.REACH_DISTANCE.get(), !AtlasCombat.CONFIG.bedrockBlockReach.get() ? 0.0 : 2.0)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(ForgeMod.ATTACK_RANGE.get());
    }

    @Redirect(method = "tick", at = @At(value = "FIELD",target = "Lnet/minecraft/world/entity/player/Player;attackStrengthTicker:I",opcode = Opcodes.PUTFIELD))
    public void redirectAttackStrengthTicker(Player instance, int value) {
        if(player.getUseItem().getItem() instanceof SwordItem swordItem) {
            ((ISwordItem)swordItem).addStrengthTimer();
        }
        --instance.attackStrengthTicker;
        setIsParryTicker(getIsParryTicker() + 1);
        if(getIsParryTicker() >= 40) {
            setIsParry(false);
            setIsParryTicker(0);
        }
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameIgnoreDurability(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    public boolean redirectDurability(boolean original) {
        return true;
    }

    @Inject(method = "blockUsingShield", at=@At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;canDisableShield(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"), cancellable = true)
    public void blockUsingShield(@NotNull LivingEntity attacker, CallbackInfo ci) {
        ci.cancel();
    }

    @Override
    public boolean customShieldInteractions(float damage) {
        player.getCooldowns().addCooldown(Items.SHIELD, (int)(damage * 20.0F));
        player.releaseUsingItem();
        player.stopUsingItem();
        player.level.broadcastEntityEvent(player, (byte)30);
        return true;
    }

    @Override
    public boolean hasEnabledShieldOnCrouch() {
        return true;
    }

    /**
     * @author zOnlyKroks
     * @reason change attacks
     */
    @Inject(method = "attack", at = @At(value = "HEAD"), cancellable = true)
    public void attack(Entity target, CallbackInfo ci) {
        newAttack(target);
        ci.cancel();
    }
    @Override
    public void newAttack(Entity target) {
        if (!net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget((Player)(Object)this, target)) return;
        if (target.isAttackable()) {
            if (!target.skipAttackInteraction(player)) {
                if(isAttackAvailable(baseValue)) {
                    float attackDamageBonus;
                    LivingEntity livingEntity = target instanceof LivingEntity ? (LivingEntity) target : null;
                    boolean bl = livingEntity != null;
                    if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TridentItem && bl) {
                        EnchantmentHelper helper = new EnchantmentHelper();
                        attackDamageBonus = ((IEnchantmentHelper)helper).getDamageBonus(player.getMainHandItem(), livingEntity);
                    } else if (bl) {
                        attackDamageBonus = EnchantmentHelper.getDamageBonus(player.getMainHandItem(), livingEntity.getMobType());
                    } else {
                        attackDamageBonus = EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEFINED);
                    }
                    float attackDamage = (float) ((IAttributeInstance)player.getAttribute(Attributes.ATTACK_DAMAGE)).calculateValue(attackDamageBonus);
                    float currentAttackReach = (float) this.getAttackRange(player, 2.5);
                    if (attackDamage > 0.0F) {
                        if(bl) {
                            ((LivingEntityExtensions)livingEntity).setEnemy(player);
                        }
                        boolean bl2 = false;
                        int knockbackBonus = (int) this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
                        knockbackBonus += EnchantmentHelper.getKnockbackBonus(player);
                        if (player.isSprinting()) {
                            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, player.getSoundSource(), 1.0F, 1.0F);
                            ++knockbackBonus;
                            bl2 = true;
                        }

                        boolean isCrit = player.fallDistance > 0.0F
                                && !player.isOnGround()
                                && !player.onClimbable()
                                && !player.isInWater()
                                && !player.hasEffect(MobEffects.BLINDNESS)
                                && !player.isPassenger()
                                && target instanceof LivingEntity;
                        net.minecraftforge.event.entity.player.CriticalHitEvent hitResult = net.minecraftforge.common.ForgeHooks.getCriticalHit((Player)(Object)this, target, isCrit, isCrit ? 1.5F : 1.0F);
                        isCrit = hitResult != null;
                        if (isCrit) {
                            attackDamage *= hitResult.getDamageModifier();
                            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, player.getSoundSource(), 1.0F, 1.0F);
                            player.crit(target);
                        }
                        if (getIsParry()) {
                            attackDamage *= 1.25;
                            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, player.getSoundSource(), 1.0F, 1.0F);
                            player.crit(target);
                            setIsParry(false);
                        }

                        boolean bl4 = false;
                        double d = (player.walkDist - player.walkDistO);
                        if (!isCrit && !bl2 && player.isOnGround() && d < player.getSpeed()) {
                            bl4 = checkSweepAttack();
                        }

                        float j = bl ? livingEntity.getHealth() : 0.0F;
                        boolean bl5 = false;
                        int getFireAspectLvL = EnchantmentHelper.getFireAspect(player);
                        if (getFireAspectLvL > 0 && !target.isOnFire()) {
                            bl5 = true;
                            target.setSecondsOnFire(1 + getFireAspectLvL * 4);
                        }

                        Vec3 vec3 = target.getDeltaMovement();
                        boolean bl6 = target.hurt(DamageSource.playerAttack(player), attackDamage);
                        if (bl6) {
                            if (knockbackBonus > 0) {
                                if (bl) {
                                    ((LivingEntityExtensions)livingEntity)
                                            .newKnockback((knockbackBonus * 0.5F),
                                                    Mth.sin(player.getYRot() * (float) (Math.PI / 180.0)),
                                                    -Mth.cos(player.getYRot() * (float) (Math.PI / 180.0))
                                            );
                                } else {
                                    target.push(
                                            (-Mth.sin(player.getYRot() * (float) (Math.PI / 180.0)) * knockbackBonus * 0.5F),
                                            0.1,
                                            (Mth.cos(player.getYRot() * (float) (Math.PI / 180.0)) * knockbackBonus * 0.5F)
                                    );
                                }

                                player.setDeltaMovement(player.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                                player.setSprinting(false);
                            }

                            if (bl4) {
                                AABB box = target.getBoundingBox().inflate(1.0, 0.25, 1.0);
                                this.betterSweepAttack(box, currentAttackReach, attackDamage, target);
                            }

                            if (target instanceof ServerPlayer serverPlayer && target.hurtMarked) {
                                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
                                target.hurtMarked = false;
                                target.setDeltaMovement(vec3);
                            }

                            if (!isCrit && !bl4) {
                                player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, player.getSoundSource(), 1.0F, 1.0F);
                            }

                            if (attackDamageBonus > 0.0F) {
                                player.magicCrit(target);
                            }

                            player.setLastHurtMob(target);
                            if (bl) {
                                EnchantmentHelper.doPostHurtEffects(livingEntity, player);
                            }

                            EnchantmentHelper.doPostDamageEffects(player, target);
                            ItemStack itemStack2 = player.getMainHandItem();
                            Entity entity = target;
                            if (target instanceof net.minecraftforge.entity.PartEntity partEntity) {
                                entity = partEntity.getParent();
                            }

                            if (!player.level.isClientSide && !itemStack2.isEmpty() && entity instanceof LivingEntity livingEntity1) {
                                ItemStack copy = itemStack2.copy();
                                itemStack2.hurtEnemy(livingEntity1, player);
                                if (itemStack2.isEmpty()) {
                                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem((Player)(Object)this, copy, InteractionHand.MAIN_HAND);
                                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                                }
                            }

                            if (bl) {
                                float m = j - livingEntity.getHealth();
                                player.awardStat(Stats.DAMAGE_DEALT, Math.round(m * 10.0F));

                                if (player.level instanceof ServerLevel serverLevel && m > 2.0F) {
                                    int n = (int) (m * 0.5);
                                    serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5), target.getZ(), n, 0.1, 0.0, 0.1, 0.2);
                                }
                            }

                            player.causeFoodExhaustion(0.1F);
                        } else {
                            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
                            if (bl5) {
                                target.clearFire();
                            }
                        }
                    }

                    this.resetAttackStrengthTicker(true);
                }

            }
        }
    }
    @Override
    public void attackAir() {
        if (this.isAttackAvailable(baseValue)) {
            player.swing(InteractionHand.MAIN_HAND);
            float var1 = (float)((ItemExtensions)player.getItemInHand(InteractionHand.MAIN_HAND).getItem()).getAttackDamage(player);
            if (var1 > 0.0F && this.checkSweepAttack()) {
                float var2 = (float) this.getAttackRange(player, 2.5);
                double var5 = (-Mth.sin(player.yBodyRot * 0.017453292F)) * 2.0;
                double var7 = Mth.cos(player.yBodyRot * 0.017453292F) * 2.0;
                AABB var9 = player.getBoundingBox().inflate(1.0, 0.25, 1.0).move(var5, 0.0, var7);
                betterSweepAttack(var9, var2, var1, null);
            }

            this.resetAttackStrengthTicker(false);
        }
    }
    @Override
    public void resetAttackStrengthTicker(boolean hit) {
        this.missedAttackRecovery = !hit;
        int var2 = (int) (this.getCurrentItemAttackStrengthDelay() * 2);
        if (var2 > this.attackStrengthTicker && AtlasCombat.CONFIG.attackSpeed.get()) {
            this.attackStrengthStartValue = var2;
            this.attackStrengthTicker = this.attackStrengthStartValue;
        }
    }
    /**
     * @author
     * @reason
     */
    @Overwrite
    public float getCurrentItemAttackStrengthDelay() {
        double attackSpeed = getAttribute(Attributes.ATTACK_SPEED).getValue() - 1.5D;
        attackSpeed = Mth.clamp(attackSpeed, 0.1, 1024.0);
        return (float) (1.0F / attackSpeed * 20.0F + 0.5F);
    }
    /**
     * @author
     * @reason
     */
    @Overwrite
    public float getAttackStrengthScale(float baseTime) {
        if (this.attackStrengthStartValue == 0) {
            return 2.0F;
        }
        return Mth.clamp(2.0F * (1.0F - (this.attackStrengthTicker - baseTime) / this.attackStrengthStartValue), 0.0F, 2.0F);
    }

    public float getCurrentAttackReach(float baseValue) {
        return (float)((ItemExtensions) player.getItemInHand(InteractionHand.MAIN_HAND).getItem()).getAttackReach(player);
    }

    @Override
    public boolean isAttackAvailable(float baseTime) {
        if (getAttackStrengthScale(baseTime) < 1.0F) {
            return (this.missedAttackRecovery && this.attackStrengthStartValue - this.attackStrengthTicker - baseTime > 4.0F);
        }
        return true;
    }

    protected boolean checkSweepAttack() {
        return getAttackStrengthScale(baseValue) > 1.95F && EnchantmentHelper.getSweepingDamageRatio(player) > 0.0F && player.getItemInHand(InteractionHand.MAIN_HAND).canPerformAction(ToolActions.SWORD_SWEEP);
    }

    public void betterSweepAttack(AABB var1, float var2, float var3, Entity var4) {
        float sweepingDamageRatio = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * var3;
        List<LivingEntity> livingEntities = player.level.getEntitiesOfClass(LivingEntity.class, var1);
        Iterator<LivingEntity> livingEntityIterator = livingEntities.iterator();

        while (true) {
            LivingEntity var8;
            do {
                do {
                    do {
                        do {
                            if (!livingEntityIterator.hasNext()) {
                                player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
                                if (player.level instanceof ServerLevel serverLevel) {
                                    double var11 = -Mth.sin(player.getYRot() * 0.017453292F);
                                    double var12 = Mth.cos(player.getYRot() * 0.017453292F);
                                    serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + var11, player.getY() + player.getBbHeight() * 0.5, player.getZ() + var12, 0, var11, 0.0, var12, 0.0);
                                }

                                return;
                            }

                            var8 = livingEntityIterator.next();
                        } while (var8 == player);
                    } while (var8 == var4);
                } while (player.isAlliedTo(var8));
            } while (var8 instanceof ArmorStand armorStand && armorStand.isMarker());

            float var9 = var2 + var8.getBbWidth() * 0.5F;
            if (player.distanceToSqr(var8) < (var9 * var9)) {
                ((LivingEntityExtensions)var8).newKnockback(0.5F, Mth.sin(player.getYRot() * 0.017453292F), (-Mth.cos(player.getYRot() * 0.017453292F)));
                var8.hurt(DamageSource.playerAttack(player), sweepingDamageRatio);
            }
        }
    }

    @Override
    public boolean isItemOnCooldown(ItemStack var1) {
        return player.getCooldowns().isOnCooldown(var1.getItem());
    }
    @Override
    public Multimap getAdditionalModifiers() {
        return additionalModifiers;
    }

    @Override
    public double getAttackRange(LivingEntity entity, double baseAttackRange) {
        @Nullable final var attackRange = this.getAttribute(ForgeMod.ATTACK_RANGE.get());
        int var2 = 0;
        baseAttackRange = AtlasCombat.CONFIG.attackReach.get() ? baseAttackRange : Mth.ceil(baseAttackRange);
        float var3 = getAttackStrengthScale(baseValue);
        if (var3 > 1.95F && !player.isCrouching()) {
            var2 = 1;
        }
        return (attackRange != null) ? (baseAttackRange + attackRange.getValue() + var2) : baseAttackRange + var2;
    }

    @Override
    public double getSquaredAttackRange(LivingEntity entity, double sqBaseAttackRange) {
        final var attackRange = getAttackRange(entity, Math.sqrt(sqBaseAttackRange));
        return attackRange * attackRange;
    }

    @Override
    public double getReach(LivingEntity entity, double baseAttackRange) {
        @Nullable final var attackRange = entity.getAttribute(ForgeMod.REACH_DISTANCE.get());
        return (attackRange != null) ? (baseAttackRange + attackRange.getValue()) : baseAttackRange;
    }

    @Override
    public double getSquaredReach(LivingEntity entity, double sqBaseAttackRange) {
        final var attackRange = getReach(entity, Math.sqrt(sqBaseAttackRange));
        return attackRange * attackRange;
    }

    @Override
    public boolean getMissedAttackRecovery() {
        return missedAttackRecovery;
    }

    @Override
    public int getAttackStrengthStartValue() {
        return attackStrengthStartValue;
    }
    @Override
    public void setAttackStrengthTicker2(int value) {
        this.attackStrengthStartValue = value;
        player.attackStrengthTicker = this.attackStrengthStartValue;
    }
}
