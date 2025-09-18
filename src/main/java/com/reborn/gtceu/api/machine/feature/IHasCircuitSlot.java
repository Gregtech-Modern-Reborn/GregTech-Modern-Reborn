package com.reborn.gtceu.api.machine.feature;

import com.reborn.gtceu.api.machine.trait.NotifiableItemStackHandler;

public interface IHasCircuitSlot {

    default boolean isCircuitSlotEnabled() {
        return true;
    }

    NotifiableItemStackHandler getCircuitInventory();
}
