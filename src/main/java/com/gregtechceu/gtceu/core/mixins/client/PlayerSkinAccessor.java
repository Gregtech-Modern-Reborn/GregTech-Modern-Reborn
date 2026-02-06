package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerSkin.class)
public interface PlayerSkinAccessor {

    @Accessor("capeTexture")
    @Mutable
    void gtceu$setCapeTexture(ResourceLocation capeTexture);
}
