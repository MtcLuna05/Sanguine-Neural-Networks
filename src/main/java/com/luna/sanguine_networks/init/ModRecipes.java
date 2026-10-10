package com.luna.sanguine_networks.init;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.recipe.CatalystRecipe;
import com.luna.sanguine_networks.recipe.ModelRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SanguineNeuralNetworks.MODID);

    public static final RegistryObject<RecipeSerializer<CatalystRecipe>> CATALYST_RECIPE_SERIALIZER =
        SERIALIZERS.register("catalyst", () -> CatalystRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<ModelRecipe>> BLOOD_RECIPE_SERIALIZER =
        SERIALIZERS.register("blood", () -> ModelRecipe.Serializer.INSTANCE);
}
