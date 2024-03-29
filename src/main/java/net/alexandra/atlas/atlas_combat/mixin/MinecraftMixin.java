package net.alexandra.atlas.atlas_combat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements IMinecraft {
	@Shadow
	@Final
	public Options options;

	@Shadow
	@Nullable
	public LocalPlayer player;
	@Unique
	public boolean retainAttack;

	@Shadow
	@Nullable
	public HitResult hitResult;
	@Shadow
	private int rightClickDelay;
	@Shadow
	@Final
	private static Logger LOGGER;

	@Shadow
	@Nullable
	public MultiPlayerGameMode gameMode;

	@Shadow
	@Nullable
	public ClientLevel level;

	@Shadow
	protected abstract boolean startAttack();
	@Shadow
	protected abstract void startUseItem();

	@Shadow
	@org.jetbrains.annotations.Nullable
	public Entity crosshairPickEntity;

	@Shadow
	protected int missTime;

	@Unique
	Entity lastPickedEntity = null;

	@Shadow
	@Nullable
	public Screen screen;

	@Inject(method = "tick", at = @At(value = "TAIL"))
	public void injectSomething(CallbackInfo ci) {
		assert player != null;
		if(crosshairPickEntity != null && hitResult != null && (this.hitResult).distanceTo(this.crosshairPickEntity) <= ((PlayerExtensions)player).getAttackRange(player, 2.5)) {
			lastPickedEntity = crosshairPickEntity;
		}
		if (screen != null) {
			this.retainAttack = false;
		}
	}
	@ModifyExpressionValue(method = "handleKeybinds",
			slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 0)
				),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 0))
	public boolean allowBlockHitting(boolean original) {
		if (!original) return false;
		assert player != null;
		boolean bl = !(player.getUseItem().getItem() instanceof IShieldItem shieldItem && !shieldItem.getBlockingType().canBlockHit());
		if(bl && ((PlayerExtensions)this.player).isAttackAvailable(0.0F)) {
			assert hitResult != null;
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				startAttack();
			}
		}
		return bl;
	}
	@Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2))
	public void checkIfCrouch(CallbackInfo ci) {
		assert player != null;
		if(((PlayerExtensions) player).hasEnabledShieldOnCrouch() && player.isCrouching()) {
			while(options.keyUse.consumeClick()) {
				startUseItem();
			}
			while(options.keyAttack.consumeClick()) {
				startAttack();
			}
		}
	}
	@Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"))
	public void checkIfCrouch(MultiPlayerGameMode instance, Player player) {
		if(!((PlayerExtensions) player).hasEnabledShieldOnCrouch() || !player.isCrouching() || !(((LivingEntityExtensions)player).getBlockingItem().getItem() instanceof IShieldItem shieldItem && shieldItem.getBlockingType().canCrouchBlock())) {
			instance.releaseUsingItem(player);
		}
	}
	@ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 4))
	public boolean redirectContinue(boolean original) {
		return original || retainAttack;
	}
	@Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;startAttack()Z"))
	public boolean redirectAttack(Minecraft instance) {
		assert hitResult != null;
		HitResult newResult = redirectResult(hitResult);
		this.hitResult = newResult == null ? hitResult : newResult;
		assert player != null;
		if (!((PlayerExtensions) this.player).isAttackAvailable(0.0F)) {
			if (hitResult.getType() != HitResult.Type.BLOCK) {
				float var1 = this.player.getAttackStrengthScale(0.0F);
				if (var1 < 0.8F) {
					return false;
				}

				if (var1 < 1.0F) {
					this.retainAttack = true;
					return false;
				}
			}
		}
		return startAttack();
	}
	@Inject(method = "startAttack", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1))
	private void startAttack(CallbackInfoReturnable<Boolean> cir) {
		this.retainAttack = false;
	}
	@ModifyExpressionValue(method = "startAttack", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1))
	public HitResult changeResult(HitResult original) {
		return redirectResult(original);
	}
	@Redirect(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V"))
	public void redirectAttack(MultiPlayerGameMode instance, Player player, Entity entity) {
		if (player.distanceTo(entity) <= ((PlayerExtensions)player).getAttackRange(player, 2.5)) {
			instance.attack(player, entity);
		} else {
			((IPlayerGameMode)instance).swingInAir(player);
		}
	}
	@ModifyExpressionValue(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasMissTime()Z"))
	public boolean removeMissTime(boolean original) {
		return false;
	}
	@Redirect(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
	public void redirectReset(LocalPlayer player) {
		assert gameMode != null;
		EntityHitResult result = findEntity(player, 1.0F, ((PlayerExtensions)player).getAttackRange(player, 2.5));
		if(result != null && AtlasCombat.CONFIG.refinedCoyoteTime.get()) {
			if(!(result.getEntity() instanceof Player)) {
				if (result.getEntity() instanceof Guardian
						|| result.getEntity() instanceof Cat
						|| result.getEntity() instanceof Vex
						|| (result.getEntity() instanceof LivingEntity entity && entity.isBaby())
						|| result.getEntity() instanceof Fox
						|| result.getEntity() instanceof Frog
						|| result.getEntity() instanceof Bee
						|| result.getEntity() instanceof Bat
						|| result.getEntity() instanceof AbstractFish
						|| result.getEntity() instanceof Rabbit) {
					result = findEntity(player, 1.0F, ((PlayerExtensions)player).getAttackRange(player, 2.5));
				} else {
					result = findNormalEntity(player, 1.0F, ((PlayerExtensions) player).getAttackRange(player, 2.5));
				}
				if(result != null) {
					this.gameMode.attack(player, result.getEntity());
				} else {
					((IPlayerGameMode)gameMode).swingInAir(player);
				}
			} else {
				((IPlayerGameMode)gameMode).swingInAir(player);
			}
		} else {
			((IPlayerGameMode)gameMode).swingInAir(player);
		}
	}
	@Override
	public final HitResult redirectResult(HitResult instance) {
		if(instance.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult)instance;
			BlockPos blockPos = blockHitResult.getBlockPos();
			assert player != null;
			assert level != null;
			boolean bl = !level.getBlockState(blockPos).canOcclude() && !level.getBlockState(blockPos).getBlock().hasCollision;
			EntityHitResult rayTraceResult = rayTraceEntity(player, 1.0F, ((PlayerExtensions)player).getAttackRange(player, 2.5));
			Entity entity = rayTraceResult != null ? rayTraceResult.getEntity() : null;
			if (entity != null && bl) {
				crosshairPickEntity = entity;
				hitResult = rayTraceResult;
				return hitResult;
			}else {
				return instance;
			}

		}
		return instance;
	}
	@Unique
	@Override
	public final void startUseItem(InteractionHand interactionHand) {
		assert player != null;
		assert gameMode != null;
		if (!gameMode.isDestroying()) {
			this.rightClickDelay = 4;
			if (!this.player.isHandsBusy()) {
				if (this.hitResult == null) {
					LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
				}
				var inputEvent = net.minecraftforge.client.ForgeHooksClient.onClickInput(1, this.options.keyUse, interactionHand);
				if (inputEvent.isCanceled()) {
					if (inputEvent.shouldSwingHand()) this.player.swing(interactionHand);
					return;
				}
				ItemStack itemStack = this.player.getItemInHand(interactionHand);
				if (!itemStack.isEmpty()) {
					this.gameMode.useItem(this.player, interactionHand);
				}
			}
		}
	}
	@Nullable
	@Override
	public EntityHitResult rayTraceEntity(Player player, float partialTicks, double blockReachDistance) {
		Vec3 from = player.getEyePosition(partialTicks);
		Vec3 look = player.getViewVector(partialTicks);
		Vec3 to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

		return ProjectileUtil.getEntityHitResult(
				player.level(),
				player,
				from,
				to,
				new AABB(from, to),
				EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != null
				&& e.isPickable()
				&& e instanceof LivingEntity)
		);
	}
	@Nullable
	@Override
	public EntityHitResult findEntity(Player player, float partialTicks, double blockReachDistance) {
		Vec3 from = player.getEyePosition(partialTicks);
		Vec3 look = player.getViewVector(partialTicks);
		Vec3 to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

		for (double i = -1.0; i <= 1.0; i += 0.1) {
			for (double j = -1.0; j <= 1.0; j += 0.1) {
				for (double k = -1.0; k <= 1.0; k += 0.1) {
					EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
							player.level(),
							player,
							from,
							to,
							new AABB(from, to.add(i, j, k)),
							EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != null
									&& e.isPickable()
									&& e instanceof LivingEntity)
					);
					if(entityHitResult != null) {
						boolean bl3 = entityHitResult.getEntity() == lastPickedEntity;
						if(bl3
								|| entityHitResult.getEntity() instanceof Guardian
								|| entityHitResult.getEntity() instanceof Cat
								|| entityHitResult.getEntity() instanceof Vex
								|| (entityHitResult.getEntity() instanceof LivingEntity entity && entity.isBaby())
								|| entityHitResult.getEntity() instanceof Fox
								|| entityHitResult.getEntity() instanceof Frog
								|| entityHitResult.getEntity() instanceof Bee
								|| entityHitResult.getEntity() instanceof Bat
								|| entityHitResult.getEntity() instanceof AbstractFish
								|| entityHitResult.getEntity() instanceof Rabbit) {
							return entityHitResult;
						}
					}
				}
			}
		}
		return null;
	}
	@Nullable
	@Override
	public EntityHitResult findNormalEntity(Player player, float partialTicks, double blockReachDistance) {
		Vec3 from = player.getEyePosition(partialTicks);
		Vec3 look = player.getViewVector(partialTicks);
		Vec3 to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

		for (double i = -0.5; i <= 0.5; i += 0.1) {
			for (double j = -0.5; j <= 0.5; j += 0.1) {
				for (double k = -0.5; k <= 0.5; k += 0.1) {
					EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
							player.level(),
							player,
							from,
							to,
							new AABB(from, to.add(i, j, k)),
							EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != null
									&& e.isPickable()
									&& e instanceof LivingEntity)
					);
					if(entityHitResult != null) {
						boolean bl3 = entityHitResult.getEntity() == lastPickedEntity;
						if(bl3
								|| entityHitResult.getEntity() instanceof Guardian
								|| entityHitResult.getEntity() instanceof Cat
								|| entityHitResult.getEntity() instanceof Vex
								|| (entityHitResult.getEntity() instanceof LivingEntity entity && entity.isBaby())
								|| entityHitResult.getEntity() instanceof Fox
								|| entityHitResult.getEntity() instanceof Frog
								|| entityHitResult.getEntity() instanceof Bee
								|| entityHitResult.getEntity() instanceof Bat
								|| entityHitResult.getEntity() instanceof AbstractFish
								|| entityHitResult.getEntity() instanceof Rabbit) {
							return entityHitResult;
						}
					}
				}
			}
		}
		return null;
	}
	@Nullable
	@Override
	public EntityHitResult findEntity(Player player, float partialTicks, double blockReachDistance, int strengthMultiplier) {
		if(strengthMultiplier <= 50) {
			strengthMultiplier = 50;
		}
		Vec3 from = player.getEyePosition(partialTicks);
		Vec3 look = player.getViewVector(partialTicks);
		Vec3 to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

		for (double i = -1.0; i <= 1.0; i += 0.1) {
			for (double j = -1.0; j <= 1.0; j += 0.1) {
				for (double k = -1.0; k <= 1.0; k += 0.1) {
					EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
							player.level(),
							player,
							from,
							to,
							new AABB(from, to.add(i * (strengthMultiplier / 100F), j * (strengthMultiplier / 100F), k * (strengthMultiplier / 100F))),
							EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != null
									&& e.isPickable()
									&& e instanceof LivingEntity)
					);
					if(entityHitResult != null) {
						boolean bl3 = entityHitResult.getEntity() == lastPickedEntity;
						if(bl3
								|| entityHitResult.getEntity() instanceof Guardian
								|| entityHitResult.getEntity() instanceof Cat
								|| entityHitResult.getEntity() instanceof Vex
								|| (entityHitResult.getEntity() instanceof LivingEntity entity && entity.isBaby())
								|| entityHitResult.getEntity() instanceof Fox
								|| entityHitResult.getEntity() instanceof Frog
								|| entityHitResult.getEntity() instanceof Bee
								|| entityHitResult.getEntity() instanceof Bat
								|| entityHitResult.getEntity() instanceof AbstractFish
								|| entityHitResult.getEntity() instanceof Rabbit) {
							return entityHitResult;
						}
					}
				}
			}
		}
		return null;
	}
	@Nullable
	@Override
	public EntityHitResult findNormalEntity(Player player, float partialTicks, double blockReachDistance, int strengthMultiplier) {
		if(strengthMultiplier <= 50) {
			strengthMultiplier = 50;
		}
		Vec3 from = player.getEyePosition(partialTicks);
		Vec3 look = player.getViewVector(partialTicks);
		Vec3 to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

		for (double i = -0.5; i <= 0.5; i += 0.1) {
			for (double j = -0.5; j <= 0.5; j += 0.1) {
				for (double k = -0.5; k <= 0.5; k += 0.1) {
					EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
							player.level(),
							player,
							from,
							to,
							new AABB(from, to.add(i * (strengthMultiplier / 100F), j * (strengthMultiplier / 100F), k * (strengthMultiplier / 100F))),
							EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != null
									&& e.isPickable()
									&& e instanceof LivingEntity)
					);
					if(entityHitResult != null) {
						boolean bl3 = entityHitResult.getEntity() == lastPickedEntity;
						if(bl3
								|| entityHitResult.getEntity() instanceof Guardian
								|| entityHitResult.getEntity() instanceof Cat
								|| entityHitResult.getEntity() instanceof Vex
								|| (entityHitResult.getEntity() instanceof LivingEntity entity && entity.isBaby())
								|| entityHitResult.getEntity() instanceof Fox
								|| entityHitResult.getEntity() instanceof Frog
								|| entityHitResult.getEntity() instanceof Bee
								|| entityHitResult.getEntity() instanceof Bat
								|| entityHitResult.getEntity() instanceof AbstractFish
								|| entityHitResult.getEntity() instanceof Rabbit) {
							return entityHitResult;
						}
					}
				}
			}
		}
		return null;
	}
	@Inject(method = "continueAttack", at = @At(value = "HEAD"), cancellable = true)
	private void continueAttack(boolean bl, CallbackInfo ci) {
		assert player != null;
		if (missTime <= 0 && !this.player.isUsingItem()) {
			if (bl && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
				this.retainAttack = false;
			} else if (bl && ((PlayerExtensions)this.player).isAttackAvailable(-1.0F) && ((IOptions)options).autoAttack().get() && AtlasCombat.CONFIG.autoAttackAllowed.get()) {
				this.startAttack();
				ci.cancel();
			}
		}
	}
	@Override
	public void initiateAttack() {
		startAttack();
	}
}
