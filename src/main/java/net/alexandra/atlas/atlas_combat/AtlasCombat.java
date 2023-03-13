package net.alexandra.atlas.atlas_combat;

import com.google.common.collect.Sets;
import net.alexandra.atlas.atlas_combat.config.ForgeConfig;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.networking.NetworkHandler;
import net.alexandra.atlas.atlas_combat.networking.S2CServerConfigSyncPacket;
import net.alexandra.atlas.atlas_combat.util.DummyAttackDamageMobEffect;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Mod(AtlasCombat.MODID)
@Mod.EventBusSubscriber(modid = AtlasCombat.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class AtlasCombat
{
    public static final DeferredRegister<MobEffect> VANILLA_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "minecraft");

    public static final RegistryObject<MobEffect> DAMAGE_BOOST = registerEffect("strength", () -> new DummyAttackDamageMobEffect(MobEffectCategory.BENEFICIAL, 9643043, 0.2)
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> WEAKNESS = registerEffect("weakness", () -> new DummyAttackDamageMobEffect(MobEffectCategory.HARMFUL, 4738376, -0.2)
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final DeferredRegister<Attribute> VANILLA_ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, "minecraft");

    public static final RegistryObject<Attribute> ATTACK_SPEED = registerAttribute("generic.attack_speed", () -> (new RangedAttribute("attribute.name.generic.attack_speed", 4.0, 0.10000000149011612, 1024.0)).setSyncable(true));

    private static <T extends MobEffect> RegistryObject<T> registerEffect(String name, Supplier<T> effect) {
        RegistryObject<T> toReturn = VANILLA_EFFECTS.register(name, effect);
        return toReturn;
    }
    private static <T extends Attribute> RegistryObject<T> registerAttribute(String name, Supplier<T> attribute) {
        RegistryObject<T> toReturn = VANILLA_ATTRIBUTES.register(name, attribute);
        return toReturn;
    }
    // Define mod id in a common place for everything to reference
    public static final String MODID = "atlascombat";

    public static Player player;

    public static ForgeConfig CONFIG;

    public AtlasCombat()
    {
        AtlasCombat.initConfig();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        VANILLA_EFFECTS.register(bus);
    }
    public static final Set<ToolAction> DEFAULT_ITEM_ACTIONS = of(ToolActions.SWORD_SWEEP);

    private static Set<ToolAction> of(ToolAction... actions) {
        return Stream.of(actions).collect(Collectors.toCollection(Sets::newIdentityHashSet));
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
        event.enqueueWork(() -> {
            ToolActions.DEFAULT_SWORD_ACTIONS.remove(ToolActions.SWORD_SWEEP);
            ToolActions.DEFAULT_SWORD_ACTIONS.add(ToolActions.SHIELD_BLOCK);
            if(ModList.get().isLoaded("spammycombat"))
                AtlasCombat.DEFAULT_ITEM_ACTIONS.remove(ToolActions.SWORD_SWEEP);
        });
        List<Map.Entry<ResourceKey<Item>, Item>> entries = ForgeRegistries.ITEMS.getEntries().stream().toList();
        List<Item> items = new ArrayList<>();
        for (Map.Entry<ResourceKey<Item>, Item> entry : entries) {
            items.add(entry.getValue());
        }

        for(Item item : items) {
            if(item == Items.SNOWBALL || item == Items.EGG) {
                ((ItemExtensions) item).setStackSize(64);
            } else if(item == Items.POTION) {
                ((ItemExtensions) item).setStackSize(16);
            }
            ((ItemExtensions) item).changeDefaultModifiers();
        }
    }
}
