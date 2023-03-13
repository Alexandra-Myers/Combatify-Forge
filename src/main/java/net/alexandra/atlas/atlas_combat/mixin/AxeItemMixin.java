package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.IAxeItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ToolAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
public class AxeItemMixin extends DiggerItemMixin implements IAxeItem {
	public AxeItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}
	@Override
	public float getShieldCooldownMultiplier(int SHIELD_DISABLE) {
		return 1.6F+SHIELD_DISABLE * 0.5F;
	}
	@Inject(method = "canPerformAction", at = @At(value = "RETURN"), cancellable = true)
	public void injectDefaultActions(ItemStack stack, ToolAction toolAction, CallbackInfoReturnable<Boolean> cir) {
		boolean base = cir.getReturnValue();
		base |= AtlasCombat.DEFAULT_ITEM_ACTIONS.contains(toolAction);
		cir.setReturnValue(base);
	}
}
