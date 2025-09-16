package com.reborn.gtceu.api.capability;

import com.reborn.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import java.util.List;

public interface ICentralMonitor {

    List<MonitorGroup> getMonitorGroups();
}
