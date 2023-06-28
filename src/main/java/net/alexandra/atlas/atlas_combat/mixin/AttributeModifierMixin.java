package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.IAttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(AttributeModifier.class)
public class AttributeModifierMixin implements IAttributeModifier {
	@Shadow
	@Mutable
	@Final
	private double amount;

	@Shadow @Final private UUID id;

	@Override
	public void modifySpeed() {
		if (id == Item.BASE_ATTACK_SPEED_UUID) {
			if(amount >= 0) {
				amount = AtlasCombat.CONFIG.fastestToolAttackSpeed.get();
			} else if(amount >= -1) {
				amount = AtlasCombat.CONFIG.fastToolAttackSpeed.get();
			} else if(amount == -2) {
				amount = AtlasCombat.CONFIG.defaultAttackSpeed.get();
			} else if(amount >= -2.5) {
				amount = AtlasCombat.CONFIG.fastToolAttackSpeed.get();
			} else if(amount > -3) {
				amount = AtlasCombat.CONFIG.defaultAttackSpeed.get();
			} else if (amount > -3.5) {
				amount = AtlasCombat.CONFIG.slowToolAttackSpeed.get();
			} else {
				amount = AtlasCombat.CONFIG.slowestToolAttackSpeed.get();
			}
		}
	}
}