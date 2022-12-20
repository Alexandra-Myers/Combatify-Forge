package net.alexandra.atlas.atlas_combat;

import net.alexandra.atlas.atlas_combat.config.ForgeConfig;
import net.alexandra.atlas.atlas_combat.networking.NetworkHandler;
import net.alexandra.atlas.atlas_combat.networking.S2CServerConfigSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;


@Mod(AtlasCombat.MODID)
@Mod.EventBusSubscriber(modid = AtlasCombat.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class AtlasCombat
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "atlascombat";

    public static Player player;

    public static ForgeConfig CONFIG;

    public AtlasCombat()
    {
        initConfig();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void initConfig() {
        CONFIG = new ForgeConfig();
    }

    @SubscribeEvent
    public void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer cr = (ServerPlayer) event.getEntity();
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> cr), new S2CServerConfigSyncPacket(CONFIG));
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        NetworkHandler.init();
        DispenserBlock.registerBehavior(Items.TRIDENT, new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level world, Position position, ItemStack stack) {
                ThrownTrident trident = new ThrownTrident(EntityType.TRIDENT, world);
                trident.tridentItem = stack.copy();
                trident.setPosRaw(position.x(), position.y(), position.z());
                trident.pickup = AbstractArrow.Pickup.ALLOWED;
                return trident;
            }
        });
    }
}
