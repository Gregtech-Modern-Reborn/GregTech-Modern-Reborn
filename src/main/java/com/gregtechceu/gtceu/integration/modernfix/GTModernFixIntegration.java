package com.gregtechceu.gtceu.integration.modernfix;

import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import com.gregtechceu.gtceu.core.mixins.neoforge.BakedModelWrapperAccessor;

import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;

import net.minecraft.client.resources.model.*;

import lombok.Getter;
import org.embeddedt.modernfix.ModernFixClient;
import org.embeddedt.modernfix.api.entrypoint.ModernFixClientIntegration;
import org.jetbrains.annotations.ApiStatus;

public class GTModernFixIntegration implements ModernFixClientIntegration {

    private static GTModernFixIntegration INSTANCE = null;
    @Getter
    private static boolean dynamicResourcesEnabled = false;

    @ApiStatus.Internal
    public GTModernFixIntegration() {
        INSTANCE = this;
    }

    public static void setAsLast() {
        if (INSTANCE != null) {
            ModernFixClient.CLIENT_INTEGRATIONS.remove(INSTANCE);
        } else {
            INSTANCE = new GTModernFixIntegration();
        }
        ModernFixClient.CLIENT_INTEGRATIONS.add(INSTANCE);
    }

    @Override
    public void onDynamicResourcesStatusChange(boolean enabled) {
        dynamicResourcesEnabled = enabled;
    }

    @Override
    public BakedModel onBakedModelLoad(ModelResourceLocation location, UnbakedModel baseModel,
                                       BakedModel originalModel, ModelState state, ModelBakery bakery,
                                       ModelBakery.TextureGetter textureGetter) {
        if (originalModel instanceof CustomBakedModel<?> ctmModel) {
            // Unwrap all machine models from LDLib CTM models so we don't need to be as aggressive with mixins
            if (((BakedModelWrapperAccessor<?>) ctmModel).gtceu$getParent() instanceof MachineModel machineModel) {
                return machineModel;
            }
        }
        return originalModel;
    }
}
