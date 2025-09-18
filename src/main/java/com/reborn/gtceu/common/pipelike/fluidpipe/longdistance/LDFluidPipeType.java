package com.reborn.gtceu.common.pipelike.fluidpipe.longdistance;

import com.reborn.gtceu.api.pipenet.longdistance.LongDistancePipeType;
import com.reborn.gtceu.config.ConfigHolder;

public class LDFluidPipeType extends LongDistancePipeType {

    public static final LDFluidPipeType INSTANCE = new LDFluidPipeType();

    private LDFluidPipeType() {
        super("fluid");
    }

    @Override
    public int getMinLength() {
        return ConfigHolder.INSTANCE.machines.ldFluidPipeMinDistance;
    }
}
