package com.reborn.gtceu.integration.emi.orevein;

import com.reborn.gtceu.GTCEu;
import com.reborn.gtceu.api.data.chemical.ChemicalHelper;
import com.reborn.gtceu.api.data.tag.TagPrefix;
import com.reborn.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.reborn.gtceu.client.ClientProxy;
import com.reborn.gtceu.common.data.GTItems;
import com.reborn.gtceu.common.data.GTMaterials;

import net.minecraft.network.chat.Component;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;

public class GTBedrockOreEmiCategory extends EmiRecipeCategory {

    public static final GTBedrockOreEmiCategory CATEGORY = new GTBedrockOreEmiCategory();

    public GTBedrockOreEmiCategory() {
        super(GTCEu.id("bedrock_ore_diagram"),
                EmiStack.of(ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Tungstate)));
    }

    public static void registerDisplays(EmiRegistry registry) {
        for (BedrockOreDefinition fluid : ClientProxy.CLIENT_BEDROCK_ORE_VEINS.values()) {
            registry.addRecipe(new GTBedrockOre(fluid));
        }
    }

    public static void registerWorkStations(EmiRegistry registry) {
        registry.addWorkstation(CATEGORY, EmiStack.of(GTItems.PROSPECTOR_HV.asStack()));
        registry.addWorkstation(CATEGORY, EmiStack.of(GTItems.PROSPECTOR_LuV.asStack()));
    }

    @Override
    public Component getName() {
        return Component.translatable("gtceu.jei.bedrock_ore_diagram");
    }
}
