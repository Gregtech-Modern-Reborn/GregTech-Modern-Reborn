package com.reborn.gtceu.utils;

import com.reborn.gtceu.api.machine.IMachineBlockEntity;
import com.reborn.gtceu.api.machine.WorkableTieredMachine;
import com.reborn.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.reborn.gtceu.api.machine.trait.RecipeHandlerList;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import java.util.Collection;

/**
 * Dummy machine used for searching recipes outside of a machine.
 */
public class DummyRecipeLogicMachine extends WorkableTieredMachine implements IRecipeLogicMachine {

    public DummyRecipeLogicMachine(IMachineBlockEntity be, int tier, Int2IntFunction tankScalingFunction,
                                   Collection<RecipeHandlerList> handlers,
                                   Object... args) {
        super(be, tier, tankScalingFunction, args);
        reinitializeHandlers(handlers);
    }

    public void reinitializeHandlers(Collection<RecipeHandlerList> handlers) {
        this.capabilitiesProxy.clear();
        this.capabilitiesFlat.clear();
        for (RecipeHandlerList handlerList : handlers) {
            this.addHandlerList(handlerList);
        }
    }
}
