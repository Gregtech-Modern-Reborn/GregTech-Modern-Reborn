package com.reborn.gtceu.api.machine.steam;

import com.reborn.gtceu.api.machine.IMachineBlockEntity;
import com.reborn.gtceu.api.machine.MetaMachine;
import com.reborn.gtceu.api.machine.feature.ITieredMachine;
import com.reborn.gtceu.api.machine.property.GTMachineModelProperties;
import com.reborn.gtceu.api.machine.trait.NotifiableFluidTank;
import com.reborn.gtceu.common.data.GTMaterials;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SteamMachine extends MetaMachine implements ITieredMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(SteamMachine.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    public static final BooleanProperty STEEL_PROPERTY = GTMachineModelProperties.IS_STEEL_MACHINE;

    @Getter
    public final boolean isHighPressure;
    @Persisted
    public final NotifiableFluidTank steamTank;

    public SteamMachine(IMachineBlockEntity holder, boolean isHighPressure, Object... args) {
        super(holder);
        this.isHighPressure = isHighPressure;
        this.steamTank = createSteamTank(args);
        this.steamTank.setFilter(fluidStack -> fluidStack.getFluid().is(GTMaterials.Steam.getFluidTag()));
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public int getTier() {
        return isHighPressure ? 1 : 0;
    }

    protected abstract NotifiableFluidTank createSteamTank(Object... args);
}
