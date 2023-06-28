package net.alexandra.atlas.atlas_combat.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.DefaultedItemExtensions;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.extensions.WeaponWithType;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(DiggerItem.class)
public abstract class DiggerItemMixin extends TieredItem implements Vanishable, ItemExtensions, DefaultedItemExtensions, WeaponWithType {
	@Mutable
	private WeaponType type;
	@Shadow
	@Mutable
	@Final
	private Multimap<Attribute, AttributeModifier> defaultModifiers;
	public DiggerItemMixin(Tier tier, Properties properties) {
		super(tier, properties);
	}
	@Redirect(method = "hurtEnemy",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
	public <T extends LivingEntity> void damage(ItemStack instance, int amount, T entity, Consumer<T> breakCallback) {
		boolean bl = instance.getItem() instanceof AxeItem || instance.getItem() instanceof HoeItem;
		if(AtlasCombat.CONFIG.toolsAreWeapons.get() || bl) {
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
	public Multimap<Attribute, AttributeModifier> getDefaultModifiers() {
		return defaultModifiers;
	}
	@Override
	public void setDefaultModifiers(ImmutableMultimap<Attribute, AttributeModifier> modifiers) {
		defaultModifiers = modifiers;
	}

	@Override
	public void changeDefaultModifiers() {
		ImmutableMultimap.Builder<Attribute, AttributeModifier> var3 = ImmutableMultimap.builder();
		type = getWeaponType();
		type.addCombatAttributes(this.getTier(), var3);
		((DefaultedItemExtensions)this).setDefaultModifiers(var3.build());
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

	@Override
	public WeaponType getWeaponType() {
		return WeaponType.PICKAXE;
	}
}