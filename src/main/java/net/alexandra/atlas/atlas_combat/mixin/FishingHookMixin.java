package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Entity {
	public FishingHookMixin(EntityType<?> p_19870_, Level p_19871_) {
		super(p_19870_, p_19871_);
	}

	@Shadow
	@Nullable
	public abstract Player getPlayerOwner();

	@Inject(method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", at = @At(value = "TAIL"))
	public void injectChanges(Player player, Level level, int i, int j, CallbackInfo ci) {
		if(AtlasCombat.CONFIG.fishingHookKB.get())
			setDeltaMovement(getDeltaMovement().multiply(1.5, 1, 1.5));

	}
	@Inject(method = "onHitEntity", at = @At(value = "TAIL"))
	protected void onHitEntity(EntityHitResult entityHitResult, CallbackInfo ci) {
		if(AtlasCombat.CONFIG.fishingHookKB.get() && entityHitResult.getEntity() instanceof LivingEntity livingEntity)
			livingEntity.hurt(damageSources().thrown(FishingHook.class.cast(this), getPlayerOwner()), 0);
	}
}