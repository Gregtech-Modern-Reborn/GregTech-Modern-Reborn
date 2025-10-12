package com.gregtechceu.gtceu.integration.ponder;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CokeOvenScreen {

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(ResourceLocation.tryBuild("gtceu", "coke_oven"))
                .addStoryBoard("coke_oven/coke_oven_stage_1", CokeOvenScreenAnimationStage1::coke_oven);
    }
}
