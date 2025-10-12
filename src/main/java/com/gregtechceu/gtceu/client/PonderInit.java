package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.integration.ponder.GTMRPonderPlugin;

import net.createmod.ponder.foundation.PonderIndex;

public class PonderInit {

    public static void init() {
        PonderIndex.addPlugin(new GTMRPonderPlugin());
    }
}
