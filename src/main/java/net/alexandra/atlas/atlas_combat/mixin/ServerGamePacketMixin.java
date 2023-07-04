package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleInteract", at = @At(value = "HEAD"))
	public void injectPlayer(ServerboundInteractPacket packet, CallbackInfo ci) {
		AtlasCombat.player = player;
	}

	@Redirect(
			method = "handleUseItemOn",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;canInteractWith(Lnet/minecraft/core/BlockPos;D)Z",opcode = Opcodes.GETSTATIC))
	private boolean getActualReachDistance(ServerPlayer instance, BlockPos blockPos, double v) {
		return instance.canInteractWith(blockPos, 0);
	}
}
