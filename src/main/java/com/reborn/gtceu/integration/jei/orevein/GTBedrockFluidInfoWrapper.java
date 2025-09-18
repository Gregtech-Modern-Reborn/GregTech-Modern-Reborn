package com.reborn.gtceu.integration.jei.orevein;

import com.reborn.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.reborn.gtceu.integration.xei.widgets.GTOreVeinWidget;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;

public class GTBedrockFluidInfoWrapper extends ModularWrapper<GTOreVeinWidget> {

    public final BedrockFluidDefinition fluid;

    public GTBedrockFluidInfoWrapper(BedrockFluidDefinition fluid) {
        super(new GTOreVeinWidget(fluid));
        this.fluid = fluid;
    }
}
