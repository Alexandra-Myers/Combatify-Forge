package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.*;
import net.alexandra.atlas.atlas_combat.util.BlockingType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandMixin implements IItemInHandRenderer {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Unique
	private HumanoidArm humanoidArm;
	@Unique
	private ItemStack itemStack;
	@Unique
	private float f;

	@Shadow
	protected abstract void applyItemArmTransform(PoseStack matrices, HumanoidArm arm, float equipProgress);

	@Shadow
	public abstract void renderItem(LivingEntity entity, ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light);


	@Inject(method = "renderArmWithItem", at = @At(value = "HEAD"), cancellable = true)
	private void renderArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
		this.itemStack = itemStack;
		HumanoidArm humanoidArm = interactionHand == InteractionHand.MAIN_HAND
				? abstractClientPlayer.getMainArm()
				: abstractClientPlayer.getMainArm().getOpposite();
		LivingEntityExtensions livingEntityExtensions = ((LivingEntityExtensions)abstractClientPlayer);
		this.humanoidArm = humanoidArm;
		if (AtlasCombat.CONFIG.swordBlocking.get()) {
			if (abstractClientPlayer.getUsedItemHand() == interactionHand && livingEntityExtensions.getBlockingItem().getItem() instanceof IShieldItem shieldItem && shieldItem.getBlockingType().isToolBlocker()) {
				poseStack.pushPose();
				applyItemArmTransform(poseStack, humanoidArm, i);
				applyItemBlockTransform2(poseStack, humanoidArm);
				boolean isRightHand = humanoidArm == HumanoidArm.RIGHT;
				renderItem(abstractClientPlayer, itemStack, isRightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !isRightHand, poseStack, multiBufferSource, j);

				poseStack.popPose();
				ci.cancel();
			}
		}
	}
	@Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0))
	private void injectFishing(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
		int q = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
		if(((IOptions) minecraft.options).fishingRodLegacy().get() && itemStack.getItem() instanceof FishingRodItem || itemStack.getItem() instanceof FoodOnAStickItem<?>) {
			poseStack.translate(q * 0.08f, 0.1f, -0.33f);
			poseStack.scale(0.95f, 1f, 1f);
		} else if(((IOptions) minecraft.options).fishingRodLegacy().get()) {
			poseStack.scale(0.95f, 1f, 1f);
			poseStack.mulPose(Axis.YP.rotationDegrees(q * 0.5F));
		}
	}
	@Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void modifyBowCode(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci, boolean bl, HumanoidArm humanoidArm, boolean bl2, int q) {
		this.f = f;
	}
	@Redirect(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 5))
	private void modifyBowCode(PoseStack instance, float x, float y, float z) {
		int q = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
		instance.translate(q * -0.2785682, 0.18344387412071228, 0.15731531381607056);
	}
	@Redirect(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 6))
	private void modifyBowCode1(PoseStack instance, float x, float y, float z) {
		float r = (float)itemStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
		double l = r / 20.0;
		l = (l * l + l * 2.0) / 3.0;
		if (l > 1.0) {
			l = 1.0;
		}
		float m = Mth.sin((float) ((r - 0.1) * 1.3));
		Item item = itemStack.getItem();
		double n = (item instanceof IBowItem ? ((IBowItem)item).getFatigueForTime((int) r) : l) - 0.1;
		double o = m * n;
		instance.translate(o * 0.0F, o * 0.004F, o * 0.0F);
	}

	@ModifyVariable(method = "tick", slice = @Slice(
			from = @At(value = "JUMP", ordinal = 3)
	), at = @At(value = "FIELD", ordinal = 0))
	public float modifyArmHeight(float f) {
		f *= 0.5;
		f = f * f * f * 0.25F + 0.75F;
		double offset = 0.0F;
		return (float)((double)f * (1.0 - offset) + offset);
	}

	@Inject(method = "applyItemArmTransform", at = @At(value = "HEAD"), cancellable = true)
	public void injectSwordBlocking(PoseStack matrices, HumanoidArm arm, float equipProgress, CallbackInfo ci) {
		if(((LivingEntityExtensions)minecraft.player).getBlockingItem().getItem() instanceof IShieldItem shieldItem && shieldItem.getBlockingType().isToolBlocker()) {
			int i = arm == HumanoidArm.RIGHT ? 1 : -1;
			matrices.translate(((float)i * 0.56F), (-0.52F + 0.0 * -0.6F), -0.72F);
			ci.cancel();
		}
	}
	@Override
	public void applyItemBlockTransform2(PoseStack poseStack, HumanoidArm humanoidArm) {
		int reverse = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
		poseStack.translate(reverse * -0.14142136F, 0.08F, 0.14142136F);
		poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
		poseStack.mulPose(Axis.YP.rotationDegrees((float)reverse * 13.365F));
		poseStack.mulPose(Axis.ZP.rotationDegrees((float)reverse * 78.05F));
	}
}
