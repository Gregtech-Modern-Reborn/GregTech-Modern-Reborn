package com.gregtechceu.gtceu.api.gui.fancy;

import net.minecraft.network.RegistryFriendlyByteBuf;

public interface IFancyCustomClientActionHandler {

    default void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {}
}
