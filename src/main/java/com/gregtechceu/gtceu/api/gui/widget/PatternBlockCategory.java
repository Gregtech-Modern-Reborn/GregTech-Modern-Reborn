package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.common.block.BatteryBlock;
import com.gregtechceu.gtceu.common.block.CoilBlock;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

/**
 * Functional block categories and classification logic for the multiblock JEI/XEI pattern preview highlight UI.
 */
public enum PatternBlockCategory {

    CONTROLLER("gtceu.jei.multiblock_info.category.controller", 0xFFFF1744, 0.30f, 0.90f, 0.38f, 0.98f),
    ITEM_INPUT("gtceu.jei.multiblock_info.category.item_input", 0xFF00E676, 0.26f, 0.85f, 0.34f, 0.95f),
    ITEM_OUTPUT("gtceu.jei.multiblock_info.category.item_output", 0xFFFFAB40, 0.26f, 0.85f, 0.34f, 0.95f),
    FLUID_INPUT("gtceu.jei.multiblock_info.category.fluid_input", 0xFF2979FF, 0.26f, 0.85f, 0.34f, 0.95f),
    FLUID_OUTPUT("gtceu.jei.multiblock_info.category.fluid_output", 0xFF1DE9B6, 0.26f, 0.85f, 0.34f, 0.95f),
    ENERGY("gtceu.jei.multiblock_info.category.energy", 0xFFFFEA00, 0.26f, 0.85f, 0.34f, 0.95f),
    MAINTENANCE("gtceu.jei.multiblock_info.category.maintenance", 0xFF00E5FF, 0.26f, 0.85f, 0.34f, 0.95f),
    MUFFLER("gtceu.jei.multiblock_info.category.muffler", 0xFFD500F9, 0.26f, 0.85f, 0.34f, 0.95f),
    PARALLEL("gtceu.jei.multiblock_info.category.parallel", 0xFFFF4081, 0.26f, 0.85f, 0.34f, 0.95f),
    SPECIAL_HATCH("gtceu.jei.multiblock_info.category.special_hatch", 0xFF7C4DFF, 0.26f, 0.85f, 0.34f, 0.95f),
    FLEXIBLE_HATCH("gtceu.jei.multiblock_info.category.flexible_hatch", 0xFFAEEA00, 0.20f, 0.72f, 0.34f, 0.95f),
    COIL_TIERED("gtceu.jei.multiblock_info.category.coil_tiered", 0xFFFF6D00, 0.26f, 0.85f, 0.34f, 0.95f),
    GLASS_FRAME("gtceu.jei.multiblock_info.category.glass_frame", 0xFFB3E5FC, 0.22f, 0.75f, 0.34f, 0.95f),
    CASING("gtceu.jei.multiblock_info.category.casing", 0xFF90A4AE, 0.13f, 0.38f, 0.32f, 0.90f);

    private static final Map<PartAbility, PatternBlockCategory> ABILITY_TO_CATEGORY = buildAbilityCategoryMap();

    @Nullable
    private static Map<Block, EnumSet<PatternBlockCategory>> cachedHatchBlockCategories;
    @Nullable
    private static Set<Block> cachedTieredBlocks;

    private final String langKey;
    private final int argbColor;
    private final float allFaceAlpha;
    private final float allLineAlpha;
    private final float soloFaceAlpha;
    private final float soloLineAlpha;

    PatternBlockCategory(String langKey, int argbColor, float allFaceAlpha, float allLineAlpha,
                         float soloFaceAlpha, float soloLineAlpha) {
        this.langKey = langKey;
        this.argbColor = argbColor;
        this.allFaceAlpha = allFaceAlpha;
        this.allLineAlpha = allLineAlpha;
        this.soloFaceAlpha = soloFaceAlpha;
        this.soloLineAlpha = soloLineAlpha;
    }

    public @NotNull String getLangKey() {
        return langKey;
    }

    public int getArgbColor() {
        return argbColor;
    }

    public int getRgbColor() {
        return argbColor & 0x00FFFFFF;
    }

    public float getRed() {
        return ((argbColor >> 16) & 0xFF) / 255.0f;
    }

    public float getGreen() {
        return ((argbColor >> 8) & 0xFF) / 255.0f;
    }

    public float getBlue() {
        return (argbColor & 0xFF) / 255.0f;
    }

    public float getFaceAlpha(boolean solo) {
        return solo ? soloFaceAlpha : allFaceAlpha;
    }

    public float getLineAlpha(boolean solo) {
        return solo ? soloLineAlpha : allLineAlpha;
    }

    public static @Nullable PatternBlockCategory classify(@Nullable BlockState previewState,
                                                          @Nullable BlockEntity previewEntity,
                                                          @Nullable TraceabilityPredicate predicate,
                                                          @NotNull Map<TraceabilityPredicate, EnumSet<PatternBlockCategory>> predicateCache) {
        if (previewState == null || previewState.isAir()) {
            return null;
        }

        Block previewBlock = previewState.getBlock();
        if ((predicate != null && predicate.isController) || isControllerBlock(previewBlock)) {
            return CONTROLLER;
        }

        PatternBlockCategory previewHatch = classifyConcreteHatch(previewBlock, previewEntity);
        if (previewHatch != null) {
            return previewHatch;
        }

        if (predicate != null) {
            EnumSet<PatternBlockCategory> allowedHatches = predicateCache.computeIfAbsent(
                    predicate, PatternBlockCategory::extractAllowedHatchCategories);
            if (allowedHatches.size() == 1) {
                return allowedHatches.iterator().next();
            }
            if (allowedHatches.size() > 1) {
                return FLEXIBLE_HATCH;
            }
        }

        if (isCoilOrTieredBlock(previewBlock)) {
            return COIL_TIERED;
        }
        if (isGlassOrFrameBlock(previewState, previewBlock)) {
            return GLASS_FRAME;
        }
        return CASING;
    }

    private static boolean isControllerBlock(Block block) {
        return block instanceof MetaMachineBlock metaBlock &&
                metaBlock.definition instanceof MultiblockMachineDefinition;
    }

    private static @Nullable PatternBlockCategory classifyConcreteHatch(Block block, @Nullable BlockEntity blockEntity) {
        EnumSet<PatternBlockCategory> cats = getHatchBlockCategories().get(block);
        if (cats != null && !cats.isEmpty()) {
            if (cats.size() == 1) {
                return cats.iterator().next();
            }
            return FLEXIBLE_HATCH;
        }
        if (blockEntity instanceof IMachineBlockEntity holder && holder.getMetaMachine() instanceof IMultiPart) {
            return SPECIAL_HATCH;
        }
        if (block instanceof IMachineBlock && !isControllerBlock(block)) {
            return SPECIAL_HATCH;
        }
        return null;
    }

    private static EnumSet<PatternBlockCategory> extractAllowedHatchCategories(TraceabilityPredicate predicate) {
        EnumSet<PatternBlockCategory> allowed = EnumSet.noneOf(PatternBlockCategory.class);
        collectFromSimplePredicates(predicate.common, allowed);
        collectFromSimplePredicates(predicate.limited, allowed);
        return allowed;
    }

    private static void collectFromSimplePredicates(List<SimplePredicate> simplePredicates,
                                                    EnumSet<PatternBlockCategory> out) {
        if (simplePredicates == null || simplePredicates.isEmpty()) {
            return;
        }
        Map<Block, EnumSet<PatternBlockCategory>> hatchMap = getHatchBlockCategories();
        for (SimplePredicate simple : simplePredicates) {
            if (simple == null || simple.candidates == null) {
                continue;
            }
            BlockInfo[] infos = simple.candidates.get();
            if (infos == null || infos.length == 0) {
                continue;
            }
            EnumSet<PatternBlockCategory> singleMatches = EnumSet.noneOf(PatternBlockCategory.class);
            EnumSet<PatternBlockCategory> multiIntersection = null;
            boolean sawMultiAbilityHatch = false;
            boolean sawUnknownMachineHatch = false;

            for (BlockInfo info : infos) {
                if (info == null) continue;
                BlockState state = info.getBlockState();
                if (state == null || state.isAir()) continue;
                Block block = state.getBlock();
                if (isControllerBlock(block)) continue;

                EnumSet<PatternBlockCategory> blockCats = hatchMap.get(block);
                if (blockCats != null && !blockCats.isEmpty()) {
                    if (blockCats.size() == 1) {
                        singleMatches.addAll(blockCats);
                    } else {
                        sawMultiAbilityHatch = true;
                        if (multiIntersection == null) {
                            multiIntersection = EnumSet.copyOf(blockCats);
                        } else {
                            multiIntersection.retainAll(blockCats);
                        }
                    }
                } else if (block instanceof IMachineBlock) {
                    sawUnknownMachineHatch = true;
                }
            }

            if (!singleMatches.isEmpty()) {
                out.addAll(singleMatches);
            } else if (sawMultiAbilityHatch) {
                if (multiIntersection != null && multiIntersection.size() == 1) {
                    out.addAll(multiIntersection);
                } else {
                    out.add(FLEXIBLE_HATCH);
                }
            }
            if (sawUnknownMachineHatch) {
                out.add(SPECIAL_HATCH);
            }
        }
    }

    private static boolean isCoilOrTieredBlock(Block block) {
        if (block instanceof CoilBlock || block instanceof BatteryBlock) {
            return true;
        }
        if (getTieredBlocks().contains(block)) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        String path = key.getPath();
        return path.contains("coil") || path.contains("filter_casing") || path.contains("battery_empty_tier");
    }

    private static boolean isGlassOrFrameBlock(BlockState state, Block block) {
        if (block instanceof AbstractGlassBlock || state.is(Tags.Blocks.GLASS)) {
            return true;
        }
        if (block instanceof MaterialBlock materialBlock && materialBlock.tagPrefix == TagPrefix.frameGt) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        String path = key.getPath();
        return path.contains("glass") || path.endsWith("_frame") || path.contains("frame_box");
    }

    private static Map<Block, EnumSet<PatternBlockCategory>> getHatchBlockCategories() {
        Map<Block, EnumSet<PatternBlockCategory>> map = cachedHatchBlockCategories;
        if (map != null && !map.isEmpty()) {
            return map;
        }
        map = new HashMap<>();
        for (Map.Entry<PartAbility, PatternBlockCategory> entry : ABILITY_TO_CATEGORY.entrySet()) {
            PatternBlockCategory category = entry.getValue();
            for (Block block : entry.getKey().getAllBlocks()) {
                if (block != null) {
                    map.computeIfAbsent(block, b -> EnumSet.noneOf(PatternBlockCategory.class)).add(category);
                }
            }
        }
        cachedHatchBlockCategories = map;
        return map;
    }

    private static Set<Block> getTieredBlocks() {
        Set<Block> blocks = cachedTieredBlocks;
        if (blocks != null && !blocks.isEmpty()) {
            return blocks;
        }
        blocks = new HashSet<>();
        addSupplierBlocks(GTCEuAPI.HEATING_COILS.values(), blocks);
        addSupplierBlocks(GTCEuAPI.CLEANROOM_FILTERS.values(), blocks);
        addSupplierBlocks(GTCEuAPI.PSS_BATTERIES.values(), blocks);
        cachedTieredBlocks = blocks;
        return blocks;
    }

    private static void addSupplierBlocks(Collection<? extends Supplier<? extends Block>> suppliers, Set<Block> out) {
        for (Supplier<? extends Block> supplier : suppliers) {
            if (supplier == null) continue;
            Block block = supplier.get();
            if (block != null) {
                out.add(block);
            }
        }
    }

    private static Map<PartAbility, PatternBlockCategory> buildAbilityCategoryMap() {
        Map<PartAbility, PatternBlockCategory> map = new LinkedHashMap<>();

        map.put(PartAbility.IMPORT_ITEMS, ITEM_INPUT);
        map.put(PartAbility.STEAM_IMPORT_ITEMS, ITEM_INPUT);

        map.put(PartAbility.EXPORT_ITEMS, ITEM_OUTPUT);
        map.put(PartAbility.STEAM_EXPORT_ITEMS, ITEM_OUTPUT);

        map.put(PartAbility.IMPORT_FLUIDS, FLUID_INPUT);
        map.put(PartAbility.IMPORT_FLUIDS_1X, FLUID_INPUT);
        map.put(PartAbility.IMPORT_FLUIDS_4X, FLUID_INPUT);
        map.put(PartAbility.IMPORT_FLUIDS_9X, FLUID_INPUT);

        map.put(PartAbility.EXPORT_FLUIDS, FLUID_OUTPUT);
        map.put(PartAbility.EXPORT_FLUIDS_1X, FLUID_OUTPUT);
        map.put(PartAbility.EXPORT_FLUIDS_4X, FLUID_OUTPUT);
        map.put(PartAbility.EXPORT_FLUIDS_9X, FLUID_OUTPUT);

        map.put(PartAbility.INPUT_ENERGY, ENERGY);
        map.put(PartAbility.OUTPUT_ENERGY, ENERGY);
        map.put(PartAbility.SUBSTATION_INPUT_ENERGY, ENERGY);
        map.put(PartAbility.SUBSTATION_OUTPUT_ENERGY, ENERGY);
        map.put(PartAbility.INPUT_LASER, ENERGY);
        map.put(PartAbility.OUTPUT_LASER, ENERGY);
        map.put(PartAbility.STEAM, ENERGY);

        map.put(PartAbility.MAINTENANCE, MAINTENANCE);
        map.put(PartAbility.MUFFLER, MUFFLER);

        map.put(PartAbility.PARALLEL_HATCH, PARALLEL);
        map.put(PartAbility.MULTI_PARALLEL_HATCH, PARALLEL);

        map.put(PartAbility.ROTOR_HOLDER, SPECIAL_HATCH);
        map.put(PartAbility.PUMP_FLUID_HATCH, SPECIAL_HATCH);
        map.put(PartAbility.TANK_VALVE, SPECIAL_HATCH);
        map.put(PartAbility.PASSTHROUGH_HATCH, SPECIAL_HATCH);
        map.put(PartAbility.COMPUTATION_DATA_RECEPTION, SPECIAL_HATCH);
        map.put(PartAbility.COMPUTATION_DATA_TRANSMISSION, SPECIAL_HATCH);
        map.put(PartAbility.OPTICAL_DATA_RECEPTION, SPECIAL_HATCH);
        map.put(PartAbility.OPTICAL_DATA_TRANSMISSION, SPECIAL_HATCH);
        map.put(PartAbility.DATA_ACCESS, SPECIAL_HATCH);
        map.put(PartAbility.HPCA_COMPONENT, SPECIAL_HATCH);
        map.put(PartAbility.OBJECT_HOLDER, SPECIAL_HATCH);

        return Collections.unmodifiableMap(map);
    }
}
