package com.reborn.gtceu.common.recipe.condition;

import com.reborn.gtceu.api.machine.trait.RecipeLogic;
import com.reborn.gtceu.api.recipe.GTRecipe;
import com.reborn.gtceu.api.recipe.RecipeCondition;
import com.reborn.gtceu.api.recipe.condition.RecipeConditionType;
import com.reborn.gtceu.common.data.GTRecipeConditions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class ThunderCondition extends RecipeCondition {

    public static final Codec<ThunderCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(Codec.FLOAT.fieldOf("level").forGetter(val -> val.level))
                    .apply(instance, ThunderCondition::new));

    public final static ThunderCondition INSTANCE = new ThunderCondition();
    private float level;

    public ThunderCondition(boolean isReverse, float level) {
        super(isReverse);
        this.level = level;
    }

    public ThunderCondition(float level) {
        this.level = level;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.THUNDER;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.thunder.tooltip", level);
    }

    public float getLevel() {
        return level;
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.machine.self().getLevel();
        return level != null && level.getThunderLevel(1) >= this.level;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new ThunderCondition();
    }
}
