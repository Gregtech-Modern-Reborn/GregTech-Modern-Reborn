package com.reborn.gtceu.integration.emi.orevein;

import com.reborn.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.reborn.gtceu.client.ClientProxy;
import com.reborn.gtceu.integration.xei.widgets.GTOreVeinWidget;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import org.jetbrains.annotations.Nullable;

public class GTBedrockFluid extends ModularEmiRecipe<WidgetGroup> {

    private final BedrockFluidDefinition fluid;

    public GTBedrockFluid(BedrockFluidDefinition fluid) {
        super(() -> new GTOreVeinWidget(fluid));
        this.fluid = fluid;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTBedrockFluidEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return ClientProxy.CLIENT_FLUID_VEINS.inverse().get(fluid).withPrefix("/bedrock_fluid_diagram/");
    }
}
