package com.reborn.gtceu.integration.top.provider;

import com.reborn.gtceu.GTCEu;
import com.reborn.gtceu.api.machine.MetaMachine;
import com.reborn.gtceu.common.machine.multiblock.primitive.PrimitivePumpMachine;
import com.reborn.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;

public class PrimitivePumpProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("primitive_pump_provider");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level,
                             BlockState blockState, IProbeHitData iProbeHitData) {
        if (MetaMachine.getMachine(level, iProbeHitData.getPos()) instanceof PrimitivePumpMachine pump) {
            IProbeInfo verticalPane = iProbeInfo.vertical(iProbeInfo.defaultLayoutStyle().spacing(0));
            verticalPane.text(Component.translatable("gtceu.top.primitive_pump_production",
                    FormattingUtil.formatNumbers(pump.getFluidProduction())));
        }
    }
}
