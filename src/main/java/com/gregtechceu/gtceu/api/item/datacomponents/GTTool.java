package com.gregtechceu.gtceu.api.item.datacomponents;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

import java.util.Optional;

public record GTTool(Optional<Integer> enchantability, int lastCraftingUse) {

    public static final GTTool EMPTY = new GTTool();

    public static final Codec<GTTool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("enchantability").forGetter(GTTool::enchantability),
            Codec.INT.lenientOptionalFieldOf("last_crafting_use", 0).forGetter(GTTool::lastCraftingUse))
            .apply(instance, GTTool::new));
    public static final StreamCodec<ByteBuf, GTTool> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), GTTool::enchantability,
            ByteBufCodecs.VAR_INT, GTTool::lastCraftingUse,
            GTTool::new);

    public GTTool() {
        this(Optional.empty(), 0);
    }

    public GTTool setEnchantability(int enchantability) {
        return new GTTool(Optional.of(enchantability), this.lastCraftingUse);
    }

    public GTTool setLastCraftingUse(int lastCraftingUse) {
        return new GTTool(this.enchantability, lastCraftingUse);
    }
}
