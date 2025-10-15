package com.gregtechceu.gtceu.common.data.machines;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MultiParallelHatchPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.GTValues.UV;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.registerTieredMachines;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableTieredHullMachineModel;

public class GTMRMachines {

    public static final MachineDefinition[] MULTI_PARALLEL_HATCH = registerTieredMachines("multi_parallel_hatch",
            MultiParallelHatchPartMachine::new,
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " MultiParallel Control Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.MULTI_PARALLEL_HATCH)
                    .modelProperty(IS_FORMED, false)
                    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                    .model(createWorkableTieredHullMachineModel(
                            GTValues.getResourceLocation(tier))
                            .andThen((ctx, prov, model) -> {
                                model.addReplaceableTextures("bottom", "top", "side");
                            }))
                    .tooltips(Component.translatable("gtceu.machine.multi_parallel_hatch_mk" + tier + ".tooltip"),
                            Component.translatable("gtceu.part_sharing.disabled"))
                    .register(),
            ConfigHolder.INSTANCE.machines.highTierContent ? HighTiersArray : LowTiersArray);

    public static void init() {}
}
