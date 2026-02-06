package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public interface IAddInformation extends IItemComponent {

    void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                         TooltipFlag isAdvanced);
}
