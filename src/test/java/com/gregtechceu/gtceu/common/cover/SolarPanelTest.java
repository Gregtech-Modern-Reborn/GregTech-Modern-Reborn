package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Solar panel covers need real sky access, which the templates cannot provide: the automated game test
 * server generates an ordinary overworld and lays every test out far underground, so sky light never
 * reaches a machine standing inside the structure. Both tests therefore build their machine in open air
 * high above their own test area and pin the weather, which keeps them independent of the terrain.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class SolarPanelTest {

    private static final int OPEN_SKY_Y = 300;

    private static BatteryBufferMachine makeBatteryBuffer(GameTestHelper helper, int tier) {
        var level = helper.getLevel();
        // rain and thunder also hide the sun, and the test world's weather is not deterministic
        level.setWeatherParameters(24000, 0, false, false);
        BlockPos anchor = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos pos = new BlockPos(anchor.getX(), OPEN_SKY_Y, anchor.getZ());
        // Only the structure itself is cleaned up between runs, so clear this spot first: setting the same
        // machine block again would keep the old block entity, together with its covers and stored energy.
        BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))
                .forEach(p -> level.setBlockAndUpdate(p.immutable(), Blocks.AIR.defaultBlockState()));
        level.setBlockAndUpdate(pos, GTMachines.BATTERY_BUFFER_4[tier].getBlock().defaultBlockState());
        return (BatteryBufferMachine) ((MetaMachineBlockEntity) level.getBlockEntity(pos)).getMetaMachine();
    }

    private static void placeSolar(GameTestHelper helper, MetaMachine machine) {
        TestUtils.placeCover(helper, machine, GTItems.COVER_SOLAR_PANEL_HV.asStack(), Direction.UP);
    }

    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void generatesEnergyAtDayTest(GameTestHelper helper) {
        helper.setDayTime(6000);
        BatteryBufferMachine machine = makeBatteryBuffer(helper, GTValues.HV);
        machine.getBatteryInventory().insertItem(0, GTItems.BATTERY_HV_LITHIUM.asStack(), false);
        placeSolar(helper, machine);
        // the sky light of the freshly placed machine is only known once the light engine caught up
        helper.succeedWhen(() -> helper.assertTrue(machine.energyContainer.getEnergyStored() > 0,
                "Solar panel cover didn't generate energy at day time"));
    }

    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void doesntGenerateEnergyAtDayWhenBlockedTest(GameTestHelper helper) {
        helper.setDayTime(6000);
        BatteryBufferMachine machine = makeBatteryBuffer(helper, GTValues.HV);
        var level = helper.getLevel();
        BlockPos above = machine.getPos().above();
        level.setBlockAndUpdate(above.above(), Blocks.DIAMOND_BLOCK.defaultBlockState());
        machine.getBatteryInventory().insertItem(0, GTItems.BATTERY_HV_LITHIUM.asStack(), false);
        helper.startSequence()
                // blocks placed at runtime leave the light engine a tick behind, so only cover the machine
                // once the sky above it really is blocked - a battery keeps what leaked in before that
                .thenWaitUntil(
                        () -> helper.assertTrue(!level.canSeeSky(above), "sky above the machine is not blocked yet"))
                .thenExecute(() -> placeSolar(helper, machine))
                .thenExecuteAfter(40, () -> helper.assertTrue(machine.energyContainer.getEnergyStored() == 0,
                        "Solar panel cover generated energy when blocked"))
                .thenSucceed();
    }
}
