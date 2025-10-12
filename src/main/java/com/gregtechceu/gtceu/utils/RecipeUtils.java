package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import java.util.*;

public class RecipeUtils {

    /**
     * 合并两个 Map<RecipeCapability<?>, List<Content>>。
     * 原 Map 会被修改，内容安全追加。
     *
     * @param target 目标 Map（被合并）
     * @param source 来源 Map（内容追加到目标）
     */
    public static void mergeMapOfLists(
                                       Map<RecipeCapability<?>, List<Content>> target,
                                       Map<RecipeCapability<?>, List<Content>> source) {
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : source.entrySet()) {
            RecipeCapability<?> key = entry.getKey();
            List<Content> valueToAdd = entry.getValue();
            if (valueToAdd == null) continue;

            // 获取目标 Map 对应的 List
            List<Content> targetList = target.get(key);

            if (targetList == null) {
                // key 不存在，创建可变列表
                target.put(key, new ArrayList<>(valueToAdd));
            } else {
                // key 已存在，保证可变
                if (!(targetList instanceof ArrayList)) {
                    targetList = new ArrayList<>(targetList);
                    target.put(key, targetList);
                }
                // 合并内容
                targetList.addAll(valueToAdd);
            }
        }
    }

    public static Map<RecipeCapability<?>, ChanceLogic> mergeChanceLogicMap(
                                                                            Map<RecipeCapability<?>, ChanceLogic> target,
                                                                            Map<RecipeCapability<?>, ChanceLogic> source) {
        // 确保 target 可变
        Map<RecipeCapability<?>, ChanceLogic> mutableTarget;
        if (target instanceof HashMap) {
            mutableTarget = target;
        } else {
            mutableTarget = new HashMap<>(target);
        }

        // 合并 source
        if (source != null) {
            mutableTarget.putAll(source); // key 相同会覆盖
        }

        return mutableTarget;
    }

    public static void RemoveAllInputNoConsume(GTRecipe recipe) {
        recipe.inputs.forEach((key, values) -> {
            values.removeIf(value -> value.chance == 0);
        });
    }
}
