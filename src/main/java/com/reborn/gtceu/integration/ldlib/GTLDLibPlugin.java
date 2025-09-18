package com.reborn.gtceu.integration.ldlib;

import com.reborn.gtceu.common.data.GTSyncedFieldAccessors;

import com.lowdragmc.lowdraglib.plugin.ILDLibPlugin;
import com.lowdragmc.lowdraglib.plugin.LDLibPlugin;

@LDLibPlugin
public class GTLDLibPlugin implements ILDLibPlugin {

    @Override
    public void onLoad() {
        GTSyncedFieldAccessors.init();
    }
}
