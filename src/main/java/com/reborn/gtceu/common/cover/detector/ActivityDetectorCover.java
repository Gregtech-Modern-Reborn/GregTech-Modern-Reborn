package com.reborn.gtceu.common.cover.detector;

import com.reborn.gtceu.api.capability.GTCapabilityHelper;
import com.reborn.gtceu.api.capability.ICoverable;
import com.reborn.gtceu.api.cover.CoverDefinition;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ActivityDetectorCover extends DetectorCover {

    public ActivityDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() &&
                GTCapabilityHelper.getWorkable(coverHolder.getLevel(), coverHolder.getPos(), attachedSide) != null;
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0) {
            return;
        }

        var workable = GTCapabilityHelper.getWorkable(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);

        boolean isCurrentlyWorking = workable.isActive() && workable.isWorkingEnabled();

        setRedstoneSignalOutput(isCurrentlyWorking != isInverted() ? 15 : 0);
    }
}
