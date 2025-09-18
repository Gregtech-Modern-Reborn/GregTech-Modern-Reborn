package com.reborn.gtceu.api.machine.feature;

import com.reborn.gtceu.common.item.PortableScannerBehavior;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDataInfoProvider {

    @Nonnull
    List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode);

    @Nullable
    default List<Component> getDebugInfo(Player player, int logLevel, PortableScannerBehavior.DisplayMode mode) {
        return null;
    }
}
