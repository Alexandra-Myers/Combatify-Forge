package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;

import java.util.function.Function;

public interface IActionType {
    ServerboundInteractPacket.ActionType completeCreate(String name, Function<FriendlyByteBuf, ServerboundInteractPacket.Action> p_179636_);
}
