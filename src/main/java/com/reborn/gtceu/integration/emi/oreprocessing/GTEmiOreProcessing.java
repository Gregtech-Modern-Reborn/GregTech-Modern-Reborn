package com.reborn.gtceu.integration.emi.oreprocessing;

import com.reborn.gtceu.api.data.chemical.material.Material;
import com.reborn.gtceu.integration.xei.widgets.GTOreByProductWidget;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;

import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import org.jetbrains.annotations.Nullable;

public class GTEmiOreProcessing extends ModularEmiRecipe<GTOreByProductWidget> {

    final Material material;

    public GTEmiOreProcessing(Material material) {
        super(() -> new GTOreByProductWidget(material));
        this.material = material;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTOreProcessingEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return material.getResourceLocation();
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
}
