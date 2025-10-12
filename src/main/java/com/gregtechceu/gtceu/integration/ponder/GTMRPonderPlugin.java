package com.gregtechceu.gtceu.integration.ponder;

import com.gregtechceu.gtceu.GTCEu;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class GTMRPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return GTCEu.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> registry) {
        CokeOvenScreen.register(registry);
    }
}
