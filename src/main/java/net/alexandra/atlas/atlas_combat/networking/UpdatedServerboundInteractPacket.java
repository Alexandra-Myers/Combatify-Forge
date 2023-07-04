package net.alexandra.atlas.atlas_combat.networking;

import net.alexandra.atlas.atlas_combat.extensions.IHandler;
import net.alexandra.atlas.atlas_combat.extensions.IServerboundInteractPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.jetbrains.annotations.NotNull;

public class UpdatedServerboundInteractPacket extends ServerboundInteractPacket implements IServerboundInteractPacket {
    public static final ServerboundInteractPacket.Action MISS_ATTACK_ACTION = new ServerboundInteractPacket.Action() {

        @Override
        public ServerboundInteractPacket.@NotNull ActionType getType() {
            return ActionType.ATTACK;
        }

        @Override
        public void dispatch(ServerboundInteractPacket.Handler handler) {
            ((IHandler) handler).onMissAttack();
        }

        @Override
        public void write(FriendlyByteBuf buf) {
        }
    };

    public UpdatedServerboundInteractPacket(int i, boolean bl, ServerboundInteractPacket.Action action) {
        super(i, bl, action);
    }

    public static ServerboundInteractPacket createMissPacket(int i, boolean bl) {
        return new ServerboundInteractPacket(i, bl, MISS_ATTACK_ACTION);
    }
}
