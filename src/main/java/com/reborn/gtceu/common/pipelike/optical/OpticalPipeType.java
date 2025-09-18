package com.reborn.gtceu.common.pipelike.optical;

import com.reborn.gtceu.GTCEu;
import com.reborn.gtceu.api.pipenet.IPipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum OpticalPipeType implements IPipeType<OpticalPipeProperties>, StringRepresentable {

    NORMAL;

    public static final ResourceLocation TYPE = GTCEu.id("optical");

    @Override
    public float getThickness() {
        return 0.375F;
    }

    @Override
    public OpticalPipeProperties modifyProperties(OpticalPipeProperties baseProperties) {
        return baseProperties;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
