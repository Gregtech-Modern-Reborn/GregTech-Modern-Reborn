package com.gregtechceu.gtceu.api.item.component.forge;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public interface IComponentCapability {

    void attachCapabilities(RegisterCapabilitiesEvent event, Item item);
}
