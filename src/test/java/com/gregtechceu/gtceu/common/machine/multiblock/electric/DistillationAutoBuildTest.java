package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.mojang.authlib.GameProfile;

import java.util.List;
import java.util.UUID;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class DistillationAutoBuildTest {

    @GameTest(template = "empty", batch = "DistillationAutoBuild_largeCreative")
    public static void largeDistilleryCreativeAutoBuildForms(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 20, 2));
        clearRegion(helper, pos.offset(-5, -3, -5), pos.offset(5, 16, 5));

        helper.getLevel().setBlockAndUpdate(pos, GCYMMachines.LARGE_DISTILLERY.getBlock().defaultBlockState());
        DistillationTowerMachine controller = (DistillationTowerMachine) MetaMachine.getMachine(helper.getLevel(), pos);
        helper.assertTrue(controller != null, "Expected DistillationTowerMachine controller for Large Distillery");
        controller.setFrontFacing(Direction.NORTH);

        FakePlayer player = createTestPlayer(helper, GameType.CREATIVE);
        controller.getPattern().autoBuild(player, controller.getMultiblockState());

        boolean matched = controller.checkPatternWithLock();
        helper.assertTrue(matched,
                "Auto-built Large Distillery failed BlockPattern check: " +
                        (controller.getMultiblockState().hasError() ?
                                controller.getMultiblockState().error.getErrorInfo().getString() : "unknown"));
        controller.onStructureFormed();
        helper.assertTrue(controller.isFormed(),
                "Auto-built Large Distillery must stay formed after DistillationTowerMachine.onStructureFormed()");

        List<IMultiPart> fluidExports = controller.getParts().stream()
                .filter(part -> PartAbility.EXPORT_FLUIDS.isApplicable(part.self().getBlockState().getBlock()))
                .filter(part -> part.self().getPos().getY() >= pos.getY() + 1)
                .toList();
        helper.assertTrue(fluidExports.size() == 1,
                "Expected exactly 1 fluid export hatch above controller on auto-built Large Distillery, got " +
                        fluidExports.size());
        helper.assertTrue(fluidExports.get(0).self().getPos().getY() == pos.getY() + 1,
                "Expected fluid export hatch at controllerY + 1");
        helper.assertTrue(controller.getFluidOutputs() != null && controller.getFluidOutputs().size() == 1 &&
                controller.getFirstValid() != null,
                "Expected valid fluidOutputs and firstValid handler on formed Large Distillery");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "DistillationAutoBuild_largeSurvival")
    public static void largeDistillerySurvivalAutoBuildForms(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 20, 2));
        clearRegion(helper, pos.offset(-5, -3, -5), pos.offset(5, 16, 5));

        helper.getLevel().setBlockAndUpdate(pos, GCYMMachines.LARGE_DISTILLERY.getBlock().defaultBlockState());
        DistillationTowerMachine controller = (DistillationTowerMachine) MetaMachine.getMachine(helper.getLevel(), pos);
        helper.assertTrue(controller != null, "Expected DistillationTowerMachine controller for Large Distillery");
        controller.setFrontFacing(Direction.NORTH);

        FakePlayer player = createTestPlayer(helper, GameType.SURVIVAL);
        player.getInventory().add(new ItemStack(GCYMBlocks.CASING_WATERTIGHT.get(), 45));
        player.getInventory().add(GTMachines.ENERGY_INPUT_HATCH[GTValues.IV].asStack());
        player.getInventory().add(GTMachines.FLUID_IMPORT_HATCH[GTValues.IV].asStack());
        player.getInventory().add(GTMachines.MAINTENANCE_HATCH.asStack());
        player.getInventory().add(GTMachines.FLUID_EXPORT_HATCH[GTValues.IV].asStack());
        player.getInventory().add(new ItemStack(GTBlocks.CASING_STEEL_PIPE.get(), 1));
        player.getInventory().add(GTMachines.MUFFLER_HATCH[GTValues.IV].asStack());

        controller.getPattern().autoBuild(player, controller.getMultiblockState());

        boolean matched = controller.checkPatternWithLock();
        helper.assertTrue(matched,
                "Survival auto-built Large Distillery failed BlockPattern check: " +
                        (controller.getMultiblockState().hasError() ?
                                controller.getMultiblockState().error.getErrorInfo().getString() : "unknown"));
        controller.onStructureFormed();
        helper.assertTrue(controller.isFormed(),
                "Survival auto-built Large Distillery must form cleanly");
        helper.assertTrue(controller.getFluidOutputs() != null && controller.getFluidOutputs().size() == 1 &&
                controller.getFirstValid() != null,
                "Expected 1 valid fluid output on survival auto-built Large Distillery");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "DistillationAutoBuild_largeManualAndLayerLimits")
    public static void largeDistilleryAllowsSkippedLayersAndRejectsDuplicateLayerExport(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 20, 2));
        clearRegion(helper, pos.offset(-5, -3, -5), pos.offset(5, 16, 5));

        helper.getLevel().setBlockAndUpdate(pos, GCYMMachines.LARGE_DISTILLERY.getBlock().defaultBlockState());
        DistillationTowerMachine controller = (DistillationTowerMachine) MetaMachine.getMachine(helper.getLevel(), pos);
        helper.assertTrue(controller != null, "Expected DistillationTowerMachine controller for Large Distillery");
        controller.setFrontFacing(Direction.NORTH);

        // Start from auto-built 1-layer tower, then expand to 3 repeatable X-layers (y+1, y+2, y+3) + cap at y+4.
        // Controller faces NORTH: RIGHT = WEST (-X), BACK = SOUTH (+Z), UP = +Y.
        // Controller is at local (x=0, y=0, z=0) -> world (0, 0, 0); pipe P is at world (0, dy, 2).
        FakePlayer player = createTestPlayer(helper, GameType.CREATIVE);
        controller.getPattern().autoBuild(player, controller.getMultiblockState());

        var casing = GCYMBlocks.CASING_WATERTIGHT.getDefaultState();
        var pipe = GTBlocks.CASING_STEEL_PIPE.getDefaultState();
        var air = Blocks.AIR.defaultBlockState();
        // Clear old cap (y+2) and rebuild X-layers at dy = 1, 2, 3 with 0 hatches first.
        clearRegion(helper, pos.offset(-3, 1, -1), pos.offset(3, 5, 5));
        for (int dy = 1; dy <= 3; dy++) {
            helper.getLevel().setBlockAndUpdate(pos.offset(0, dy, 0), casing);
            helper.getLevel().setBlockAndUpdate(pos.offset(-1, dy, 1), casing);
            helper.getLevel().setBlockAndUpdate(pos.offset(1, dy, 1), casing);
            helper.getLevel().setBlockAndUpdate(pos.offset(-2, dy, 2), casing);
            helper.getLevel().setBlockAndUpdate(pos.offset(0, dy, 2), pipe);
            helper.getLevel().setBlockAndUpdate(pos.offset(2, dy, 2), casing);
            helper.getLevel().setBlockAndUpdate(pos.offset(-1, dy, 3), casing);
            helper.getLevel().setBlockAndUpdate(pos.offset(1, dy, 3), casing);
            helper.getLevel().setBlockAndUpdate(pos.offset(0, dy, 4), casing);
        }
        // Top cap at dy = 4: #ZZZ#, #ZCZ#, #ZZZ# at dz = 1..3, dx = -1..1
        for (int dz = 1; dz <= 3; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                helper.getLevel().setBlockAndUpdate(pos.offset(dx, 4, dz),
                        (dx == 0 && dz == 2) ? GTMachines.MUFFLER_HATCH[GTValues.IV].getBlock().defaultBlockState() :
                                casing);
            }
        }

        // 0 fluid export hatches must be rejected cleanly.
        helper.assertFalse(controller.checkPatternWithLock(),
                "Large Distillery with 0 fluid export hatches must fail pattern validation");

        // Place only a single fluid export hatch on the top repeatable layer (dy = 3), skipping dy = 1 and dy = 2.
        BlockPos layer3ExportPos = pos.offset(0, 3, 0);
        if (GTCEu.Mods.isAE2Loaded()) {
            helper.getLevel().setBlockAndUpdate(layer3ExportPos,
                    GTAEMachines.FLUID_EXPORT_HATCH_ME.getBlock().defaultBlockState());
        } else {
            helper.getLevel().setBlockAndUpdate(layer3ExportPos,
                    GTMachines.FLUID_EXPORT_HATCH[GTValues.IV].getBlock().defaultBlockState());
        }

        helper.assertTrue(controller.checkPatternWithLock(),
                "Large Distillery with skipped lower export layers (and ME export when AE2 loaded) must match pattern");
        controller.onStructureFormed();
        helper.assertTrue(controller.isFormed(),
                "Large Distillery with skipped lower export layers must form cleanly");
        helper.assertTrue(controller.getFluidOutputs() != null && controller.getFluidOutputs().size() == 3 &&
                controller.getFirstValid() != null,
                "Expected 3 fluid output slots (2 voided + 1 valid) for export on layer 3");

        // Adding a second export hatch (regular 1x alongside ME, or second regular 1x) on the SAME layer dy = 3
        // must be rejected by maxLayerLimited(1).
        controller.onStructureInvalid();
        BlockPos sameLayerSecondExport = pos.offset(0, 3, 4);
        helper.getLevel().setBlockAndUpdate(sameLayerSecondExport,
                GTMachines.FLUID_EXPORT_HATCH[GTValues.IV].getBlock().defaultBlockState());
        helper.assertFalse(controller.checkPatternWithLock(),
                "Large Distillery with 2 fluid export hatches on the same layer must fail maxLayerLimited(1)");

        // Restore casing so cleanup is tidy.
        helper.getLevel().setBlockAndUpdate(sameLayerSecondExport, air);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "DistillationAutoBuild_towerCreative")
    public static void distillationTowerCreativeAutoBuildForms(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 20, 2));
        clearRegion(helper, pos.offset(-4, -2, -4), pos.offset(4, 14, 4));

        helper.getLevel().setBlockAndUpdate(pos, GTMultiMachines.DISTILLATION_TOWER.getBlock().defaultBlockState());
        DistillationTowerMachine controller = (DistillationTowerMachine) MetaMachine.getMachine(helper.getLevel(), pos);
        helper.assertTrue(controller != null, "Expected DistillationTowerMachine controller for Distillation Tower");
        controller.setFrontFacing(Direction.NORTH);

        FakePlayer player = createTestPlayer(helper, GameType.CREATIVE);
        controller.getPattern().autoBuild(player, controller.getMultiblockState());

        boolean matched = controller.checkPatternWithLock();
        helper.assertTrue(matched,
                "Auto-built Distillation Tower failed BlockPattern check: " +
                        (controller.getMultiblockState().hasError() ?
                                controller.getMultiblockState().error.getErrorInfo().getString() : "unknown"));
        controller.onStructureFormed();
        helper.assertTrue(controller.isFormed(),
                "Auto-built Distillation Tower must stay formed after DistillationTowerMachine.onStructureFormed()");
        helper.assertTrue(controller.getFluidOutputs() != null && controller.getFluidOutputs().size() == 2 &&
                controller.getFirstValid() != null,
                "Expected 2 fluid outputs on auto-built Distillation Tower, got " +
                        (controller.getFluidOutputs() == null ? "null" : controller.getFluidOutputs().size()));

        // When AE2 is loaded, placing an ME export hatch on the same layer (dy = 1) as the existing 1x export hatch
        // must fail maxLayerLimited(1) cleanly in BlockPattern.
        if (GTCEu.Mods.isAE2Loaded()) {
            controller.onStructureInvalid();
            // On dy = 1, autoBuild placed the 1x export hatch at (1, 1, 0) or (-1, 1, 0); back-center (0, 1, 2) is
            // guaranteed to be a casing.
            BlockPos backCenterLayer1 = pos.offset(0, 1, 2);
            helper.getLevel().setBlockAndUpdate(backCenterLayer1,
                    GTAEMachines.FLUID_EXPORT_HATCH_ME.getBlock().defaultBlockState());
            helper.assertFalse(controller.checkPatternWithLock(),
                    "Distillation Tower with both regular 1x and ME export hatch on the same layer must fail pattern");
        }
        helper.succeed();
    }

    private static FakePlayer createTestPlayer(GameTestHelper helper, GameType gameType) {
        FakePlayer player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "gtceu_distillery_test"));
        player.setGameMode(gameType);
        return player;
    }

    private static void clearRegion(GameTestHelper helper, BlockPos min, BlockPos max) {
        for (BlockPos clear : BlockPos.betweenClosed(min, max)) {
            helper.getLevel().setBlock(clear, Blocks.AIR.defaultBlockState(), 2);
        }
    }
}
