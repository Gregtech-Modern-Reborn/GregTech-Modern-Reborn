package com.gregtechceu.gtceu.api.misc.forge;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class FilteredFluidHandlerItemStackSimple extends FluidHandlerItemStackSimple {

    protected final Predicate<FluidStack> filter;

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     * @param capacity  The maximum capacity of this fluid tank.
     */
    public FilteredFluidHandlerItemStackSimple(Supplier<DataComponentType<SimpleFluidContent>> componentType,
                                               @NotNull ItemStack container, int capacity,
                                               Predicate<FluidStack> filter) {
        super(componentType, container, capacity);
        this.filter = filter;
    }

    @Override
    public boolean canFillFluidType(@NotNull FluidStack fluid) {
        return filter.test(fluid);
    }
}
