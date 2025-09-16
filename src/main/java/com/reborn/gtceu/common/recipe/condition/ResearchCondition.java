package com.reborn.gtceu.common.recipe.condition;

import com.reborn.gtceu.api.machine.trait.RecipeLogic;
import com.reborn.gtceu.api.recipe.GTRecipe;
import com.reborn.gtceu.api.recipe.RecipeCondition;
import com.reborn.gtceu.api.recipe.ResearchData;
import com.reborn.gtceu.api.recipe.condition.RecipeConditionType;
import com.reborn.gtceu.common.data.GTRecipeConditions;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ResearchCondition extends RecipeCondition {

    public static final Codec<ResearchCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(ResearchData.CODEC.fieldOf("research").forGetter(val -> val.data))
                    .apply(instance, ResearchCondition::new));
    public static final ResearchCondition INSTANCE = new ResearchCondition();
    public ResearchData data;

    public ResearchCondition() {
        this.data = new ResearchData();
    }

    public ResearchCondition(boolean isReverse, ResearchData data) {
        super(isReverse);
        this.data = data;
    }

    @Override
    public RecipeConditionType<ResearchCondition> getType() {
        return GTRecipeConditions.RESEARCH;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("gtceu.recipe.research");
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        return true;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new ResearchCondition();
    }
}
