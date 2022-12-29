package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.extensions.IForgePlayer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ForgeMod.class)
public class ForgeModMixin {
    @Shadow @Final private static DeferredRegister<Attribute> ATTRIBUTES;
    @Shadow
    @Mutable
    @Final
    public static RegistryObject<Attribute> REACH_DISTANCE = ATTRIBUTES.register("block_reach", () -> new RangedAttribute("attribute.name.generic.block_reach", 0.0, -1024.0, 1024.0).setSyncable(true));

    @Shadow
    @Mutable
    @Final
    public static RegistryObject<Attribute> ATTACK_RANGE = ATTRIBUTES.register("attack_reach", () -> new RangedAttribute("attribute.name.generic.attack_reach", 0.0, -1024.0, 1024.0).setSyncable(true));
}
