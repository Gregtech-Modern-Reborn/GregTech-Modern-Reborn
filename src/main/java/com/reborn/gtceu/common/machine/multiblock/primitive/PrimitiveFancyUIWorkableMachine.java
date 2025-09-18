package com.reborn.gtceu.common.machine.multiblock.primitive;

import com.reborn.gtceu.api.machine.IMachineBlockEntity;
import com.reborn.gtceu.api.machine.feature.IFancyUIMachine;

public class PrimitiveFancyUIWorkableMachine extends PrimitiveWorkableMachine implements IFancyUIMachine {

    public PrimitiveFancyUIWorkableMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }
}
