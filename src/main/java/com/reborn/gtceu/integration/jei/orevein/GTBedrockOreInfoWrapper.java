package com.reborn.gtceu.integration.jei.orevein;

import com.reborn.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.reborn.gtceu.integration.xei.widgets.GTOreVeinWidget;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;

public class GTBedrockOreInfoWrapper extends ModularWrapper<GTOreVeinWidget> {

    public final BedrockOreDefinition bedrockOre;

    public GTBedrockOreInfoWrapper(BedrockOreDefinition bedrockOre) {
        super(new GTOreVeinWidget(bedrockOre));
        this.bedrockOre = bedrockOre;
    }
}
