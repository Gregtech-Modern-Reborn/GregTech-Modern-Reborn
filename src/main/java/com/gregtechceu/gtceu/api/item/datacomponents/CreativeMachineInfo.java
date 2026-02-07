package com.gregtechceu.gtceu.api.item.datacomponents;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

public record CreativeMachineInfo(int outputPerCycle, int ticksPerCycle) {

    // spotless:off
    public static final Codec<CreativeMachineInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.orElse(1).fieldOf("output_per_cycle").forGetter(CreativeMachineInfo::outputPerCycle),
            ExtraCodecs.NON_NEGATIVE_INT.orElse(1).fieldOf("ticks_per_cycle").forGetter(CreativeMachineInfo::ticksPerCycle)
    ).apply(instance, CreativeMachineInfo::new));
    // spotless:on
    public static final StreamCodec<ByteBuf, CreativeMachineInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CreativeMachineInfo::outputPerCycle,
            ByteBufCodecs.VAR_INT, CreativeMachineInfo::ticksPerCycle,
            CreativeMachineInfo::new);
}
