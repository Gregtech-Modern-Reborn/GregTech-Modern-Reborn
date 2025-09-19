package com.reborn.bettergtae.data.recipe;

import com.reborn.bettergtae.common.data.machine.BGTAEMachines;
import com.reborn.gtceu.GTCEu;
import com.reborn.gtceu.api.data.chemical.ChemicalHelper;
import com.reborn.gtceu.api.data.tag.TagPrefix;
import com.reborn.gtceu.common.data.GTBlocks;
import com.reborn.gtceu.common.data.GTItems;
import com.reborn.gtceu.common.data.GTMachines;
import com.reborn.gtceu.common.data.GTMaterials;
import com.reborn.gtceu.data.recipe.CustomTags;
import com.reborn.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

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
    }
}
