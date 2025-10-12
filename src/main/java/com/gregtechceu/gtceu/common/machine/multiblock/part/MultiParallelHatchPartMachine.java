package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.IMultiParallelHatch;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;

public class MultiParallelHatchPartMachine extends TieredPartMachine implements IFancyUIMachine, IMultiParallelHatch {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MultiParallelHatchPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);
    private static final int minMultiParallel = 1;

    private final int maxMultiParallel;

    @Persisted
    private int currentMultiParallel = 1;

    public MultiParallelHatchPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.maxMultiParallel = processTierToMaxMultiParallel(tier);
    }

    public void setCurrentMultiParallel(int parallelAmount) {
        this.currentMultiParallel = Mth.clamp(parallelAmount, minMultiParallel, this.maxMultiParallel);
        for (IMultiController controller : this.getControllers()) {
            if (controller instanceof IRecipeLogicMachine rlm) {
                rlm.getRecipeLogic().markLastRecipeDirty();
                rlm.getRecipeLogic().setMultiParallelLogic(true);
                rlm.getRecipeLogic().setMultiParallelCount(this.currentMultiParallel);
            }
        }
    }

    public Widget createUIWidget() {
        WidgetGroup parallelAmountGroup = new WidgetGroup(0, 0, 100, 20);
        parallelAmountGroup.addWidget(new IntInputWidget(this::getCurrentMultiParallel, this::setCurrentMultiParallel)
                .setMin(minMultiParallel)
                .setMax(maxMultiParallel));

        return parallelAmountGroup;
    }

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public int getCurrentMultiParallel() {
        return this.currentMultiParallel;
    }

    public static int processTierToMaxMultiParallel(int tier) {
        if (ConfigHolder.INSTANCE.machines.multiParallelHatchSettings) {
            return Math.max(2, tier * 8);
        } else {
            return (1 << (tier + 1));
        }
    }
}
