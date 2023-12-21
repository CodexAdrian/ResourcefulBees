package com.teamresourceful.resourcefulbees.common.setup.data.honeydata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.resourcefulbees.api.data.BeekeeperTradeData;
import com.teamresourceful.resourcefulbees.api.data.honey.HoneyBlockData;
import com.teamresourceful.resourcefulbees.api.data.honey.base.HoneyDataSerializer;
import com.teamresourceful.resourcefulbees.common.setup.data.beedata.TradeData;
import com.teamresourceful.resourcefulbees.common.util.ModResourceLocation;
import com.teamresourceful.resourcefullib.common.codecs.recipes.LazyHolders;
import com.teamresourceful.resourcefullib.common.color.Color;
import com.teamresourceful.resourcefullib.common.item.LazyHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public record CustomHoneyBlockData(
        Color color,
        float jumpFactor,
        float speedFactor,
        LazyHolder<Item> blockItem,
        LazyHolder<Block> block,
        BeekeeperTradeData tradeData
) implements HoneyBlockData {

    private static final CustomHoneyBlockData DEFAULT = new CustomHoneyBlockData(Color.DEFAULT, 0.5f, 0.4f, LazyHolder.of(BuiltInRegistries.ITEM, Items.AIR), LazyHolder.of(BuiltInRegistries.BLOCK, Blocks.AIR), TradeData.DEFAULT);
    private static final Codec<HoneyBlockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Color.CODEC.optionalFieldOf("color", Color.DEFAULT).forGetter(HoneyBlockData::color),
            Codec.FLOAT.optionalFieldOf("jumpFactor", 0.5f).forGetter(HoneyBlockData::jumpFactor),
            Codec.FLOAT.optionalFieldOf("speedFactor", 0.4f).forGetter(HoneyBlockData::speedFactor),
            LazyHolders.LAZY_ITEM.optionalFieldOf("honeyBlockItem", LazyHolder.of(BuiltInRegistries.ITEM, Items.HONEY_BLOCK)).forGetter(HoneyBlockData::blockItem),
            LazyHolders.LAZY_BLOCK.optionalFieldOf("honeyBlock", LazyHolder.of(BuiltInRegistries.BLOCK, Blocks.HONEY_BLOCK)).forGetter(HoneyBlockData::block),
            TradeData.CODEC.optionalFieldOf("tradeData", TradeData.DEFAULT).forGetter(HoneyBlockData::tradeData)
    ).apply(instance, CustomHoneyBlockData::new));
    public static final HoneyDataSerializer<HoneyBlockData> SERIALIZER = HoneyDataSerializer.of(new ModResourceLocation("block"), 1, id -> CODEC, DEFAULT);

    @Override
    public HoneyDataSerializer<HoneyBlockData> serializer() {
        return SERIALIZER;
    }
}
