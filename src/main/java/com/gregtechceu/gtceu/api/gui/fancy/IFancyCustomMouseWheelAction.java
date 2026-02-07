package com.gregtechceu.gtceu.api.gui.fancy;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IFancyCustomMouseWheelAction extends IFancyCustomClientActionHandler {

    default boolean mouseWheelMove(BiConsumer<Integer, Consumer<RegistryFriendlyByteBuf>> writeClientAction,
                                   double mouseX,
                                   double mouseY, double wheelDelta) {
        return false;
    }
}
