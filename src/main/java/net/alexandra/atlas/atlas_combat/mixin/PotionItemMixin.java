package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionItemMixin {


    @Inject(method = "getUseDuration", at = @At(value = "RETURN"), cancellable = true)
    public void getUseDuration(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(AtlasCombat.CONFIG.potionUseDuration.get());
    }
}
