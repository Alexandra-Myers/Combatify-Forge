package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.enchantment.CustomEnchantmentHelper;
import net.alexandra.atlas.atlas_combat.extensions.*;
import net.alexandra.atlas.atlas_combat.util.ShieldUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 800)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtensions {

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Shadow
	protected abstract boolean checkTotemDeathProtection(DamageSource source);

	@Shadow
	@Nullable
	protected abstract SoundEvent getDeathSound();

	@Shadow
	protected abstract float getSoundVolume();

	@Shadow
	protected abstract void playHurtSound(DamageSource source);

	@Shadow
	@Nullable
	private DamageSource lastDamageSource;
	@Unique
	boolean isParry = false;

	@Shadow
	private long lastDamageStamp;

	@Shadow
	protected abstract void hurtCurrentlyUsedShield(float amount);

	@Shadow
	protected abstract void blockUsingShield(LivingEntity attacker);
	@Unique
	public int isParryTicker = 0;

	@Shadow
	@Nullable
	protected Player lastHurtByPlayer;
	@Unique
	public Entity enemy;

	@Shadow
	protected int lastHurtByPlayerTime;

	@Shadow
	protected float lastHurt;

	@Shadow
	public abstract double getAttributeValue(Attribute attribute);

	@Shadow
	protected abstract void hurtHelmet(DamageSource par1, float par2);

	@Shadow
	protected int noActionTime;

	@Shadow
	protected abstract float getDamageAfterArmorAbsorb(DamageSource damageSource, float v);

	@Shadow
	protected abstract float getDamageAfterMagicAbsorb(DamageSource damageSource, float v);

	@Shadow
	public abstract float getAbsorptionAmount();

	@Shadow
	public abstract void setAbsorptionAmount(float v);

	@Shadow
	public abstract CombatTracker getCombatTracker();

	@Shadow
	public abstract void setHealth(float v);

	@Shadow
	public abstract float getHealth();

	@Shadow
	protected abstract void hurtArmor(DamageSource damageSource, float v);

	@Shadow
	public abstract int getArmorValue();

	@Shadow
	public abstract boolean hasEffect(MobEffect mobEffect);

	@Shadow
	@javax.annotation.Nullable
	public abstract MobEffectInstance getEffect(MobEffect mobEffect);

	@Shadow
	protected abstract void actuallyHurt(DamageSource p_21240_, float p_21241_);

	@Shadow
	public abstract boolean isBlocking();

	@Shadow
	public abstract boolean isDamageSourceBlocked(DamageSource damageSource);

	@Inject(method = "isBlocking", at = @At(value="RETURN"), cancellable = true)
	public void isBlocking(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(!this.getBlockingItem().isEmpty());
	}
	@Inject(method="blockedByShield", at=@At(value="RETURN"), cancellable = true)
	public void blockedByShield(LivingEntity target, CallbackInfo ci) {
		double x = target.getX() - ((LivingEntity)(Object)this).getX();
		double z = target.getZ() - ((LivingEntity)(Object)this).getZ();
		if(((LivingEntityExtensions)target).getBlockingItem().getItem() instanceof SwordItem) {
			((LivingEntityExtensions)target).newKnockback(0.25F, x, z);
			newKnockback(0.25F, x, z);
			ci.cancel();
		}
		((LivingEntityExtensions)target).newKnockback(0.5F, x, z);
		newKnockback(0.5F, x, z);
		if (((LivingEntity)(Object)this).getMainHandItem().canDisableShield(((LivingEntityExtensions) target).getBlockingItem(), target, (LivingEntity)(Object)this)) {
			float damage = 1.6F + (float) CustomEnchantmentHelper.getChopping(((LivingEntity) (Object)this)) * 0.5F;
			if(target instanceof PlayerExtensions player) {
				player.customShieldInteractions(damage);
			}
		}
	}
	@Inject(method = "actuallyHurt", at = @At(value = "HEAD"), cancellable = true)
	public void addPiercing(DamageSource source, float amount, CallbackInfo ci) {
		if (!this.isInvulnerableTo(source)) {
			amount = net.minecraftforge.common.ForgeHooks.onLivingHurt((LivingEntity)(Object)this, source, amount);
			if (amount <= 0) return;
			if(source.getEntity() instanceof Player player) {
				Item item = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
				if(item instanceof PiercingItem piercingItem) {
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
			float var8 = Math.max(amount - getAbsorptionAmount(), 0.0F);
			setAbsorptionAmount(this.getAbsorptionAmount() - (amount - var8));
			float g = amount - var8;
			if (g > 0.0F && g < 3.4028235E37F && source.getEntity() instanceof ServerPlayer) {
				((ServerPlayer)source.getEntity()).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(g * 10.0F));
			}

			var8 = net.minecraftforge.common.ForgeHooks.onLivingDamage((LivingEntity)(Object)this, source, var8);
			if (var8 != 0.0F) {
				float h = getHealth();
				getCombatTracker().recordDamage(source, h, var8);
				setHealth(h - var8);
				this.setAbsorptionAmount(this.getAbsorptionAmount() - var8);
				this.gameEvent(GameEvent.ENTITY_DAMAGED);
			}
		}
		ci.cancel();
	}
	@ModifyConstant(method = "handleEntityEvent", constant = @Constant(intValue = 20, ordinal = 0))
	private int syncInvulnerability(int x) {
		return 10;
	}
	@Override
	public void setEnemy(Entity enemy) {
		this.enemy = enemy;
	}

	/**
	 * @author zOnlyKroks
	 */
	@Inject(method = "hurt", at = @At("HEAD"),cancellable = true)
	public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(doHurt(source, amount));
		cir.cancel();
	}
	@Override
	public boolean doHurt(DamageSource source, float amount) {
		LivingEntity thisEntity = ((LivingEntity)(Object)this);
		if (!net.minecraftforge.common.ForgeHooks.onLivingAttack(thisEntity, source, amount)) return false;
		if (this.isInvulnerableTo(source)) {
			return false;
		} else if (this.level.isClientSide) {
			return false;
		} else if (thisEntity.isDeadOrDying()) {
			return false;
		} else if (source.isFire() && thisEntity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
			return false;
		} else {
			if (thisEntity.isSleeping() && !this.level.isClientSide) {
				thisEntity.stopSleeping();
			}

			noActionTime = 0;
			float f = amount;
			boolean bl = false;
			float g = 0.0F;
			Entity entity;
			if (amount > 0.0F && this.isDamageSourceBlocked(source) && thisEntity instanceof Player player) {
				if (this.getBlockingItem().getItem() instanceof ShieldItem shieldItem && !player.getCooldowns().isOnCooldown(shieldItem)) {
					float blockStrength = ShieldUtils.getShieldBlockDamageValue(getBlockingItem());
					boolean bl3 = source.isExplosion() || source.isProjectile();
					net.minecraftforge.event.entity.living.ShieldBlockEvent ev = net.minecraftforge.common.ForgeHooks.onShieldBlock(thisEntity, source, bl3 ? amount : blockStrength);
					if(!ev.isCanceled()) {
						if (bl3 || blockStrength >= amount) {
							if (ev.shieldTakesDamage())
								hurtCurrentlyUsedShield(amount);
						} else {
							if (ev.shieldTakesDamage())
								hurtCurrentlyUsedShield(blockStrength);
						}
						amount -= ev.getBlockedDamage();
						g = ev.getBlockedDamage();
						if (!source.isProjectile() && !source.isExplosion()) {
							entity = source.getDirectEntity();
							if (entity instanceof LivingEntity) {
								this.blockUsingShield((LivingEntity) entity);
							}
						}
						bl = true;
					}
				} else if (this.getBlockingItem().getItem() instanceof SwordItem shieldItem) {
					if (player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
						boolean blocked = !source.isExplosion() && !source.isProjectile();
						float actualStrength = ((IShieldItem) shieldItem).getShieldBlockDamageValue(getBlockingItem());
						net.minecraftforge.event.entity.living.ShieldBlockEvent ev = net.minecraftforge.common.ForgeHooks.onShieldBlock(thisEntity, source, source.isProjectile() ? 0 : source.isExplosion() ? 10 : amount * actualStrength);
						if(!ev.isCanceled()) {
							if (!blocked) {
								isParryTicker = 0;
								isParry = true;
								entity = source.getDirectEntity();
								if (entity instanceof LivingEntity) {
									this.blockUsingShield((LivingEntity) entity);
								}
							}
							if (ev.shieldTakesDamage())
								hurtCurrentlyUsedShield(ev.getBlockedDamage());
							amount -= ev.getBlockedDamage();
							g = ev.getBlockedDamage();
							bl = true;
						}
					}
				}
			}
			Entity entity2 = source.getEntity();
			enemy = entity2;
			int invulnerableTime = 10;
			if (entity2 instanceof Player player) {
				invulnerableTime = (int) Math.min(player.getCurrentItemAttackStrengthDelay(), invulnerableTime);
			}
			if(thisEntity.isUsingItem() && thisEntity.getUseItem().isEdible() && !source.isFire() && !source.isMagic() && !source.isFall() && AtlasCombat.CONFIG.eatingInterruption.get()) {
				thisEntity.stopUsingItem();
			}

			if (source.isProjectile()) {
				invulnerableTime = 0;
			}
			thisEntity.animationSpeed = 1.5F;
			boolean bl2 = true;
			if (this.invulnerableTime > 0) {
				if (amount <= this.lastHurt) {
					return false;
				}

				this.actuallyHurt(source, amount - this.lastHurt);
				this.lastHurt = amount;
				bl2 = false;
			} else if(source.isFire()) {
				this.lastHurt = amount;
				this.invulnerableTime = 15;
				this.actuallyHurt(source, amount);
				thisEntity.hurtDuration = 10;
				thisEntity.hurtTime = thisEntity.hurtDuration;
			} else {
				this.lastHurt = amount;
				this.invulnerableTime = invulnerableTime;
				this.actuallyHurt(source, amount);
				thisEntity.hurtDuration = 10;
				thisEntity.hurtTime = thisEntity.hurtDuration;
			}

			if (source.isDamageHelmet() && !thisEntity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
				hurtHelmet(source, amount);
				amount *= 0.75F;
			}

			thisEntity.hurtDir = 0.0F;
			if (entity2 != null) {
				if (entity2 instanceof LivingEntity && !source.isNoAggro()) {
					thisEntity.setLastHurtByMob((LivingEntity)entity2);
				}

				if (entity2 instanceof Player) {
					this.lastHurtByPlayerTime = 100;
					this.lastHurtByPlayer = (Player)entity2;
				} else if (entity2 instanceof Wolf wolf && wolf.isTame()) {
					this.lastHurtByPlayerTime = 100;
					LivingEntity livingEntity = wolf.getOwner();
					if (livingEntity != null && livingEntity.getType() == EntityType.PLAYER) {
						this.lastHurtByPlayer = (Player)livingEntity;
					} else {
						this.lastHurtByPlayer = null;
					}
				}
			}

			if (bl2) {
				if (bl) {
					this.level.broadcastEntityEvent(thisEntity, (byte)29);
				} else if (source instanceof EntityDamageSource && ((EntityDamageSource)source).isThorns()) {
					this.level.broadcastEntityEvent(thisEntity, (byte)33);
				} else {
					byte b;
					if (source == DamageSource.DROWN) {
						b = 36;
					} else if (source.isFire()) {
						b = 37;
					} else if (source == DamageSource.SWEET_BERRY_BUSH) {
						b = 44;
					} else if (source == DamageSource.FREEZE) {
						b = 57;
					} else {
						b = 2;
					}

					this.level.broadcastEntityEvent(thisEntity, b);
				}

				if (source != DamageSource.DROWN && (!bl || amount > 0.0F)) {
					this.markHurt();
				}

				if (entity2 != null) {
					double d = entity2.getX() - this.getX();

					double e;
					for (e = entity2.getZ() - this.getZ(); d * d + e * e < 1.0E-4; e = (Math.random() - Math.random()) * 0.01) {
						d = (Math.random() - Math.random()) * 0.01;
					}

					thisEntity.hurtDir = (float) (Mth.atan2(e, d) * 180.0F / (float) Math.PI - (double) this.getYRot());
					if ((AtlasCombat.CONFIG.fishingHookKB.get() && source.getDirectEntity() instanceof FishingHook) || (!source.isProjectile() && AtlasCombat.CONFIG.midairKB.get())) {
						projectileKnockback(0.5F, d, e);
					} else {
						newKnockback(0.5F, d, e);
					}
				} else {
					thisEntity.hurtDir = (float)((int)(Math.random() * 2.0) * 180);
				}
			}

			if (thisEntity.isDeadOrDying()) {
				if (!this.checkTotemDeathProtection(source)) {
					SoundEvent soundEvent = this.getDeathSound();
					if (bl2 && soundEvent != null) {
						this.playSound(soundEvent, this.getSoundVolume(), thisEntity.getVoicePitch());
					}

					thisEntity.die(source);
				}
			} else if (bl2) {
				this.playHurtSound(source);
			}

			boolean bl3 = !bl || amount > 0.0F;
			if (bl3) {
				this.lastDamageSource = source;
				this.lastDamageStamp = this.level.getGameTime();
			}

			if (((LivingEntity)(Object)this) instanceof ServerPlayer) {
				CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer)thisEntity, source, f, amount, bl);
				if (g > 0.0F && g < 3.4028235E37F) {
					((ServerPlayer)thisEntity).awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(g * 10.0F));
				}
			}

			if (entity2 instanceof ServerPlayer) {
				CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer)entity2, thisEntity, source, f, amount, bl);
			}
			return bl3;
		}
	}

	/**
	 * @author
	 * @reason
	 */
	@Override
	public void newKnockback(float var1, double var2, double var4) {
		double var6 = getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		ItemStack var8 = this.getBlockingItem();
		if (!var8.isEmpty()) {
			var6 = Math.min(1.0, var6 + (double)((IShieldItem)var8.getItem()).getShieldKnockbackResistanceValue(var8));
		}

		var1 = (float)((double)var1 * (1.0 - var6));
		if (!(var1 <= 0.0F)) {
			this.hasImpulse = true;
			Vec3 var9 = this.getDeltaMovement();
			Vec3 var10 = (new Vec3(var2, 0.0, var4)).normalize().scale(var1);
			this.setDeltaMovement(var9.x / 2.0 - var10.x, this.onGround ? Math.min(0.4, (double)var1 * 0.75) : Math.min(0.4, var9.y + (double)var1 * 0.5), var9.z / 2.0 - var10.z);
		}
	}
	@Override
	public void projectileKnockback(float var1, double var2, double var4) {
		double var6 = getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		ItemStack var8 = this.getBlockingItem();
		if (!var8.isEmpty()) {
			var6 = Math.min(1.0, var6 + (double)((IShieldItem)var8.getItem()).getShieldKnockbackResistanceValue(var8));
		}

		var1 = (float)((double)var1 * (1.0 - var6));
		if (!(var1 <= 0.0F)) {
			this.hasImpulse = true;
			Vec3 var9 = this.getDeltaMovement();
			Vec3 var10 = (new Vec3(var2, 0.0, var4)).normalize().scale(var1);
			this.setDeltaMovement(var9.x / 2.0 - var10.x, Math.min(0.4, (double)var1 * 0.75), var9.z / 2.0 - var10.z);
		}
	}

	@Inject(method = "isDamageSourceBlocked", at = @At(value = "HEAD"), cancellable = true)
	public void isDamageSourceBlocked(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		Entity entity = source.getDirectEntity();
		boolean bl = false;
		if (entity instanceof AbstractArrow arrow) {
			if (arrow.getPierceLevel() > 0) {
				bl = true;
			}
		}
		if (!source.isBypassArmor() && this.isBlocking() && !bl) {
			Vec3 sourcePos = source.getSourcePosition();
			if (sourcePos != null) {
				Vec3 currentVector = this.getViewVector(1.0F);
				if (currentVector.y > -0.99 && currentVector.y < 0.99) {
					currentVector = (new Vec3(currentVector.x, 0.0, currentVector.z)).normalize();
					Vec3 sourceVector = sourcePos.vectorTo(this.position());
					sourceVector = (new Vec3(sourceVector.x, 0.0, sourceVector.z)).normalize();
					cir.setReturnValue(sourceVector.dot(currentVector) * 3.1415927410125732 < -0.8726646304130554);
				}
			}
		}

		cir.setReturnValue(false);
	}

	@Override
	public ItemStack getBlockingItem() {
		LivingEntity thisLivingEntity = ((LivingEntity) (Object)this);
		if (thisLivingEntity.isUsingItem() && !thisLivingEntity.getUseItem().isEmpty()) {
			if (thisLivingEntity.getUseItem().getUseAnimation() == UseAnim.BLOCK) {
				return thisLivingEntity.getUseItem();
			}
		} else if ((thisLivingEntity.isOnGround() && thisLivingEntity.isCrouching() && this.hasEnabledShieldOnCrouch() || thisLivingEntity.isPassenger()) && this.hasEnabledShieldOnCrouch()) {
			for(InteractionHand hand : InteractionHand.values()) {
				ItemStack var1 = thisLivingEntity.getItemInHand(hand);
				if (!var1.isEmpty() && var1.getUseAnimation() == UseAnim.BLOCK && !this.isItemOnCooldown(var1) && !(var1.getItem() instanceof SwordItem)) {
					return var1;
				}
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public boolean isItemOnCooldown(ItemStack var1) {
		return false;
	}

	@Override
	public boolean hasEnabledShieldOnCrouch() {
		return true;
	}
	@Override
	public boolean getIsParry() {
		return isParry;
	}
	@Override
	public void setIsParry(boolean isParry) {
		this.isParry = isParry;
	}
	@Override
	public int getIsParryTicker() {
		return isParryTicker;
	}
	@Override
	public void setIsParryTicker(int isParryTicker) {
		this.isParryTicker = isParryTicker;
	}
	@Override
	public float getNewDamageAfterArmorAbsorb(DamageSource source, float amount, double piercingLevel) {
		if (!source.isBypassArmor()) {
			hurtArmor(source, amount);
			double armourStrength = getArmorValue();
			double toughness = this.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
			amount = CombatRules.getDamageAfterAbsorb(amount, (float)(armourStrength - (armourStrength * piercingLevel)), (float)(toughness - (toughness * piercingLevel)));
		}

		return amount;
	}

	@Override
	public float getNewDamageAfterMagicAbsorb(DamageSource source, float amount, double piercingLevel) {
		if (source.isBypassMagic()) {
			return amount;
		} else {
			if (hasEffect(MobEffects.DAMAGE_RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
				int i = (getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
				int j = 25 - i;
				float f = amount * (float)(j - (j * piercingLevel));
				float g = amount;
				amount = Math.max(f / 25.0F, 0.0F);
				float h = g - amount;
				if (h > 0.0F && h < 3.4028235E37F) {
					if (((LivingEntity)(Object)this) instanceof ServerPlayer serverPlayer) {
						serverPlayer.awardStat(Stats.DAMAGE_RESISTED, Math.round(h * 10.0F));
					} else if (source.getEntity() instanceof ServerPlayer) {
						((ServerPlayer)source.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(h * 10.0F));
					}
				}
			}

			if (amount <= 0.0F) {
				return 0.0F;
			} else if (source.isBypassMagic()) {
				return amount;
			} else {
				int i = EnchantmentHelper.getDamageProtection(this.getArmorSlots(), source);
				if (i > 0) {
					amount = CombatRules.getDamageAfterMagicAbsorb(amount, (float)(i - (i * piercingLevel)));
				}

				return amount;
			}
		}
	}
}
