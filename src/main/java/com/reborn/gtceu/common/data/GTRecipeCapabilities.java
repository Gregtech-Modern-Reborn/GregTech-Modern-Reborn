package com.reborn.gtceu.common.data;

import com.reborn.gtceu.api.GTCEuAPI;
import com.reborn.gtceu.api.addon.AddonFinder;
import com.reborn.gtceu.api.addon.IGTAddon;
import com.reborn.gtceu.api.capability.recipe.*;
import com.reborn.gtceu.api.recipe.ingredient.EnergyStack;
import com.reborn.gtceu.api.recipe.ingredient.FluidIngredient;
import com.reborn.gtceu.api.registry.GTRegistries;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModLoader;

public class GTRecipeCapabilities {

    public final static RecipeCapability<Ingredient> ITEM = ItemRecipeCapability.CAP;
    public final static RecipeCapability<FluidIngredient> FLUID = FluidRecipeCapability.CAP;
    public final static RecipeCapability<BlockState> BLOCK_STATE = BlockStateRecipeCapability.CAP;
    public final static RecipeCapability<EnergyStack> EU = EURecipeCapability.CAP;
    public final static RecipeCapability<Integer> CWU = CWURecipeCapability.CAP;

    public static void init() {
        GTRegistries.RECIPE_CAPABILITIES.unfreeze();

        GTRegistries.RECIPE_CAPABILITIES.register(ITEM.name, ITEM);
        GTRegistries.RECIPE_CAPABILITIES.register(FLUID.name, FLUID);
        GTRegistries.RECIPE_CAPABILITIES.register(BLOCK_STATE.name, BLOCK_STATE);
        GTRegistries.RECIPE_CAPABILITIES.register(EU.name, EU);
        GTRegistries.RECIPE_CAPABILITIES.register(CWU.name, CWU);

        AddonFinder.getAddons().forEach(IGTAddon::registerRecipeCapabilities);
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.RECIPE_CAPABILITIES,
                (Class<RecipeCapability<?>>) (Class<?>) RecipeCapability.class));
        GTRegistries.RECIPE_CAPABILITIES.freeze();
    }
}
