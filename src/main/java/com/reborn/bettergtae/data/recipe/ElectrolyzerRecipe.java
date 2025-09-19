package com.reborn.bettergtae.data.recipe;

import com.reborn.gtceu.GTCEu;
import com.reborn.gtceu.api.GTValues;
import com.reborn.gtceu.api.data.tag.TagUtil;
import com.reborn.gtceu.common.data.GTRecipeTypes;

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
