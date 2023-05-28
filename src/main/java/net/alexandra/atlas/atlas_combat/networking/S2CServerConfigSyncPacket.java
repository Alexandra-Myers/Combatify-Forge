package net.alexandra.atlas.atlas_combat.networking;

import net.alexandra.atlas.atlas_combat.AtlasClient;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.ForgeConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class S2CServerConfigSyncPacket{

    private ForgeConfig config;

    public S2CServerConfigSyncPacket(ForgeConfig config) {
        this.config = config;
    }

    public S2CServerConfigSyncPacket(FriendlyByteBuf byteBuf) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ForgeConfig client = AtlasCombat.CONFIG;
            client.autoAttackAllowed.set(byteBuf.readBoolean());
            client.attackReach.set(byteBuf.readBoolean());
            client.attackSpeed.set(byteBuf.readBoolean());
            client.axeReachBuff.set(byteBuf.readBoolean());
            client.blockReach.set(byteBuf.readBoolean());
            client.bedrockBlockReach.set(byteBuf.readBoolean());
            client.ctsAttackBalancing.set(byteBuf.readBoolean());
            client.eatingInterruption.set(byteBuf.readBoolean());
            client.refinedCoyoteTime.set(byteBuf.readBoolean());
            client.midairKB.set(byteBuf.readBoolean());
            client.fishingHookKB.set(byteBuf.readBoolean());
            client.fistDamage.set(byteBuf.readBoolean());
            client.saturationHealing.set(byteBuf.readBoolean());
            client.swordBlocking.set(byteBuf.readBoolean());
            client.toolsAreWeapons.set(byteBuf.readBoolean());
            client.potionUseDuration.set(byteBuf.readInt());
            client.honeyBottleUseDuration.set(byteBuf.readInt());
            client.milkBucketUseDuration.set(byteBuf.readInt());
            client.stewUseDuration.set(byteBuf.readInt());
            client.instantHealthBonus.set(byteBuf.readInt());
            client.eggItemCooldown.set(byteBuf.readInt());
            client.snowballItemCooldown.set(byteBuf.readInt());
            client.snowballDamage.set(byteBuf.readDouble());
            client.eggDamage.set(byteBuf.readDouble());
            client.bowUncertainty.set(byteBuf.readDouble());
            client.swordAttackDamageBonus.set(byteBuf.readDouble());
            client.axeAttackDamageBonus.set(byteBuf.readDouble());
            client.tridentAttackDamageBonus.set(byteBuf.readDouble());
            client.baseHoeAttackDamageBonus.set(byteBuf.readDouble());
            client.ironDiaHoeAttackDamageBonus.set(byteBuf.readDouble());
            client.netheriteHoeAttackDamageBonus.set(byteBuf.readDouble());
            client.swordAttackSpeed.set(byteBuf.readDouble());
            client.axeAttackSpeed.set(byteBuf.readDouble());
            client.tridentAttackSpeed.set(byteBuf.readDouble());
            client.woodenHoeAttackSpeed.set(byteBuf.readDouble());
            client.stoneHoeAttackSpeed.set(byteBuf.readDouble());
            client.ironHoeAttackSpeed.set(byteBuf.readDouble());
            client.goldDiaNethHoeAttackSpeed.set(byteBuf.readDouble());
            client.defaultAttackSpeed.set(byteBuf.readDouble());
            client.slowestToolAttackSpeed.set(byteBuf.readDouble());
            client.slowToolAttackSpeed.set(byteBuf.readDouble());
            client.fastToolAttackSpeed.set(byteBuf.readDouble());
            client.fastestToolAttackSpeed.set(byteBuf.readDouble());
        });
    }

    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeBoolean(config.autoAttackAllowed.get());
        byteBuf.writeBoolean(config.attackReach.get());
        byteBuf.writeBoolean(config.attackSpeed.get());
        byteBuf.writeBoolean(config.axeReachBuff.get());
        byteBuf.writeBoolean(config.blockReach.get());
        byteBuf.writeBoolean(config.bedrockBlockReach.get());
        byteBuf.writeBoolean(config.ctsAttackBalancing.get());
        byteBuf.writeBoolean(config.eatingInterruption.get());
        byteBuf.writeBoolean(config.refinedCoyoteTime.get());
        byteBuf.writeBoolean(config.midairKB.get());
        byteBuf.writeBoolean(config.fishingHookKB.get());
        byteBuf.writeBoolean(config.fistDamage.get());
        byteBuf.writeBoolean(config.saturationHealing.get());
        byteBuf.writeBoolean(config.swordBlocking.get());
        byteBuf.writeBoolean(config.toolsAreWeapons.get());
        byteBuf.writeInt(config.potionUseDuration.get());
        byteBuf.writeInt(config.honeyBottleUseDuration.get());
        byteBuf.writeInt(config.milkBucketUseDuration.get());
        byteBuf.writeInt(config.stewUseDuration.get());
        byteBuf.writeInt(config.instantHealthBonus.get());
        byteBuf.writeInt(config.eggItemCooldown.get());
        byteBuf.writeInt(config.snowballItemCooldown.get());
        byteBuf.writeDouble(config.snowballDamage.get());
        byteBuf.writeDouble(config.eggDamage.get());
        byteBuf.writeDouble(config.bowUncertainty.get());
        byteBuf.writeDouble(config.swordAttackDamageBonus.get());
        byteBuf.writeDouble(config.axeAttackDamageBonus.get());
        byteBuf.writeDouble(config.tridentAttackDamageBonus.get());
        byteBuf.writeDouble(config.baseHoeAttackDamageBonus.get());
        byteBuf.writeDouble(config.ironDiaHoeAttackDamageBonus.get());
        byteBuf.writeDouble(config.netheriteHoeAttackDamageBonus.get());
        byteBuf.writeDouble(config.swordAttackSpeed.get());
        byteBuf.writeDouble(config.axeAttackSpeed.get());
        byteBuf.writeDouble(config.tridentAttackSpeed.get());
        byteBuf.writeDouble(config.woodenHoeAttackSpeed.get());
        byteBuf.writeDouble(config.stoneHoeAttackSpeed.get());
        byteBuf.writeDouble(config.ironHoeAttackSpeed.get());
        byteBuf.writeDouble(config.goldDiaNethHoeAttackSpeed.get());
        byteBuf.writeDouble(config.defaultAttackSpeed.get());
        byteBuf.writeDouble(config.slowestToolAttackSpeed.get());
        byteBuf.writeDouble(config.slowToolAttackSpeed.get());
        byteBuf.writeDouble(config.fastToolAttackSpeed.get());
        byteBuf.writeDouble(config.fastestToolAttackSpeed.get());
    }


    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                // Make sure it's only executed on the physical client
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> NetworkHandler.handlePacket(ctx))
        );
        ctx.get().setPacketHandled(true);
    }
}
