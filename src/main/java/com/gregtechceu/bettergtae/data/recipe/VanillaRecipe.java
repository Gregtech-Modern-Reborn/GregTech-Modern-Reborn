package com.gregtechceu.bettergtae.data.recipe;

import com.gregtechceu.bettergtae.common.data.machine.BGTAEMachines;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTItems.*;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTItems.*;

public class VanillaRecipe {

    public static void init(Consumer<FinishedRecipe> consumer) {
        VanillaRecipeHelper.addShapedRecipe(consumer, true, GTCEu.id("cleanroom_sterile_maintenance_hatch"),
                BGTAEMachines.STERILE_CLEANING_MAINTENANCE_HATCH.asStack(),
                "ABA",
                "CDC",
                "EAE",
                'A', CustomTags.UHV_CIRCUITS,
                'B', GTMachines.CLEANING_MAINTENANCE_HATCH.asStack(),
                'C', GTItems.ROBOT_ARM_UV.asStack(),
                'D', GTBlocks.FILTER_CASING_STERILE.asStack(),
                'E', ChemicalHelper.get(TagPrefix.cableGtSingle, GTMaterials.Europium));
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("cupronickel_credit_to_copper_credit"),
                COPPER_CREDIT.asStack(), CUPRONICKEL_CREDIT.asStack(), CUPRONICKEL_CREDIT.asStack(),
                CUPRONICKEL_CREDIT.asStack(), CUPRONICKEL_CREDIT.asStack(), CUPRONICKEL_CREDIT.asStack(),
                CUPRONICKEL_CREDIT.asStack(), CUPRONICKEL_CREDIT.asStack(), CUPRONICKEL_CREDIT.asStack());
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("copper_credit_to_cupronickel_credit"),
                CUPRONICKEL_CREDIT.asStack(8), COPPER_CREDIT.asStack());
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("copper_credit_to_silver_credit"),
                SILVER_CREDIT.asStack(), COPPER_CREDIT.asStack(), COPPER_CREDIT.asStack(), COPPER_CREDIT.asStack(),
                COPPER_CREDIT.asStack(), COPPER_CREDIT.asStack(), COPPER_CREDIT.asStack(), COPPER_CREDIT.asStack(),
                COPPER_CREDIT.asStack());
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("silver_credit_to_copper_credit"),
                COPPER_CREDIT.asStack(8), SILVER_CREDIT.asStack());
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("silver_credit_to_gold_credit"),
                GOLD_CREDIT.asStack(), SILVER_CREDIT.asStack(), SILVER_CREDIT.asStack(), SILVER_CREDIT.asStack(),
                SILVER_CREDIT.asStack(), SILVER_CREDIT.asStack(), SILVER_CREDIT.asStack(), SILVER_CREDIT.asStack(),
                SILVER_CREDIT.asStack());
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("gold_credit_to_silver_credit"),
                SILVER_CREDIT.asStack(8), GOLD_CREDIT.asStack());
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("gold_credit_to_platinum_credit"),
                PLATINUM_CREDIT.asStack(), GOLD_CREDIT.asStack(), GOLD_CREDIT.asStack(), GOLD_CREDIT.asStack(),
                GOLD_CREDIT.asStack(), GOLD_CREDIT.asStack(), GOLD_CREDIT.asStack(), GOLD_CREDIT.asStack(),
                GOLD_CREDIT.asStack());
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("platinum_credit_to_gold_credit"),
                GOLD_CREDIT.asStack(8), PLATINUM_CREDIT.asStack());
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("platinum_credit_to_osmium_credit"),
                OSMIUM_CREDIT.asStack(), PLATINUM_CREDIT.asStack(), PLATINUM_CREDIT.asStack(),
                PLATINUM_CREDIT.asStack(), PLATINUM_CREDIT.asStack(), PLATINUM_CREDIT.asStack(),
                PLATINUM_CREDIT.asStack(), PLATINUM_CREDIT.asStack(), PLATINUM_CREDIT.asStack());
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("osmium_credit_to_platinum_credit"),
                PLATINUM_CREDIT.asStack(8), OSMIUM_CREDIT.asStack());
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("osmium_credit_to_naquadah_credit"),
                NAQUADAH_CREDIT.asStack(), OSMIUM_CREDIT.asStack(), OSMIUM_CREDIT.asStack(), OSMIUM_CREDIT.asStack(),
                OSMIUM_CREDIT.asStack(), OSMIUM_CREDIT.asStack(), OSMIUM_CREDIT.asStack(), OSMIUM_CREDIT.asStack(),
                OSMIUM_CREDIT.asStack());
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("naquadah_credit_to_osmium_credit"),
                OSMIUM_CREDIT.asStack(8), NAQUADAH_CREDIT.asStack());
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("neutronium_credit_to_naquadah_credit"),
                NAQUADAH_CREDIT.asStack(8), NEUTRONIUM_CREDIT.asStack());
        VanillaRecipeHelper.addShapelessRecipe(consumer, GTCEu.id("naquadah_credit_to_neutronium_credit"),
                NEUTRONIUM_CREDIT.asStack(), NAQUADAH_CREDIT.asStack(), NAQUADAH_CREDIT.asStack(),
                NAQUADAH_CREDIT.asStack(), NAQUADAH_CREDIT.asStack(), NAQUADAH_CREDIT.asStack(),
                NAQUADAH_CREDIT.asStack(), NAQUADAH_CREDIT.asStack(), NAQUADAH_CREDIT);
    }
}
