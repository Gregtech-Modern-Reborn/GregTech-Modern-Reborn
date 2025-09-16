package com.reborn.gtceu.api.capability;

import com.reborn.gtceu.api.machine.trait.NotifiableComputationContainer;

/**
 * Used in conjunction with {@link NotifiableComputationContainer}.
 */
public interface IOpticalComputationReceiver {

    IOpticalComputationProvider getComputationProvider();
}
