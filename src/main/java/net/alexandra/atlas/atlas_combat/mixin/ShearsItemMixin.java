package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraftforge.common.ToolAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShearsItem.class)
public class ShearsItemMixin {
    @Inject(method = "canPerformAction", at = @At(value = "RETURN"), cancellable = true)
    public void injectDefaultActions(ItemStack stack, ToolAction toolAction, CallbackInfoReturnable<Boolean> cir) {
        boolean base = cir.getReturnValue();
        base |= AtlasCombat.DEFAULT_ITEM_ACTIONS.contains(toolAction);
        cir.setReturnValue(base);
    }
}
