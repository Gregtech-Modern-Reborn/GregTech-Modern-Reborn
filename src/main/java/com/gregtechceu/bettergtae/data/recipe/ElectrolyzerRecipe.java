package com.gregtechceu.bettergtae.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;

import appeng.core.definitions.AEItems;

import java.util.function.Consumer;

public class ElectrolyzerRecipe {

    public static void init(Consumer<FinishedRecipe> consumer) {
        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder(GTCEu.id("charged_certus_quartz_crystal"))
                .inputItems(TagUtil.createItemTag("gems/certus_quartz"))
                .outputItems(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem())
                .duration(20)
                .EUt(GTValues.V[GTValues.LV], 1)
                .save(consumer);
    }
}
