package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.alexandra.atlas.atlas_combat.extensions.IPlayerGameMode;
import net.alexandra.atlas.atlas_combat.extensions.IServerboundInteractPacket;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.alexandra.atlas.atlas_combat.networking.UpdatedServerboundInteractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin implements IPlayerGameMode {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	protected abstract void ensureHasSentCarriedItem();

	@Shadow
	@Final
	private ClientPacketListener connection;

	@Shadow
	private GameType localPlayerMode;

	@Shadow public abstract GameType getPlayerMode();

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public boolean hasFarPickRange() {
		return false;
	}

	@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"))
	public void redirectReset(Player instance) {
		((PlayerExtensions)instance).resetAttackStrengthTicker(true);
	}
	@Redirect(method = "stopDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
	public void redirectReset2(LocalPlayer instance) {
		if(getPlayerMode() == GameType.ADVENTURE) {
			return;
		}
		((PlayerExtensions)instance).resetAttackStrengthTicker(true);
	}

	@Override
	public void swingInAir(Player player) {
		ensureHasSentCarriedItem();
		connection.send(UpdatedServerboundInteractPacket.createMissPacket(player.getId(), player.isShiftKeyDown()));
		if (localPlayerMode != GameType.SPECTATOR) {
			((PlayerExtensions)player).attackAir();
			((PlayerExtensions)player).resetAttackStrengthTicker(false);
		}
	}
}
