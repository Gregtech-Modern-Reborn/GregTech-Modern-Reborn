package com.reborn.gtceu.common.cover.detector;

import com.reborn.gtceu.api.capability.GTCapabilityHelper;
import com.reborn.gtceu.api.capability.ICoverable;
import com.reborn.gtceu.api.cover.CoverDefinition;
import com.reborn.gtceu.utils.RedstoneUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedActivityDetectorCover extends ActivityDetectorCover {

    public AdvancedActivityDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        var workable = GTCapabilityHelper.getWorkable(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
        if (workable == null || workable.getMaxProgress() == 0)
            return;

        int outputAmount = RedstoneUtil.computeRedstoneValue(workable.getProgress(), workable.getMaxProgress(),
                isInverted());

        // nonstandard logic for handling off state
        if (!workable.isWorkingEnabled() || !workable.isActive())
            outputAmount = 0;

        setRedstoneSignalOutput(outputAmount);
    }
}
