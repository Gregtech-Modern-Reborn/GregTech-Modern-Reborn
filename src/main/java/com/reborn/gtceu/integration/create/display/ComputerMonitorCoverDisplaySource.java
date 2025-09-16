package com.reborn.gtceu.integration.create.display;

import com.reborn.gtceu.api.capability.GTCapabilityHelper;
import com.reborn.gtceu.api.capability.ICoverable;
import com.reborn.gtceu.common.cover.ComputerMonitorCover;
import com.reborn.gtceu.utils.GTStringUtils;

import net.minecraft.network.chat.MutableComponent;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;

import java.util.List;

public class ComputerMonitorCoverDisplaySource extends DisplaySource {

    private int refreshTicks = 100;

    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        ICoverable coverable = GTCapabilityHelper.getCoverable(context.level(), context.getSourcePos(),
                context.blockEntity().getDirection().getOpposite());
        if (coverable != null) {
            if (coverable.getCoverAtSide(
                    context.blockEntity().getDirection().getOpposite()) instanceof ComputerMonitorCover cover) {
                refreshTicks = cover.getUpdateInterval();
                return cover.getText();
            }
        }
        return GTStringUtils.literalLine("No cover!");
    }

    @Override
    public int getPassiveRefreshTicks() {
        return refreshTicks;
    }
}
