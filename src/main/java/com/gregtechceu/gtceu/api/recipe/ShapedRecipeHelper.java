package com.gregtechceu.gtceu.api.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

public final class ShapedRecipeHelper {

    private static final Method KEY_FROM_JSON = find(Map.class, JsonObject.class);
    private static final Method PATTERN_FROM_JSON = find(String[].class, JsonArray.class);
    private static final Method DISSOLVE_PATTERN = find(
            NonNullList.class, String[].class, Map.class, int.class, int.class);
    private static final Method SHRINK = find(String[].class, String[].class);

    private ShapedRecipeHelper() {}

    @SuppressWarnings("unchecked")
    public static Map<String, Ingredient> keyFromJson(JsonObject keyEntry) {
        return (Map<String, Ingredient>) invoke(KEY_FROM_JSON, keyEntry);
    }

    public static String[] patternFromJson(JsonArray patternArray) {
        return (String[]) invoke(PATTERN_FROM_JSON, patternArray);
    }

    @SuppressWarnings("unchecked")
    public static NonNullList<Ingredient> dissolvePattern(
                                                          String[] pattern, Map<String, Ingredient> keys,
                                                          int patternWidth, int patternHeight) {
        return (NonNullList<Ingredient>) invoke(DISSOLVE_PATTERN, pattern, keys, patternWidth, patternHeight);
    }

    public static String[] shrink(String... toShrink) {
        return (String[]) invoke(SHRINK, (Object) toShrink);
    }

    private static Method find(Class<?> returnType, Class<?>... parameterTypes) {
        for (Method method : ShapedRecipe.class.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) && method.getReturnType() == returnType &&
                    Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalStateException("Could not find ShapedRecipe helper method with return type " + returnType);
    }

    private static Object invoke(Method method, Object... args) {
        try {
            return method.invoke(null, args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
