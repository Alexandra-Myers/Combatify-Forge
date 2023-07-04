package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.extensions.IForgePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IForgePlayer.class)
public interface IForgePlayerMixin {
    @Shadow
    Player self();

    /**
     * @author Alexandra
     * @reason Remove creative reach buff
     */
    @Overwrite(remap = false)
    default double getEntityReach()
    {
        return ((PlayerExtensions)self()).getAttackRange(self(), 2.5);
    }

    /**
     * @author Alexandra
     * @reason Remove creative reach buff
     */
    @Overwrite(remap = false)
    default double getBlockReach()
    {
        return ((PlayerExtensions)self()).getReach(self(), 6);
    }
}
