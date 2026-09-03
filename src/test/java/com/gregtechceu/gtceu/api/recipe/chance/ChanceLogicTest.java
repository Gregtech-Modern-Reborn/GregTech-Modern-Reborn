package com.gregtechceu.gtceu.api.recipe.chance;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Chanced outputs are rolled once for a whole recipe run, so the number of rolls is
 * {@code parallels * batchParallels}. These tests cover the run counts at which that number, and the chance
 * math derived from it, no longer fit into an int.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class ChanceLogicTest {

    @GameTest(template = "empty_5x5", batch = "chanceLogic")
    public static void orLogicKeepsChancedOutputsAtHighRunCountTest(GameTestHelper helper) {
        int maxChance = ChanceLogic.getMaxChancedValue();
        Content entry = new Content(new ItemStack(Items.DIRT), maxChance / 2, maxChance, 0);
        // A 50% output rolled a million times is 500k guaranteed outputs with nothing left to roll for.
        // The intermediate product is 5e9 here: as an int it wrapped around to 70503 outputs instead,
        // and any batch above ~430k runs of a 50% output was affected.
        List<Content> rolled = ChanceLogic.OR.roll(List.of(entry), ChanceBoostFunction.NONE, 0, 0, 1_000_000);
        helper.assertTrue(rolled.size() == 500_000,
                "expected 500000 chanced outputs, got " + rolled.size());
        helper.succeed();
    }

    @GameTest(template = "empty_5x5", batch = "chanceLogic")
    public static void runCountSaturatesInsteadOfWrappingTest(GameTestHelper helper) {
        GTRecipeType recipeType = TestUtils.createRecipeType("chance_logic_tests");
        GTRecipe recipe = recipeType
                .recipeBuilder(GTCEu.id("test_chance_logic_run_count"))
                .inputItems(new ItemStack(Items.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.LV])
                .duration(20)
                .buildRawRecipe();
        recipe.parallels = 1 << 20;
        recipe.batchParallels = 1 << 20;

        // Both factors are multiplied by the modifier, and 2^20 * 2^12 does not fit into an int: it used to
        // wrap around to zero (or worse, to a negative count, which drops every chanced output).
        GTRecipe modified = ModifierFunction.builder()
                .parallels(1 << 12)
                .batchParallels(1 << 12)
                .build()
                .apply(recipe);
        helper.assertTrue(modified != null, "modifier cancelled the recipe");
        assert modified != null;
        helper.assertTrue(modified.parallels == Integer.MAX_VALUE,
                "expected saturated parallels, got " + modified.parallels);
        helper.assertTrue(modified.batchParallels == Integer.MAX_VALUE,
                "expected saturated batch parallels, got " + modified.batchParallels);
        helper.succeed();
    }
}
