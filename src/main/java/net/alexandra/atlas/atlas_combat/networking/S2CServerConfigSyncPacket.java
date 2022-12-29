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
            client.attackReach.set(byteBuf.readBoolean());
            client.bedrockBlockReach.set(byteBuf.readBoolean());
            client.refinedCoyoteTime.set(byteBuf.readBoolean());
            client.swordBlocking.set(byteBuf.readBoolean());
            client.potionUseDuration.set(byteBuf.readInt());
            client.honeyBottleUseDuration.set(byteBuf.readInt());
            client.milkBucketUseDuration.set(byteBuf.readInt());
            client.instantHealthBonus.set(byteBuf.readInt());
            client.eggItemCooldown.set(byteBuf.readInt());
            client.snowballItemCooldown.set(byteBuf.readInt());
            client.snowballDamage.set(byteBuf.readDouble());
            client.bowUncertainty.set(byteBuf.readDouble());
        });
    }

    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeBoolean(config.attackReach.get());
        byteBuf.writeBoolean(config.bedrockBlockReach.get());
        byteBuf.writeBoolean(config.refinedCoyoteTime.get());
        byteBuf.writeBoolean(config.swordBlocking.get());
        byteBuf.writeInt(config.potionUseDuration.get());
        byteBuf.writeInt(config.honeyBottleUseDuration.get());
        byteBuf.writeInt(config.milkBucketUseDuration.get());
        byteBuf.writeInt(config.instantHealthBonus.get());
        byteBuf.writeInt(config.eggItemCooldown.get());
        byteBuf.writeInt(config.snowballItemCooldown.get());
        byteBuf.writeDouble(config.snowballDamage.get());
        byteBuf.writeDouble(config.bowUncertainty.get());
    }


    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                // Make sure it's only executed on the physical client
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> NetworkHandler.handlePacket(ctx))
        );
        ctx.get().setPacketHandled(true);
    }
}
