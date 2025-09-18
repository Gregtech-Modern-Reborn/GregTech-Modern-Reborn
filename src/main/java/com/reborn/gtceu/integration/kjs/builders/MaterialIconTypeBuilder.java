package com.reborn.gtceu.integration.kjs.builders;

import com.reborn.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.reborn.gtceu.api.registry.registrate.BuilderBase;

import net.minecraft.resources.ResourceLocation;

public class MaterialIconTypeBuilder extends BuilderBase<MaterialIconType> {

    public MaterialIconTypeBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public MaterialIconType register() {
        return new MaterialIconType(this.id.getPath());
    }
}
