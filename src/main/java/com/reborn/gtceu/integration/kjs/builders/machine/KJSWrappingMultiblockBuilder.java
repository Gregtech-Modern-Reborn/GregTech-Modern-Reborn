package com.reborn.gtceu.integration.kjs.builders.machine;

import com.reborn.gtceu.api.block.MetaMachineBlock;
import com.reborn.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.reborn.gtceu.api.item.MetaMachineItem;
import com.reborn.gtceu.api.machine.MultiblockMachineDefinition;
import com.reborn.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.reborn.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.reborn.gtceu.api.registry.registrate.BuilderBase;
import com.reborn.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.reborn.gtceu.common.registry.GTRegistration;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.generator.DataJsonGenerator;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class KJSWrappingMultiblockBuilder extends BuilderBase<MultiblockMachineDefinition> {

    @HideFromJS
    @Getter
    private final KJSTieredMultiblockBuilder tieredBuilder;

    public KJSWrappingMultiblockBuilder(ResourceLocation id, KJSTieredMultiblockBuilder tieredBuilder) {
        super(id);
        this.tieredBuilder = tieredBuilder;
    }

    public KJSWrappingMultiblockBuilder tiers(int... tiers) {
        tieredBuilder.tiers(tiers);
        return this;
    }

    public KJSWrappingMultiblockBuilder machine(KJSTieredMultiblockBuilder.TieredCreationFunction machine) {
        tieredBuilder.machine(machine);
        return this;
    }

    public KJSWrappingMultiblockBuilder definition(KJSTieredMultiblockBuilder.DefinitionFunction definition) {
        tieredBuilder.definition(definition);
        return this;
    }

    @Override
    public void generateDataJsons(DataJsonGenerator generator) {
        tieredBuilder.generateDataJsons(generator);
    }

    @Override
    public void generateAssetJsons(@Nullable AssetJsonGenerator generator) {
        tieredBuilder.generateAssetJsons(generator);
    }

    @Override
    public void generateLang(LangEventJS lang) {
        tieredBuilder.generateLang(lang);
    }

    @Override
    public MultiblockMachineDefinition register() {
        tieredBuilder.register();
        for (var def : tieredBuilder.get()) {
            if (def != null) {
                return value = def;
            }
        }
        // should never happen.
        throw new IllegalStateException("Empty tiered multiblock builder " + Arrays.toString(tieredBuilder.get()) +
                " With id " + tieredBuilder.id);
    }

    public static MultiblockMachineBuilder createKJSMulti(ResourceLocation id) {
        return new MultiblockMachineBuilder(GTRegistration.REGISTRATE, id.getPath(),
                WorkableElectricMultiblockMachine::new,
                MetaMachineBlock::new,
                MetaMachineItem::new,
                MetaMachineBlockEntity::new);
    }

    public static MultiblockMachineBuilder createKJSMulti(ResourceLocation id,
                                                          KJSTieredMachineBuilder.CreationFunction<? extends MultiblockControllerMachine> machine) {
        return new MultiblockMachineBuilder(GTRegistration.REGISTRATE, id.getPath(),
                machine::create,
                MetaMachineBlock::new,
                MetaMachineItem::new,
                MetaMachineBlockEntity::new);
    }
}
