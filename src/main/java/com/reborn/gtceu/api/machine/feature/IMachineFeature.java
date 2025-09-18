package com.reborn.gtceu.api.machine.feature;

import com.reborn.gtceu.api.machine.MetaMachine;

public interface IMachineFeature {

    default MetaMachine self() {
        return (MetaMachine) this;
    }
}
