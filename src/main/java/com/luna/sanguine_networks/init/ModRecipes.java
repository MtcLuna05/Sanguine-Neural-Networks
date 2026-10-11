package com.luna.sanguine_networks.init;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.recipe.CatalystRecipe;
import com.luna.sanguine_networks.recipe.ModelRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@net.neoforged.fml.common.EventBusSubscriber(modid = SanguineNeuralNetworks.MODID)
public class ModRecipes {
	private static net.minecraft.world.item.crafting.RecipeMap clientRecipes = net.minecraft.world.item.crafting.RecipeMap.EMPTY;

	public static void setClientRecipes(net.minecraft.world.item.crafting.RecipeMap recipes) {
		clientRecipes = recipes;
	}

	public static <I extends net.minecraft.world.item.crafting.RecipeInput, T extends net.minecraft.world.item.crafting.Recipe<I>> java.util.Collection<net.minecraft.world.item.crafting.RecipeHolder<T>> getRecipes(
			net.minecraft.world.level.Level level, net.minecraft.world.item.crafting.RecipeType<T> type) {
		var recipes = level instanceof net.minecraft.server.level.ServerLevel server ? server.recipeAccess().recipeMap() : clientRecipes;
		return recipes.byType(type);
	}

	@net.neoforged.bus.api.SubscribeEvent
	public static void syncRecipes(net.neoforged.neoforge.event.OnDatapackSyncEvent event) {
		event.sendRecipes(ModelRecipe.Type.INSTANCE, CatalystRecipe.Type.INSTANCE);
	}

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
