package com.reborn.gtceu.integration.top.forge;

import com.reborn.gtceu.integration.top.TheOneProbePlugin;

import mcjty.theoneprobe.TheOneProbe;

public class TheOneProbePluginImpl {

    public static void init() {
        TheOneProbePlugin.init(TheOneProbe.theOneProbeImp);
    }
}
