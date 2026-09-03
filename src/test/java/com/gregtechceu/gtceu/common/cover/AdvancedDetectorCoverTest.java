package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.common.cover.detector.AdvancedFluidDetectorCover;
import com.gregtechceu.gtceu.common.cover.detector.AdvancedItemDetectorCover;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * The "electrolyzer" template contains a creative tank with water,
 * that is set to auto-output into an electrolyzer when supplied with a redstone signal
 * The redstone lamp is connected to the covers that are placed in the tests in this class.
 * The creative tank's rate of output is equal to the electrolyzer's rate of processing
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class AdvancedDetectorCoverTest {

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedActivityDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        // The template's electrolyzer is UEV, which overclocks water electrolysis down to a couple of ticks,
        // making progress (and with it this cover's output) flicker between 0 and 15. Pinning the overclock
        // tier to LV keeps the recipe at its base duration so the reported progress is stable.
        ((IOverclockMachine) machine).setOverclockTier(GTValues.LV);
        TestUtils.placeCover(helper, machine, GTItems.COVER_ACTIVITY_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        // Minecraft asks a block for its signal from the opposite side (see MetaMachine#getOutputSignal),
        // so the cover attached to WEST answers a query for Direction.EAST.
        // The cover only refreshes every 20 ticks, so wait for the signal instead of probing a fixed tick.
        helper.succeedWhen(() -> helper.assertRedstoneSignal(
                new BlockPos(1, 2, 1),
                Direction.EAST,
                signal -> signal > 0,
                () -> "expected redstone signal"));
    }

    @GameTest(template = "electrolyzer", batch = "coverTests", timeoutTicks = 200)
    public static void testAdvancedActivityDetectorCoverGoesOffWhenIdle(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        // ZPM keeps water electrolysis at 23 ticks: long enough that the cover's 20 tick sampling sees
        // progress instead of only catching the tick a recipe starts on, short enough for the machine to
        // reach the end of its recipe - and with it idle - inside the test.
        ((IOverclockMachine) machine).setOverclockTier(GTValues.ZPM);
        TestUtils.placeCover(helper, machine, GTItems.COVER_ACTIVITY_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        helper.startSequence()
                .thenWaitUntil(() -> TestUtils.assertLampOn(helper, new BlockPos(0, 2, 1)))
                // Cut the water supply and drop the buffered water, so the machine falls back to idle
                // instead of chewing through the water it already pulled in.
                .thenExecute(() -> {
                    helper.pullLever(2, 2, 2);
                    var tanks = machine.getFluidHandlerCap(null, false);
                    for (int i = 0; i < tanks.getTanks(); i++) {
                        tanks.setFluidInTank(i, FluidStack.EMPTY);
                    }
                })
                .thenWaitUntil(() -> TestUtils.assertLampOff(helper, new BlockPos(0, 2, 1)))
                .thenSucceed();
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedFluidDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        AdvancedFluidDetectorCover cover = (AdvancedFluidDetectorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_FLUID_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        cover.setMaxValue(100000);
        cover.setMinValue(1);
        cover.setLatched(false);
        // At t=40, 36k will be inside, giving a redstone value of 5
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOn(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedItemDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        AdvancedItemDetectorCover cover = (AdvancedItemDetectorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_ITEM_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        cover.setLatched(true);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOn(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedItemDetectorCoverBelowThreshold(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        AdvancedItemDetectorCover cover = (AdvancedItemDetectorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_ITEM_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        cover.setMinValue(1);
        cover.setMaxValue(4);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOff(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedItemDetectorCoverAboveThreshold(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((IMachineBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 1))).getMetaMachine();
        machine.getItemHandlerCap(null, false).setStackInSlot(0, new ItemStack(Items.DIRT, 5));
        AdvancedItemDetectorCover cover = (AdvancedItemDetectorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_ITEM_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        cover.setMinValue(1);
        cover.setMaxValue(4);
        cover.setLatched(true);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOff(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }
}
