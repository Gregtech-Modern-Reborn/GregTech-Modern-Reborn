package com.reborn.gtceu.integration.emi.orevein;

import com.reborn.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.reborn.gtceu.client.ClientProxy;
import com.reborn.gtceu.integration.xei.widgets.GTOreVeinWidget;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import org.jetbrains.annotations.Nullable;

public class GTBedrockOre extends ModularEmiRecipe<WidgetGroup> {

    private final BedrockOreDefinition bedrockOre;

    public GTBedrockOre(BedrockOreDefinition bedrockOre) {
        super(() -> new GTOreVeinWidget(bedrockOre));
        this.bedrockOre = bedrockOre;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTBedrockOreEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return ClientProxy.CLIENT_BEDROCK_ORE_VEINS.inverse().get(bedrockOre).withPrefix("/bedrock_ore_diagram/");
    }
}
