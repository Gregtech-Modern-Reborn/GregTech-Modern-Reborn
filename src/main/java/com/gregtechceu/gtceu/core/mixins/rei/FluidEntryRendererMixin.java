package com.gregtechceu.gtceu.core.mixins.rei;

import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.utils.GTMath;

import net.neoforged.neoforge.fluids.FluidStack;

import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.plugin.client.entry.FluidEntryDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidEntryDefinition.FluidEntryRenderer.class, remap = false)
public class FluidEntryRendererMixin {

    @Inject(method = "getTooltip",
            at = @At(value = "TAIL"),
            remap = false,
            require = 0)
    private void gtceu$addMaterialTooltip(EntryStack<dev.architectury.fluid.FluidStack> entry, TooltipContext context,
                                          CallbackInfoReturnable<Tooltip> cir) {
        dev.architectury.fluid.FluidStack stack = entry.getValue();
        TooltipsHandler.appendFluidTooltips(new FluidStack(
                stack.getFluid().builtInRegistryHolder(),
                GTMath.saturatedCast(stack.getAmount()),
                stack.getPatch()),
                cir.getReturnValue()::add,
                context.getFlag(),
                context.vanillaContext());
    }
}
