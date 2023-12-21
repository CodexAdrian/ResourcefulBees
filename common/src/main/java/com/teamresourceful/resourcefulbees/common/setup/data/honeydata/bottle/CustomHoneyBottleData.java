package com.teamresourceful.resourcefulbees.common.setup.data.honeydata.bottle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.resourcefulbees.api.data.BeekeeperTradeData;
import com.teamresourceful.resourcefulbees.api.data.honey.base.HoneyDataSerializer;
import com.teamresourceful.resourcefulbees.api.data.honey.bottle.HoneyBottleData;
import com.teamresourceful.resourcefulbees.api.data.honey.bottle.HoneyFoodData;
import com.teamresourceful.resourcefulbees.common.setup.data.beedata.TradeData;
import com.teamresourceful.resourcefulbees.common.util.ModResourceLocation;
import com.teamresourceful.resourcefullib.common.codecs.EnumCodec;
import com.teamresourceful.resourcefullib.common.codecs.recipes.LazyHolders;
import com.teamresourceful.resourcefullib.common.color.Color;
import com.teamresourceful.resourcefullib.common.item.LazyHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

public record CustomHoneyBottleData(
        String id,
        Color color,
        HoneyFoodData food,
        Rarity rarity,
        LazyHolder<Item> bottle,
        BeekeeperTradeData tradeData
) implements HoneyBottleData {

    private static Codec<HoneyBottleData> codec(String id) {
        return RecordCodecBuilder.create(instance -> instance.group(
                RecordCodecBuilder.point(id),
                Color.CODEC.optionalFieldOf("color", Color.DEFAULT).forGetter(HoneyBottleData::color),
                CustomHoneyFoodData.CODEC.optionalFieldOf("food", CustomHoneyFoodData.DEFAULT).forGetter(HoneyBottleData::food),
                EnumCodec.of(Rarity.class).optionalFieldOf("rarity", Rarity.COMMON).forGetter(HoneyBottleData::rarity),
                LazyHolders.LAZY_ITEM.optionalFieldOf("honeyBottle", LazyHolder.of(BuiltInRegistries.ITEM, Items.HONEY_BOTTLE)).forGetter(HoneyBottleData::bottle),
                TradeData.CODEC.optionalFieldOf("tradeData", TradeData.DEFAULT).forGetter(HoneyBottleData::tradeData)
        ).apply(instance, CustomHoneyBottleData::new));
    }
    public static final HoneyDataSerializer<HoneyBottleData> SERIALIZER = HoneyDataSerializer.of(new ModResourceLocation("bottle"), 1, CustomHoneyBottleData::codec);

    @Override
    public HoneyDataSerializer<HoneyBottleData> serializer() {
        return SERIALIZER;
    }
}
