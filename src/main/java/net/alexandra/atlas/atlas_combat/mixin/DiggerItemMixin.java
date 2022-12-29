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
import org.spongepowered.asm.mixin.injection.Redirect;

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
		if(((DiggerItem) (Object)this) instanceof AxeItem) {
			type = AXE;
		}else if(((DiggerItem) (Object)this) instanceof PickaxeItem) {
			type = WeaponType.PICKAXE;
		}else if(((DiggerItem) (Object)this) instanceof ShovelItem) {
			type = WeaponType.SHOVEL;
		}else {
			type = WeaponType.HOE;
		}
		type.addCombatAttributes(this.getTier(), var3);
		defaultModifiers = var3.build();
	}
	/**
	 * @author Mojank
	 */
	@Overwrite
	public float getAttackDamage() {
		return type.getDamage(this.getTier());
	}
	@Redirect(method = "hurtEnemy",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
	public <T extends LivingEntity> void damage(ItemStack instance, int amount, T entity, Consumer<T> breakCallback) {
		decreaseDurability(instance, amount, entity, breakCallback);
	}
	public <T extends LivingEntity> void decreaseDurability(ItemStack instance, int amount, T entity, Consumer<T> breakCallback) {
		if (!entity.level.isClientSide && (!(entity instanceof Player) || !((Player)entity).getAbilities().invulnerable)) {
			if (instance.isDamageableItem() && allToolsAreWeapons) {
				if (instance.hurt(1, entity.getRandom(), entity instanceof ServerPlayer ? (ServerPlayer) entity : null)) {
					breakCallback.accept(entity);
					Item item = instance.getItem();
					instance.shrink(1);
					if (entity instanceof Player) {
						((Player) entity).awardStat(Stats.ITEM_BROKEN.get(item));
					}

					instance.setDamageValue(0);
				}
			}else if(instance.isDamageableItem()) {
				if((Object)this instanceof AxeItem || (Object) this instanceof HoeItem) {
					if (instance.hurt(1, entity.getRandom(), entity instanceof ServerPlayer ? (ServerPlayer) entity : null)) {
						breakCallback.accept(entity);
						Item item = instance.getItem();
						instance.shrink(1);
						if (entity instanceof Player) {
							((Player) entity).awardStat(Stats.ITEM_BROKEN.get(item));
						}

						instance.setDamageValue(0);
					}
				}else{
					if (instance.hurt(amount, entity.getRandom(), entity instanceof ServerPlayer ? (ServerPlayer) entity : null)) {
						breakCallback.accept(entity);
						Item item = instance.getItem();
						instance.shrink(1);
						if (entity instanceof Player) {
							((Player) entity).awardStat(Stats.ITEM_BROKEN.get(item));
						}

						instance.setDamageValue(0);
					}
				}
			}
		}
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
