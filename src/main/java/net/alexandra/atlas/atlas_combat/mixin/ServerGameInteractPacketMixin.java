package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.IHandler;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/server/network/ServerGamePacketListenerImpl$1")
public abstract class ServerGameInteractPacketMixin implements IHandler {
    @Override
    public void onMissAttack() {
        ((PlayerExtensions) AtlasCombat.player).attackAir();
    }
    @Redirect(method = "onAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;canHit(Lnet/minecraft/world/entity/Entity;D)Z"))
    public boolean redirectPadding(ServerPlayer instance, Entity entity, double v) {
        return instance.canHit(entity, 0);
    }
    @Redirect(method = "performInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;canInteractWith(Lnet/minecraft/world/entity/Entity;D)Z"))
    public boolean redirectPadding1(ServerPlayer instance, Entity entity, double v) {
        return instance.canHit(entity, 0);
    }

}
