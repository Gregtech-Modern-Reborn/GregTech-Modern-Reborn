package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualTank;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;

public class VirtualTankAccessor extends CustomObjectAccessor<VirtualTank> {

    public static final VirtualTankAccessor INSTANCE = new VirtualTankAccessor();

    protected VirtualTankAccessor() {
        super(VirtualTank.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp op, VirtualTank value, HolderLookup.Provider provider) {
        return NbtTagPayload.of(value.serializeNBT(provider));
    }

    @Override
    public VirtualTank deserialize(AccessorOp op, ITypedPayload<?> payload, HolderLookup.Provider provider) {
        var tank = new VirtualTank();
        tank.deserializeNBT(provider, (CompoundTag) payload.getPayload());
        return tank;
    }
}
