package com.reborn.gtceu.api.data.worldgen.bedrockore;

import com.reborn.gtceu.api.GTCEuAPI;
import com.reborn.gtceu.api.data.chemical.material.Material;
import com.reborn.gtceu.utils.WeightedEntry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WeightedMaterial(Material material, int weight) implements WeightedEntry {

    public static final Codec<WeightedMaterial> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    GTCEuAPI.materialManager.codec().fieldOf("material").forGetter(WeightedMaterial::material),
                    Codec.INT.fieldOf("weight").forGetter(WeightedMaterial::weight))
                    .apply(instance, WeightedMaterial::new));
}
