package com.reborn.bettergtae.utils;

import com.reborn.gtceu.GTCEu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RegistriesUtil {

    public static @NotNull String BlockId(Block block) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString();
    }

    public static Block getBlock(String string) {
        Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(string));
        if (block == null) {
            GTCEu.LOGGER.error("Block {} is null", string);
            return Blocks.AIR;
        }
        return block;
    }

    public static @NotNull String ItemId(Item item) {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).toString();
    }

    public static Item getItem(String string) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(string));
        if (item == null) {
            GTCEu.LOGGER.error("Item {} is null", string);
            return Items.AIR;
        }
        return item;
    }
}
