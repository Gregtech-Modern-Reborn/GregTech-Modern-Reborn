package com.reborn.gtceu.api.item;

import com.reborn.gtceu.api.block.IMachineBlock;
import com.reborn.gtceu.api.data.chemical.material.Material;
import com.reborn.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.reborn.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.reborn.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;
import com.reborn.gtceu.common.data.GTMaterials;
import com.reborn.gtceu.common.data.machines.GTMachineUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DrumMachineItem extends MetaMachineItem {

    @NotNull
    private Material mat = GTMaterials.NULL;

    protected DrumMachineItem(IMachineBlock block, Properties properties, @NotNull Material mat) {
        super(block, properties);
        this.mat = mat;
    }

    public static DrumMachineItem create(IMachineBlock block, Properties properties, @NotNull Material mat) {
        return new DrumMachineItem(block, properties, mat);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        final FluidPipeProperties property = mat.getProperty(PropertyKey.FLUID_PIPE);
        if (property == null) {
            return null;
        }
        return new ThermalFluidHandlerItemStack(stack,
                GTMachineUtils.DRUM_CAPACITY.getInt(getDefinition()),
                property.getMaxFluidTemperature(), property.isGasProof(), property.isAcidProof(),
                property.isCryoProof(), property.isPlasmaProof());
    }
}
