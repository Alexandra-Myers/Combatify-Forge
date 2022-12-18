package net.alexandra.atlas.atlas_combat.networking;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.ForgeConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AtlasCombat.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void init() {
        INSTANCE.messageBuilder(S2CServerConfigSyncPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CServerConfigSyncPacket::encode).decoder(S2CServerConfigSyncPacket::new)
                .consumerMainThread(S2CServerConfigSyncPacket::handle).add();
    }

    public static void handlePacket(Supplier<NetworkEvent.Context> ctx) {
        ForgeConfig config = AtlasCombat.CONFIG;
    }

}
