package net.alexandra.atlas.atlas_combat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin implements ResourceManagerReloadListener/*, AutoCloseable*/ {

    @Shadow @Final
    Minecraft minecraft;

    @Redirect(
        method = "pick(F)V",
        at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(DD)D"))
    private double getActualReachDistance(double a, double b) {
        return b;
    }
    @ModifyConstant(method = "pick")
    private double getActualAttackRange(final double attackRange) {
        if(attackRange != 9.0D) {
            return attackRange;
        }
        if (minecraft.player != null) {
            return ((PlayerExtensions)minecraft.player).getSquaredAttackRange(minecraft.player, 6.25);
        }
        return 6.25;
    }
}
