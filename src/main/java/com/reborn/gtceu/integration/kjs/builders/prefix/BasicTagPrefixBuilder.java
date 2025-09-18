package com.reborn.gtceu.integration.kjs.builders.prefix;

import com.reborn.gtceu.integration.kjs.built.KJSTagPrefix;

import net.minecraft.resources.ResourceLocation;

public class BasicTagPrefixBuilder extends TagPrefixBuilder {

    public BasicTagPrefixBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public KJSTagPrefix create(String id) {
        return new KJSTagPrefix(id);
    }
}
