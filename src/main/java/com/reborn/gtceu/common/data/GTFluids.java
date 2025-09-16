package com.reborn.gtceu.common.data;

import com.reborn.gtceu.GTCEu;
import com.reborn.gtceu.api.GTCEuAPI;
import com.reborn.gtceu.api.data.chemical.material.Material;
import com.reborn.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.reborn.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.reborn.gtceu.api.fluids.store.FluidStorageKeys;
import com.reborn.gtceu.api.registry.registrate.GTRegistrate;
import com.reborn.gtceu.common.fluid.potion.PotionFluid;
import com.reborn.gtceu.data.recipe.CustomTags;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeMod;

import com.tterrag.registrate.util.entry.FluidEntry;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static com.reborn.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTFluids {

    public static final FluidEntry<PotionFluid> POTION = REGISTRATE
            .fluid("potion", GTCEu.id("block/fluids/fluid.potion"), GTCEu.id("block/fluids/fluid.potion"),
                    PotionFluid.PotionFluidType::new, PotionFluid::new)
            .lang("Potion")
            .source(PotionFluid::new).noBlock().noBucket()
            .tag(CustomTags.POTION_FLUIDS)
            .register();

    public static void init() {
        // Register fluids for non-materials
        handleNonMaterialFluids(GTMaterials.Water, Fluids.WATER);
        handleNonMaterialFluids(GTMaterials.Lava, Fluids.LAVA);
        handleNonMaterialFluids(GTMaterials.Milk, ForgeMod.MILK);
        ForgeMod.enableMilkFluid();

        // register fluids for materials
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_FLUID);
        for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
            GTRegistrate registrate = registry.getRegistrate();
            for (var material : registry.getAllMaterials()) {
                var fluidProperty = material.getProperty(PropertyKey.FLUID);

                if (fluidProperty != null) {
                    fluidProperty.registerFluids(material, registrate);
                }
            }
        }
    }

    public static void handleNonMaterialFluids(@NotNull Material material, @NotNull Fluid fluid) {
        var property = material.getProperty(PropertyKey.FLUID);
        property.getStorage().store(FluidStorageKeys.LIQUID, () -> fluid, null);
    }

    public static void handleNonMaterialFluids(@NotNull Material material, @NotNull Supplier<Fluid> fluid) {
        var property = material.getProperty(PropertyKey.FLUID);
        property.getStorage().store(FluidStorageKeys.LIQUID, fluid, null);
    }
}
