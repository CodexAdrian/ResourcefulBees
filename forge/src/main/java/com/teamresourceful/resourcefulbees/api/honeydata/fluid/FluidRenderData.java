package com.teamresourceful.resourcefulbees.api.honeydata.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.resourcefulbees.ResourcefulBees;
import com.teamresourceful.resourcefullib.common.color.Color;
import net.minecraft.resources.ResourceLocation;

public record FluidRenderData(Color color, ResourceLocation still, ResourceLocation flowing, ResourceLocation overlay, ResourceLocation underwater) {

    private static final ResourceLocation CUSTOM_FLUID_STILL = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/custom_honey_still");
    private static final ResourceLocation CUSTOM_FLUID_FLOWING = new ResourceLocation(ResourcefulBees.MOD_ID, "block/honey/custom_honey_flow");
    private static final ResourceLocation CUSTOM_FLUID_UNDERWATER = new ResourceLocation(ResourcefulBees.MOD_ID, "textures/block/honey/custom_honey_underwater.png");

    public static final FluidRenderData DEFAULT = new FluidRenderData(Color.DEFAULT, CUSTOM_FLUID_STILL, CUSTOM_FLUID_FLOWING, CUSTOM_FLUID_FLOWING, CUSTOM_FLUID_UNDERWATER);

    public static final Codec<FluidRenderData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Color.CODEC.fieldOf("color").orElse(Color.DEFAULT).forGetter(FluidRenderData::color),
            ResourceLocation.CODEC.fieldOf("stillTexture").orElse(CUSTOM_FLUID_STILL).forGetter(FluidRenderData::still),
            ResourceLocation.CODEC.fieldOf("flowingTexture").orElse(CUSTOM_FLUID_FLOWING).forGetter(FluidRenderData::flowing),
            ResourceLocation.CODEC.fieldOf("overlayTexture").orElse(CUSTOM_FLUID_FLOWING).forGetter(FluidRenderData::overlay),
            ResourceLocation.CODEC.fieldOf("underwaterTexture").orElse(CUSTOM_FLUID_FLOWING).forGetter(FluidRenderData::underwater)
    ).apply(instance, FluidRenderData::new));
}
