package com.reborn.gtceu.common.pipelike.cable;

import com.reborn.gtceu.api.data.chemical.material.properties.WireProperties;
import com.reborn.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class LevelEnergyNet extends LevelPipeNet<WireProperties, EnergyNet> {

    public static LevelEnergyNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new LevelEnergyNet(serverLevel, tag),
                () -> new LevelEnergyNet(serverLevel), "gtcue_energy_net");
    }

    public LevelEnergyNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelEnergyNet(ServerLevel serverLevel, CompoundTag tag) {
        super(serverLevel, tag);
    }

    @Override
    protected EnergyNet createNetInstance() {
        return new EnergyNet(this);
    }
}
