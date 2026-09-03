package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.utils.RedstoneUtil;

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
        if (workable == null)
            return;

        // nonstandard logic for handling off state: an idle machine reports no progress at all, so the
        // signal has to be cleared here instead of keeping the last value of the finished recipe
        int maxProgress = workable.getMaxProgress();
        if (maxProgress <= 0 || !workable.isWorkingEnabled() || !workable.isActive()) {
            setRedstoneSignalOutput(0);
            return;
        }

        setRedstoneSignalOutput(RedstoneUtil.computeRedstoneValue(workable.getProgress(), maxProgress, isInverted()));
    }
}
