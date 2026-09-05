package com.leo.sanguine_networks.init;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.recipe.CatalystRecipe;
import com.leo.sanguine_networks.recipe.ModelRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModRecipes {
    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeType<?>> TYPES =
        DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_TYPE, SanguineNeuralNetworks.MODID);
    static {
        TYPES.register("catalyst", () -> CatalystRecipe.Type.INSTANCE);
        TYPES.register("blood", () -> ModelRecipe.Type.INSTANCE);
    }

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
        DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, SanguineNeuralNetworks.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CatalystRecipe>> CATALYST_RECIPE_SERIALIZER =
        SERIALIZERS.register("catalyst", () -> CatalystRecipe.Serializer.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ModelRecipe>> BLOOD_RECIPE_SERIALIZER =
        SERIALIZERS.register("blood", () -> ModelRecipe.Serializer.INSTANCE);
}
