package net.alexandra.atlas.atlas_combat;

import com.chocohead.mm.api.ClassTinkerers;
import net.alexandra.atlas.atlas_combat.networking.UpdatedServerboundInteractPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;

import java.util.function.Function;

public class AtlasEarlyRiser implements Runnable {
    @Override
    public void run() {
        Function<FriendlyByteBuf, ServerboundInteractPacket.Action> buf = ignored -> UpdatedServerboundInteractPacket.MISS_ATTACK_ACTION;

        ClassTinkerers.enumBuilder("ActionType", "Ljava/util/function/Function;").addEnum("MISS_ATTACK", () -> new Object[]{buf}).build();
    }
}
