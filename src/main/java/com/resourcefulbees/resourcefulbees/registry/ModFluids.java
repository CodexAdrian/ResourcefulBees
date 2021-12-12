package com.resourcefulbees.resourcefulbees.registry;

import com.resourcefulbees.resourcefulbees.ResourcefulBees;
import com.resourcefulbees.resourcefulbees.lib.ModConstants;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Rarity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModFluids {

    private ModFluids() {
        throw new IllegalStateException(ModConstants.UTILITY_CLASS);
    }

    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, ResourcefulBees.MOD_ID);

    public static final ResourceLocation FLUID_STILL = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/honey_still");
    public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/honey_flow");
    public static final ResourceLocation FLUID_OVERLAY = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/honey_overlay");

    public static final ResourceLocation CATNIP_FLUID_STILL = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/catnip_honey_still");
    public static final ResourceLocation CATNIP_FLUID_FLOWING = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/catnip_honey_flow");
    public static final ResourceLocation CATNIP_FLUID_OVERLAY = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/catnip_honey_overlay");

    public static final ResourceLocation STARRY_FLUID_STILL = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/starry_honey_still");
    public static final ResourceLocation STARRY_FLUID_FLOWING = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/starry_honey_flow");
    public static final ResourceLocation STARRY_FLUID_OVERLAY = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/starry_honey_overlay");

    public static final ResourceLocation CUSTOM_FLUID_STILL = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/custom_honey_still");
    public static final ResourceLocation CUSTOM_FLUID_FLOWING = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/custom_honey_flow");
    public static final ResourceLocation CUSTOM_FLUID_OVERLAY = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/custom_honey_overlay");

    private static ForgeFlowingFluid.Properties makeProperties() {
        return new ForgeFlowingFluid.Properties(HONEY_STILL, HONEY_FLOWING,
                FluidAttributes.builder(FLUID_STILL, FLUID_FLOWING).sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY)
                        .overlay(FLUID_OVERLAY).density(1300).temperature(300).viscosity(1800).rarity(Rarity.COMMON))
                .bucket(ModItems.HONEY_FLUID_BUCKET).block(ModBlocks.HONEY_FLUID_BLOCK).tickRate(20);
    }

    private static ForgeFlowingFluid.Properties makeCatnipProperties() {
        return new ForgeFlowingFluid.Properties(CATNIP_HONEY_STILL, CATNIP_HONEY_FLOWING,
                FluidAttributes.builder(CATNIP_FLUID_STILL, CATNIP_FLUID_FLOWING).sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY)
                        .overlay(CATNIP_FLUID_OVERLAY).density(1300).temperature(300).viscosity(1800).rarity(Rarity.COMMON))
                .bucket(ModItems.CATNIP_HONEY_FLUID_BUCKET).block(ModBlocks.CATNIP_HONEY_FLUID_BLOCK).tickRate(20);
    }

    private static ForgeFlowingFluid.Properties makeStarryProperties() {
        return new ForgeFlowingFluid.Properties(STARRY_HONEY_STILL, STARRY_HONEY_FLOWING,
                FluidAttributes.builder(STARRY_FLUID_STILL, STARRY_FLUID_FLOWING).sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY)
                        .overlay(STARRY_FLUID_OVERLAY).density(1300).temperature(300).viscosity(1800).rarity(Rarity.COMMON))
                .bucket(ModItems.STARRY_HONEY_FLUID_BUCKET).block(ModBlocks.STARRY_HONEY_FLUID_BLOCK).tickRate(20);
    }


    public static final RegistryObject<FlowingFluid> HONEY_STILL = FLUIDS.register("honey", () ->
            new ForgeFlowingFluid.Source(makeProperties())
    );
    public static final RegistryObject<FlowingFluid> HONEY_FLOWING = FLUIDS.register("honey_flowing", () ->
            new ForgeFlowingFluid.Flowing(makeProperties())
    );

    public static final RegistryObject<FlowingFluid> CATNIP_HONEY_STILL = FLUIDS.register("catnip_honey", () ->
            new ForgeFlowingFluid.Source(makeCatnipProperties())
    );

    public static final RegistryObject<FlowingFluid> CATNIP_HONEY_FLOWING = FLUIDS.register("catnip_honey_flowing", () ->
            new ForgeFlowingFluid.Flowing(makeCatnipProperties())
    );

    public static final RegistryObject<FlowingFluid> STARRY_HONEY_STILL = FLUIDS.register("starry_honey", () ->
            new ForgeFlowingFluid.Source(makeStarryProperties())
    );

    public static final RegistryObject<FlowingFluid> STARRY_HONEY_FLOWING = FLUIDS.register("starry_honey_flowing", () ->
            new ForgeFlowingFluid.Flowing(makeStarryProperties())
    );
}
