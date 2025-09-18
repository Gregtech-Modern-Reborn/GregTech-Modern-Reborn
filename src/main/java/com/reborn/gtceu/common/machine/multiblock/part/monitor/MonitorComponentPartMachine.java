package com.reborn.gtceu.common.machine.multiblock.part.monitor;

import com.reborn.gtceu.api.capability.IMonitorComponent;
import com.reborn.gtceu.api.machine.IMachineBlockEntity;
import com.reborn.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

public abstract class MonitorComponentPartMachine extends MultiblockPartMachine implements IMonitorComponent {

    public MonitorComponentPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }
}
