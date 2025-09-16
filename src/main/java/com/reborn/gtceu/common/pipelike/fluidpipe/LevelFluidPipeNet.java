package com.reborn.gtceu.common.pipelike.fluidpipe;

import com.reborn.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.reborn.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class LevelFluidPipeNet extends LevelPipeNet<FluidPipeProperties, FluidPipeNet> {

    public static LevelFluidPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new LevelFluidPipeNet(serverLevel, tag),
                () -> new LevelFluidPipeNet(serverLevel), "gtcue_fluid_pipe_net");
    }

    public LevelFluidPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelFluidPipeNet(ServerLevel serverLevel, CompoundTag tag) {
        super(serverLevel, tag);
    }

    @Override
    protected FluidPipeNet createNetInstance() {
        return new FluidPipeNet(this);
    }
}
