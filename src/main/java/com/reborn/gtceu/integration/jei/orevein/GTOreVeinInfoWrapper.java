package com.reborn.gtceu.integration.jei.orevein;

import com.reborn.gtceu.api.data.worldgen.GTOreDefinition;
import com.reborn.gtceu.integration.xei.widgets.GTOreVeinWidget;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;

public class GTOreVeinInfoWrapper extends ModularWrapper<GTOreVeinWidget> {

    public final GTOreDefinition oreDefinition;

    public GTOreVeinInfoWrapper(GTOreDefinition oreDefinition) {
        super(new GTOreVeinWidget(oreDefinition));
        this.oreDefinition = oreDefinition;
    }
}
