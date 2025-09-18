package com.reborn.gtceu.common.machine.multiblock.part.hpca;

import com.reborn.gtceu.api.GTValues;
import com.reborn.gtceu.api.gui.GuiTextures;
import com.reborn.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCABridgePartMachine extends HPCAComponentPartMachine {

    public HPCABridgePartMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    @Override
    public boolean doesAllowBridging() {
        return true;
    }

    @Override
    public ResourceTexture getComponentIcon() {
        return GuiTextures.HPCA_ICON_BRIDGE_COMPONENT;
    }

    @Override
    public int getUpkeepEUt() {
        return GTValues.VA[GTValues.IV];
    }

    @Override
    public boolean canBeDamaged() {
        return false;
    }
}
