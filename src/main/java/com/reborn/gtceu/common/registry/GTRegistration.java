package com.reborn.gtceu.common.registry;

import com.reborn.gtceu.GTCEu;
import com.reborn.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class GTRegistration {

    public static final GTRegistrate REGISTRATE = GTRegistrate.create(GTCEu.MOD_ID);
    static {
        GTRegistration.REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    private GTRegistration() {/**/}
}
