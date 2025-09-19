package com.reborn.bettergtae.data.recipe;

import com.reborn.gtceu.GTCEu;
import com.reborn.gtceu.api.GTValues;
import com.reborn.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import appeng.core.definitions.AEItems;

import java.util.function.Consumer;

public class MixerRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTCEu.id("fluix_crystal"))
                .inputFluids(new FluidStack(Fluids.WATER, 500))
                .inputItems(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem())
                .inputItems(Items.REDSTONE)
                .inputItems(Items.QUARTZ)
                .outputItems(AEItems.FLUIX_CRYSTAL.asItem(), 2)
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);

        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTCEu.id("fluix_crystal_1"))
                .inputFluids(new FluidStack(Fluids.WATER, 500))
                .inputItems(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem())
                .inputItems(AEItems.FLUIX_DUST.asItem())
                .outputItems(AEItems.FLUIX_CRYSTAL.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV])
                .save(provider);
    }
}
