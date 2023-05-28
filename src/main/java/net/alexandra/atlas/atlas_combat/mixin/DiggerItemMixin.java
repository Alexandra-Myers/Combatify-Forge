package net.alexandra.atlas.atlas_combat.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

import static net.alexandra.atlas.atlas_combat.item.WeaponType.AXE;

@Mixin(DiggerItem.class)
public class DiggerItemMixin extends TieredItem implements Vanishable, ItemExtensions {
	public boolean allToolsAreWeapons;
	private WeaponType type;
	@Shadow
	@Mutable
	@Final
	private Multimap<Attribute, AttributeModifier> defaultModifiers;

	public DiggerItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}
	@Override
	public void changeDefaultModifiers() {
		allToolsAreWeapons = AtlasCombat.CONFIG.toolsAreWeapons.get();
		ImmutableMultimap.Builder<Attribute, AttributeModifier> var3 = ImmutableMultimap.builder();
		var digger = DiggerItem.class.cast(this);
		if(digger instanceof AxeItem) {
			type = AXE;
		}else if(digger instanceof PickaxeItem) {
			type = WeaponType.PICKAXE;
		}else if(digger instanceof ShovelItem) {
			type = WeaponType.SHOVEL;
		}else {
			type = WeaponType.HOE;
		}
		type.addCombatAttributes(this.getTier(), var3);
		defaultModifiers = var3.build();
	}
	@Inject(method = "getAttackDamage", at = @At(value = "RETURN"), cancellable = true)
	public void getDamage(CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(type.getDamage(this.getTier()));
	}
	@Redirect(method = "hurtEnemy",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
	public <T extends LivingEntity> void damage(ItemStack instance, int amount, T entity, Consumer<T> breakCallback) {
		boolean bl = instance.getItem() instanceof AxeItem || instance.getItem() instanceof HoeItem;
		if(allToolsAreWeapons || bl) {
			amount -= 1;
		}
		instance.hurtAndBreak(amount, entity, breakCallback);
	}
	@Override
	public double getAttackReach(Player player) {
		float var2 = 0.0F;
		float var3 = player.getAttackStrengthScale(1.0F);
		if (var3 > 1.95F && !player.isCrouching()) {
			var2 = 1.0F;
		}
		return type.getReach() + 2.5 + var2;
	}

	@Override
	public double getAttackSpeed(Player player) {
		return type.getSpeed(this.getTier()) + 4.0;
	}

	@Override
	public double getAttackDamage(Player player) {
		return type.getDamage(this.getTier()) + 2.0;
	}

	@Override
	public void setStackSize(int stackSize) {
		this.maxStackSize = stackSize;
	}
}
