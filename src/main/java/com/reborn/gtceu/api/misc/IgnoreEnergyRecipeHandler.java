package com.reborn.gtceu.api.misc;

import com.reborn.gtceu.api.capability.recipe.EURecipeCapability;
import com.reborn.gtceu.api.capability.recipe.IO;
import com.reborn.gtceu.api.capability.recipe.IRecipeHandler;
import com.reborn.gtceu.api.capability.recipe.RecipeCapability;
import com.reborn.gtceu.api.recipe.GTRecipe;
import com.reborn.gtceu.api.recipe.ingredient.EnergyStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IgnoreEnergyRecipeHandler implements IRecipeHandler<EnergyStack> {

    @Override
    public List<EnergyStack> handleRecipeInner(IO io, GTRecipe recipe, List<EnergyStack> left, boolean simulate) {
        return null;
    }

    @Override
    public @NotNull List<Object> getContents() {
        return List.of(EnergyStack.MAX);
    }

    @Override
    public double getTotalContentAmount() {
        return Long.MAX_VALUE;
    }

    @Override
    public RecipeCapability<EnergyStack> getCapability() {
        return EURecipeCapability.CAP;
    }
}
