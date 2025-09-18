package com.reborn.gtceu.api.data.worldgen.generator;

import com.reborn.gtceu.GTCEu;
import com.reborn.gtceu.api.addon.AddonFinder;
import com.reborn.gtceu.api.addon.IGTAddon;
import com.reborn.gtceu.api.data.worldgen.GTOreDefinition;
import com.reborn.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.reborn.gtceu.api.data.worldgen.generator.indicators.NoopIndicatorGenerator;
import com.reborn.gtceu.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;

import java.util.function.Function;

public class IndicatorGenerators {

    public static final Codec<NoopIndicatorGenerator> NO_OP = register(GTCEu.id("no_op"), NoopIndicatorGenerator.CODEC,
            entry -> NoopIndicatorGenerator.INSTANCE);

    public static final Codec<SurfaceIndicatorGenerator> SURFACE = register(GTCEu.id("surface"),
            SurfaceIndicatorGenerator.CODEC, SurfaceIndicatorGenerator::new);

    public static <T extends IndicatorGenerator> Codec<T> register(ResourceLocation id, Codec<T> codec,
                                                                   Function<GTOreDefinition, T> function) {
        WorldGeneratorUtils.INDICATOR_GENERATORS.put(id, codec);
        WorldGeneratorUtils.INDICATOR_GENERATOR_FUNCTIONS.put(id, function);
        return codec;
    }

    public static void registerAddonGenerators() {
        AddonFinder.getAddons().forEach(IGTAddon::registerIndicatorGenerators);
    }
}
