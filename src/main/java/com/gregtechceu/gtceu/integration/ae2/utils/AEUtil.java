package com.gregtechceu.gtceu.integration.ae2.utils;

import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;

public class AEUtil {

    public static @Nullable GenericStack fromFluidStack(FluidStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        var key = AEFluidKey.of(stack);
        return new GenericStack(key, stack.getAmount());
    }

    public static FluidStack toFluidStack(GenericStack stack) {
        var key = stack.what();
        if (key instanceof AEFluidKey fluidKey) {
            return toFluidStack(fluidKey, stack.amount());
        }
        return FluidStack.EMPTY;
    }

    public static FluidStack toFluidStack(AEFluidKey key, long amount) {
        return key.toStack(GTMath.saturatedCast(amount));
    }

    public static ItemStack toItemStack(GenericStack stack) {
        var key = stack.what();
        if (key instanceof AEItemKey itemKey) {
            return toItemStack(itemKey, stack.amount());
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack toItemStack(AEItemKey key, long amount) {
        return key.toStack(GTMath.saturatedCast(amount));
    }

    public static boolean matches(AEFluidKey key, FluidStack stack) {
        return key.matches(stack);
    }
}
