package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.IActionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Function;

@Mixin(ServerboundInteractPacket.ActionType.class)
public class ActionTypeMixin implements IActionType, IExtensibleEnum {
    private static ServerboundInteractPacket.ActionType create(String name, Function<FriendlyByteBuf, ServerboundInteractPacket.Action> p_179636_) {
        throw new IllegalStateException("Enum not extended");
    }
    @Override
    public ServerboundInteractPacket.ActionType completeCreate(String name, Function<FriendlyByteBuf, ServerboundInteractPacket.Action> p_179636_) {
        return create(name, p_179636_);
    }
}
