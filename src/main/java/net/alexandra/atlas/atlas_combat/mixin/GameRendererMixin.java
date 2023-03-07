package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin implements ResourceManagerReloadListener/*, AutoCloseable*/ {

    @Redirect(
        method = "pick(F)V",
        at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(DD)D"))
    private double getActualReachDistance(double a, double b) {
        return b;
    }
}
