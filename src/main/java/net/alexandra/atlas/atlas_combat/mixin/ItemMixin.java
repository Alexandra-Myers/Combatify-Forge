package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.ForgeConfig;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemExtensions, IForgeItem {

	@Override
	public double getAttackReach(Player player) {
		float var2 = 0.0F;
		float var3 = player.getAttackStrengthScale(1.0F);
		if (var3 > 1.95F && !player.isCrouching()) {
			var2 = 1.0F;
		}
		return 2.5 + var2;
	}

	@Override
	public double getAttackSpeed(Player player) {
		return 4.0;
	}
	@Override
	public double getAttackDamage(Player player) {
		return 2.0;
	}
	@Override
	public void setStackSize(int stackSize) {
		((Item) (Object)this).maxStackSize = stackSize;
	}
	@Override
	public void changeDefaultModifiers() {

	}

	@Inject(method = "getUseDuration", at = @At(value = "RETURN"), cancellable = true)
	public void getUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (stack.getItem() instanceof BowlFoodItem || stack.getItem() instanceof SuspiciousStewItem) {
			cir.setReturnValue(AtlasCombat.CONFIG.stewUseDuration.get());
		}else if (stack.getItem().isEdible()) {
			cir.setReturnValue(((Item) (Object)this).getFoodProperties().isFastFood() ? 16 : 32);
		} else {
			cir.setReturnValue(0);
		}
	}

	@Override
	public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
		return AtlasCombat.DEFAULT_ITEM_ACTIONS.contains(toolAction);
	}
}
