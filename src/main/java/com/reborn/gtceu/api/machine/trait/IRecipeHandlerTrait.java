package com.reborn.gtceu.api.machine.trait;

import com.reborn.gtceu.api.capability.recipe.IO;
import com.reborn.gtceu.api.capability.recipe.IRecipeHandler;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

public interface IRecipeHandlerTrait<K> extends IRecipeHandler<K> {

    IO getHandlerIO();

    /**
     * add listener for notification when it changed.
     */
    ISubscription addChangedListener(Runnable listener);
}
