package com.reborn.gtceu.integration.jei.oreprocessing;

import com.reborn.gtceu.api.data.chemical.material.Material;
import com.reborn.gtceu.integration.xei.widgets.GTOreByProductWidget;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;

public class GTOreProcessingInfoWrapper extends ModularWrapper<GTOreByProductWidget> {

    public final Material material;

    public GTOreProcessingInfoWrapper(Material mat) {
        super(new GTOreByProductWidget(mat));
        this.material = mat;
    }
}
