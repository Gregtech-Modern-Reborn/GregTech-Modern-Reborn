package com.reborn.gtceu.api.machine.multiblock.part;

import com.reborn.gtceu.api.machine.IMachineBlockEntity;
import com.reborn.gtceu.api.machine.feature.ITieredMachine;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TieredPartMachine extends MultiblockPartMachine implements ITieredMachine {

    @Getter
    protected final int tier;

    public TieredPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }
}
