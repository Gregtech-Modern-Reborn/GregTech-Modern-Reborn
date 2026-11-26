package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Set;

import static com.gregtechceu.gtceu.gametest.util.TestUtils.getMetaMachine;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class RecipeLogicTest {

    private static GTRecipeType LCR_RECIPE_TYPE;
    private static GTRecipeType CR_RECIPE_TYPE;

    @BeforeBatch(batch = "RecipeLogic")
    public static void prepare(ServerLevel level) {
        LCR_RECIPE_TYPE = TestUtils.createRecipeType("recipe_logic_test_lcr");
        CR_RECIPE_TYPE = TestUtils.createRecipeType("recipe_logic_test_cr");

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_multiblock_recipelogic"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.HV]).duration(1)
                .buildRawRecipe());
        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_multiblock_recipelogic_16_items"))
                .inputItems(new ItemStack(Blocks.STONE, 16))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.HV]).duration(1)
                .buildRawRecipe());
        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_multiblock_fail_ev_tier"))
                .inputItems(new ItemStack(Blocks.DIAMOND_BLOCK))
                .outputItems(new ItemStack(Blocks.EMERALD_BLOCK))
                .EUt(GTValues.V[GTValues.EV]).duration(1)
                .buildRawRecipe());
        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_multiblock_fail_zpm_tier"))
                .inputItems(new ItemStack(Blocks.EMERALD_BLOCK))
                .outputItems(new ItemStack(Blocks.GOLD_BLOCK))
                .EUt(GTValues.V[GTValues.ZPM]).duration(1)
                .buildRawRecipe());
        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_multiblock_fail_dimension"))
                .inputItems(new ItemStack(Blocks.GOLD_BLOCK))
                .outputItems(new ItemStack(Blocks.IRON_BLOCK))
                .addCondition(new DimensionCondition(Level.NETHER.location()))
                .EUt(GTValues.VA[GTValues.HV]).duration(1)
                .buildRawRecipe());
        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_multiblock_power_fail_long"))
                .inputItems(new ItemStack(Blocks.COPPER_BLOCK))
                .outputItems(new ItemStack(Blocks.IRON_BLOCK))
                .EUt(GTValues.VA[GTValues.HV]).duration(20)
                .buildRawRecipe());

        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_singleblock_recipelogic"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.HV]).duration(1)
                .buildRawRecipe());
        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_singleblock_fail_ev_tier"))
                .inputItems(new ItemStack(Blocks.DIAMOND_BLOCK))
                .outputItems(new ItemStack(Blocks.EMERALD_BLOCK))
                .EUt(GTValues.V[GTValues.EV]).duration(1)
                .buildRawRecipe());
    }

    private record BusHolder(ItemBusPartMachine inputBus1, ItemBusPartMachine inputBus2, ItemBusPartMachine outputBus1,
                             WorkableMultiblockMachine controller) {}

    /**
     * Retrieves the busses for this specific template and force a multiblock structure check
     *
     * @param helper the GameTestHelper
     * @return the busses, in the BusHolder record.
     */
    private static RecipeLogicTest.BusHolder getBussesAndForm(GameTestHelper helper) {
        WorkableMultiblockMachine controller = (WorkableMultiblockMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 2, 0)));
        TestUtils.formMultiblock(controller);
        controller.setRecipeType(LCR_RECIPE_TYPE);
        ItemBusPartMachine inputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 1, 0)));
        ItemBusPartMachine inputBus2 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 2, 0)));
        ItemBusPartMachine outputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));
        return new RecipeLogicTest.BusHolder(inputBus1, inputBus2, outputBus1, controller);
    }

    @GameTest(template = "lcr_input_separation", batch = "RecipeLogic")
    public static void recipeLogicMultiBlockTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(1, 2, 0));

        RecipeLogicTest.BusHolder busHolder = getBussesAndForm(helper);

        helper.assertTrue(busHolder.controller.isFormed(), "Controller didn't form after structure check");
        helper.assertTrue(busHolder.controller.getParts().size() == 6,
                "Controller didn't register all 6 parts after structure check, only registered " +
                        busHolder.controller.getParts().size());

        RecipeLogic recipeLogic = busHolder.controller.getRecipeLogic();

        recipeLogic.findAndHandleRecipe();

        // No recipe found
        helper.assertFalse(recipeLogic.isActive(), "Recipe logic is active, even when it shouldn't be");
        helper.assertTrue(recipeLogic.getLastRecipe() == null,
                "Recipe logic has somehow found a recipe, when there should be none");

        // Put an item in the inventory that will trigger recipe recheck
        NotifiableItemStackHandler inputSlots = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler outputSlots = busHolder.outputBus1.getInventory();

        inputSlots.insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        recipeLogic.findAndHandleRecipe();
        helper.assertFalse(recipeLogic.getLastRecipe() == null,
                "Last recipe is empty, even though recipe logic should've found a recipe.");
        helper.assertTrue(recipeLogic.isActive(), "Recipelogic is inactive, when it should be active.");
        int stackCount = inputSlots.getStackInSlot(0).getCount();
        helper.assertTrue(stackCount == 15, "Count is wrong (should be 15, when it's %s)".formatted(stackCount));

        // Save a reference to the old recipe so we can make sure it's getting reused
        GTRecipe prev = recipeLogic.getLastRecipe();

        // Finish the recipe, the output should generate, and the next iteration should begin
        recipeLogic.serverTick();
        helper.assertTrue(recipeLogic.getLastRecipe().equals(prev), "lastRecipe is wrong");
        helper.assertTrue(
                TestUtils.isItemStackEqual(outputSlots.getStackInSlot(0),
                        new ItemStack(Blocks.STONE, 1)),
                "wrong output stack.");
        helper.assertTrue(recipeLogic.isActive(), "RecipeLogic is not active, when it should be.");

        // Complete the second iteration, but the machine stops because its output is now full
        // Fill up the recipe with enough stone to complete 1 more recipe and then nothing more.
        outputSlots.setStackInSlot(0,
                new ItemStack(Blocks.STONE, 63));
        for (int i = 1; i < outputSlots.getSlots(); i++) {
            outputSlots.setStackInSlot(i,
                    new ItemStack(Blocks.STONE, 64));
        }
        recipeLogic.serverTick();
        helper.assertFalse(recipeLogic.isActive(), "RecipeLogic is active, when it shouldn't be.");

        // Try to process again and get failed out because of full buffer.
        recipeLogic.serverTick();
        helper.assertFalse(recipeLogic.isActive(), "Recipelogic is active, when it shouldn't be.");

        // Some room is freed in the output bus, so we can continue now.
        outputSlots.setStackInSlot(0, ItemStack.EMPTY);
        recipeLogic.serverTick();
        helper.assertTrue(recipeLogic.isActive(), "RecipeLogic didn't start running again");
        recipeLogic.serverTick();
        helper.assertTrue(
                TestUtils.isItemStackEqual(outputSlots.getStackInSlot(0),
                        new ItemStack(Blocks.STONE, 1)),
                "Wrong stack.");

        // Finish.
        helper.succeed();
    }

    // spotless:off
    // Blocked by LDLib sync issues
    /*
    @GameTest(template = "singleblock_charged_cr", batch = "RecipeLogic")
    public static void recipeLogicSingleBlockTest(GameTestHelper helper) {
        WorkableTieredMachine machine = (WorkableTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler inputSlots = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler outputSlots = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);


        RecipeLogic recipeLogic = machine.getRecipeLogic();

        recipeLogic.findAndHandleRecipe();

        // no recipe found
        helper.assertFalse(recipeLogic.isActive(), "Recipe logic is active, even when it shouldn't be");
        helper.assertTrue(recipeLogic.getLastRecipe() == null,
                "Recipe logic has somehow found a recipe, when there should be none");

        inputSlots.insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        inputSlots.onContentsChanged();

        recipeLogic.findAndHandleRecipe();
        helper.assertFalse(recipeLogic.getLastRecipe() == null,
                "Last recipe is empty, even though recipe logic should've found a recipe.");
        helper.assertTrue(recipeLogic.isActive(), "Recipelogic is inactive, when it should be active.");
        int stackCount = inputSlots.getStackInSlot(0).getCount();
        helper.assertTrue(stackCount == 15, "Count is wrong (should be 15, when it's %s)".formatted(stackCount));

        // Save a reference to the old recipe so we can make sure it's getting reused
        ResourceLocation prev = recipeLogic.getLastRecipe().getId();

        // Finish the recipe, the output should generate, and the next iteration should begin
        recipeLogic.serverTick();
        helper.assertTrue(recipeLogic.getLastRecipe().getId().equals(prev), "lastRecipe is wrong");
        helper.assertTrue(TestUtils.isItemStackEqual(
                outputSlots.getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)),
                "wrong output stack.");
        helper.assertTrue(recipeLogic.isActive(), "RecipeLogic is not active, when it should be.");

        // Complete the second iteration, but the machine stops because its output is now full
        outputSlots.setStackInSlot(0,
                new ItemStack(Blocks.STONE, 63));
        outputSlots.setStackInSlot(1,
                new ItemStack(Blocks.STONE, 64));
        recipeLogic.serverTick();
        helper.assertFalse(recipeLogic.isActive(), "RecipeLogic is active, when it shouldn't be.");

        // Try to process again and get failed out because of full buffer.
        recipeLogic.serverTick();
        helper.assertFalse(recipeLogic.isActive(), "Recipelogic is active, when it shouldn't be.");

        // Some room is freed in the output bus, so we can continue now.
        outputSlots.setStackInSlot(0, ItemStack.EMPTY);
        recipeLogic.serverTick();
        helper.assertTrue(recipeLogic.isActive(), "RecipeLogic didn't start running again");
        recipeLogic.serverTick();
        helper.assertTrue(
                TestUtils.isItemStackEqual(
                        outputSlots.getStackInSlot(0),
                        new ItemStack(Blocks.STONE, 1)),
                "Wrong stack.");

        // Finish.
        helper.succeed();
    }
     */
    // spotless:on

    @GameTest(template = "lcr_input_separation", batch = "RecipeLogic")
    public static void softMalletStopsAfterRecipeTest(GameTestHelper helper) {
        testSoftMalletStopsAfterRecipe(helper, false);
    }

    @GameTest(template = "lcr_input_separation", batch = "RecipeLogic")
    public static void softMalletStopsMultiParallelAfterRecipeTest(GameTestHelper helper) {
        testSoftMalletStopsAfterRecipe(helper, true);
    }

    private static void testSoftMalletStopsAfterRecipe(GameTestHelper helper, boolean multiParallel) {
        BusHolder buses = getBussesAndForm(helper);
        RecipeLogic logic = buses.controller.getRecipeLogic();
        logic.setMultiParallelLogic(multiParallel);
        logic.setMultiParallelCount(1);
        NotifiableItemStackHandler inputs = buses.inputBus1.getInventory();
        NotifiableItemStackHandler outputs = buses.outputBus1.getInventory();
        inputs.setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 16));
        logic.findAndHandleRecipe();
        int consumed = 1;
        helper.assertTrue(logic.isWorking(), "Recipe did not start");
        helper.assertTrue(inputs.getStackInSlot(0).getCount() == 16 - consumed,
                "Recipe consumed an unexpected amount of input");

        clickSoftMallet(helper, buses.controller);
        helper.assertFalse(logic.isWorkingEnabled(), "Soft mallet did not schedule a stop");
        helper.assertTrue(logic.isWorking(), "Soft mallet stopped the current recipe before completion");
        logic.serverTick();

        helper.assertTrue(logic.isSuspend(), "Machine stayed working at 100% instead of stopping");
        helper.assertFalse(logic.isActive(), "Completed recipe left the suspended machine active");
        helper.assertTrue(logic.getProgress() == 0 && logic.getDuration() == 0,
                "Completed recipe progress was not cleared");
        helper.assertFalse(logic.isSuspendAfterFinish(), "Scheduled stop was not cleared");
        TestUtils.assertEqual(helper, outputs.getStackInSlot(0), new ItemStack(Blocks.STONE, consumed));
        helper.assertTrue(inputs.getStackInSlot(0).getCount() == 16 - consumed,
                "Machine consumed the next recipe's inputs before stopping");

        for (int tick = 0; tick < 5; tick++) {
            logic.serverTick();
        }
        TestUtils.assertEqual(helper, outputs.getStackInSlot(0), new ItemStack(Blocks.STONE, consumed));
        helper.assertTrue(inputs.getStackInSlot(0).getCount() == 16 - consumed,
                "Suspended machine consumed more inputs");

        clickSoftMallet(helper, buses.controller);
        helper.assertTrue(logic.isWorkingEnabled() && logic.isIdle(), "Soft mallet did not re-enable the machine");
        logic.serverTick();
        helper.assertTrue(logic.isWorking(), "Re-enabled machine did not start a new recipe");
        TestUtils.assertEqual(helper, outputs.getStackInSlot(0), new ItemStack(Blocks.STONE, consumed));
        helper.assertTrue(inputs.getStackInSlot(0).getCount() == 16 - 2 * consumed,
                "Re-enabled machine did not consume fresh inputs");

        clickSoftMallet(helper, buses.controller);
        logic.serverTick();
        helper.assertTrue(logic.isSuspend(), "Second scheduled stop did not finish");
        TestUtils.assertEqual(helper, outputs.getStackInSlot(0), new ItemStack(Blocks.STONE, 2 * consumed));
        helper.succeed();
    }

    @GameTest(template = "lcr_input_separation", batch = "RecipeLogic")
    public static void softMalletCancelsScheduledStopTest(GameTestHelper helper) {
        BusHolder buses = getBussesAndForm(helper);
        RecipeLogic logic = buses.controller.getRecipeLogic();
        buses.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 16));
        logic.findAndHandleRecipe();

        clickSoftMallet(helper, buses.controller);
        clickSoftMallet(helper, buses.controller);
        helper.assertFalse(logic.isSuspendAfterFinish(), "Second soft mallet click did not cancel the stop");
        logic.serverTick();
        helper.assertTrue(logic.isWorking(), "Machine stopped after the scheduled stop was cancelled");
        TestUtils.assertEqual(helper, buses.outputBus1.getInventory().getStackInSlot(0), new ItemStack(Blocks.STONE));
        helper.assertTrue(buses.inputBus1.getInventory().getStackInSlot(0).getCount() == 14,
                "Machine did not start the next recipe after cancelling the stop");
        helper.succeed();
    }

    private static void clickSoftMallet(GameTestHelper helper, WorkableMultiblockMachine machine) {
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        var hit = new BlockHitResult(Vec3.atCenterOf(machine.getPos()).add(0, 0, -0.5), Direction.NORTH,
                machine.getPos(), false);
        var result = machine.onToolClick(Set.of(GTToolType.SOFT_MALLET), ItemStack.EMPTY,
                new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        helper.assertTrue(result.getSecond().consumesAction(), "Machine did not accept the soft mallet click");
    }

    // Test for putting both ingredients in the same bus in 2 stacks.
    @GameTest(template = "lcr_input_separation", batch = "RecipeLogicTest")
    public static void recipeLogicInTwoStacksTest(GameTestHelper helper) {
        RecipeLogicTest.BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.STONE, 10));
        busHolder.inputBus1.getInventory().setStackInSlot(1, new ItemStack(Blocks.STONE, 6));
        helper.succeedWhen(() -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Crafting items in same bus failed, expected STONE but was " +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    private static void assertReasonKey(GameTestHelper helper, RecipeLogic logic, String expectedKey) {
        Component reason = logic.getWaitingReason();
        helper.assertTrue(reason != null,
                "Expected waitingReason with key '" + expectedKey + "' but got null");
        String actualKey = reason.getContents() instanceof TranslatableContents tc ? tc.getKey() : reason.getString();
        helper.assertTrue(expectedKey.equals(actualKey),
                "Expected waitingReason key '" + expectedKey + "' but got '" + actualKey + "'");
    }

    @GameTest(template = "lcr_input_separation", batch = "RecipeLogic")
    public static void recipeFailReasonOutputFullAndClearedTest(GameTestHelper helper) {
        BusHolder buses = getBussesAndForm(helper);
        RecipeLogic logic = buses.controller.getRecipeLogic();
        NotifiableItemStackHandler inputs = buses.inputBus1.getInventory();
        NotifiableItemStackHandler outputs = buses.outputBus1.getInventory();

        for (int i = 0; i < outputs.getSlots(); i++) {
            outputs.setStackInSlot(i, new ItemStack(Blocks.STONE, 64));
        }
        inputs.setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 4));
        logic.findAndHandleRecipe();

        helper.assertTrue(logic.isIdle(), "Startup output-full failure must remain in IDLE status");
        helper.assertFalse(logic.isActive(), "Startup output-full failure must not mark machine active");
        assertReasonKey(helper, logic, "gtceu.recipe_logic.insufficient_out");
        helper.assertTrue(logic.shouldDisplayHudReason(), "Output-full failure should display on HUD");

        inputs.setStackInSlot(0, ItemStack.EMPTY);
        logic.findAndHandleRecipe();
        helper.assertTrue(logic.getWaitingReason() == null,
                "Failure reason should clear when inputs are removed");

        inputs.setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 4));
        outputs.setStackInSlot(0, ItemStack.EMPTY);
        logic.findAndHandleRecipe();
        helper.assertTrue(logic.isWorking(), "Machine should start once output space is freed");
        helper.assertTrue(logic.getWaitingReason() == null,
                "Failure reason should clear once recipe starts working");
        helper.succeed();
    }

    @GameTest(template = "lcr_input_separation", batch = "RecipeLogic")
    public static void recipeFailReasonVoltageTierTooLowTest(GameTestHelper helper) {
        BusHolder buses = getBussesAndForm(helper);
        RecipeLogic logic = buses.controller.getRecipeLogic();
        NotifiableItemStackHandler inputs = buses.inputBus1.getInventory();

        // EV recipe fits in HV buffer capacity, rejected by ELECTRIC_OVERCLOCK modifier
        inputs.setStackInSlot(0, new ItemStack(Blocks.DIAMOND_BLOCK, 1));
        logic.findAndHandleRecipe();
        helper.assertTrue(logic.isIdle() && !logic.isActive(), "EV tier failure must remain IDLE");
        assertReasonKey(helper, logic, "gtceu.recipe_logic.insufficient_tier");

        // ZPM recipe exceeds HV buffer capacity, caught by pure diagnostic pass as tier failure
        inputs.setStackInSlot(0, new ItemStack(Blocks.EMERALD_BLOCK, 1));
        logic.findAndHandleRecipe();
        helper.assertTrue(logic.isIdle() && !logic.isActive(), "ZPM tier failure must remain IDLE");
        assertReasonKey(helper, logic, "gtceu.recipe_logic.insufficient_tier");
        helper.succeed();
    }

    @GameTest(template = "lcr_input_separation", batch = "RecipeLogic")
    public static void recipeFailReasonConditionFailedTest(GameTestHelper helper) {
        BusHolder buses = getBussesAndForm(helper);
        RecipeLogic logic = buses.controller.getRecipeLogic();
        buses.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.GOLD_BLOCK, 1));
        logic.findAndHandleRecipe();

        helper.assertTrue(logic.isIdle() && !logic.isActive(), "Condition failure must remain IDLE");
        assertReasonKey(helper, logic, "gtceu.recipe_logic.condition_fails");
        helper.assertTrue(logic.shouldDisplayHudReason(), "Condition failure should display on HUD");
        helper.succeed();
    }

    @GameTest(template = "lcr_input_separation", batch = "RecipeLogic")
    public static void recipeFailReasonPowerFailRetainedOnAutoSuspendTest(GameTestHelper helper) {
        BusHolder buses = getBussesAndForm(helper);
        RecipeLogic logic = buses.controller.getRecipeLogic();
        buses.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.COPPER_BLOCK, 1));
        logic.findAndHandleRecipe();
        helper.assertTrue(logic.isWorking(), "Long recipe did not start");

        for (var handler : buses.controller.getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP)) {
            if (handler instanceof IEnergyContainer container) {
                container.removeEnergy(container.getEnergyStored());
            }
        }
        for (int attempt = 0; attempt < 5; attempt++) {
            logic.runDelay = 0;
            logic.handleRecipeWorking();
        }

        helper.assertTrue(logic.isSuspend(), "Multiblock should suspend after 5 power-fail attempts");
        assertReasonKey(helper, logic, "gtceu.recipe_logic.power_fail_suspended");
        helper.assertTrue(logic.shouldDisplayHudReason(), "Power-fail suspend reason should display on HUD");

        logic.setWorkingEnabled(true);
        helper.assertTrue(logic.getWaitingReason() == null,
                "Re-enabling machine should clear power-fail suspend reason");
        helper.succeed();
    }

    @GameTest(template = "lcr_input_separation", batch = "RecipeLogic")
    public static void recipeFailReasonMultiParallelOutputFullTest(GameTestHelper helper) {
        BusHolder buses = getBussesAndForm(helper);
        RecipeLogic logic = buses.controller.getRecipeLogic();
        logic.setMultiParallelLogic(true);
        logic.setMultiParallelCount(2);

        NotifiableItemStackHandler outputs = buses.outputBus1.getInventory();
        for (int i = 0; i < outputs.getSlots(); i++) {
            outputs.setStackInSlot(i, new ItemStack(Blocks.STONE, 64));
        }
        buses.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 4));
        logic.findAndHandleRecipe();

        helper.assertTrue(logic.isIdle() && !logic.isActive(),
                "MultiParallel output-full failure must remain IDLE");
        assertReasonKey(helper, logic, "gtceu.recipe_logic.insufficient_out");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "RecipeLogic")
    public static void recipeFailReasonSingleblockTest(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) TestUtils.setMachine(
                helper, new BlockPos(0, 1, 0), GTMachines.COMPRESSOR[GTValues.HV]);
        GTRecipeType originalType = machine.getRecipeType();
        try {
            machine.setRecipeType(CR_RECIPE_TYPE);
            NotifiableEnergyContainer energyContainer = (NotifiableEnergyContainer) machine
                    .getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP).get(0);
            energyContainer.setEnergyStored(energyContainer.getEnergyCapacity());
            NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                    .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
            NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                    .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);
            RecipeLogic logic = machine.getRecipeLogic();

            itemIn.setStackInSlot(0, new ItemStack(Blocks.DIAMOND_BLOCK, 1));
            logic.findAndHandleRecipe();
            helper.assertTrue(logic.isIdle() && !logic.isActive(), "Singleblock tier failure must remain IDLE");
            assertReasonKey(helper, logic, "gtceu.recipe_logic.insufficient_tier");

            for (int i = 0; i < itemOut.getSlots(); i++) {
                itemOut.setStackInSlot(i, new ItemStack(Blocks.STONE, 64));
            }
            itemIn.setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 1));
            logic.findAndHandleRecipe();
            helper.assertTrue(logic.isIdle() && !logic.isActive(), "Singleblock output-full failure must remain IDLE");
            assertReasonKey(helper, logic, "gtceu.recipe_logic.insufficient_out");
            helper.succeed();
        } finally {
            machine.setRecipeType(originalType);
        }
    }
}
