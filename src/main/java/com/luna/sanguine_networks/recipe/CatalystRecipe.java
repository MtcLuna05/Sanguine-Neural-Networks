package com.luna.sanguine_networks.recipe;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.init.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class CatalystRecipe implements Recipe<RecipeInput> {
    private final Ingredient input;
    private final int uses;
    private final float multiplier;

    public CatalystRecipe(Ingredient input, int uses, float multiplier) {
        this.input = input;
        this.uses = uses;
        this.multiplier = multiplier;
    }
    public Ingredient getInput() { return input; }
    public int getUses() { return uses; }
    public float getMultiplier() { return multiplier; }

    public static Builder create(Ingredient input, int uses, float multiplier) {
	    return new Builder(new CatalystRecipe(input, uses, multiplier), Identifier.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "catalyst/" + BuiltInRegistries.ITEM.getKey(input.getValues().get(0).value()).getPath()));
    }

	public record Builder(CatalystRecipe recipe, Identifier id) {
		public void save(RecipeOutput output) {
			output.accept(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, id), recipe, null);
		}
    }
    @Override public boolean matches(RecipeInput input, Level level) { return false; }

	@Override
	public ItemStack assemble(RecipeInput input) {
		return ItemStack.EMPTY;
	}
    @Override public boolean isSpecial() { return true; }

	@Override
	public boolean showNotification() {
		return false;
	}

	@Override
	public String group() {
		return "";
	}

	@Override
	public net.minecraft.world.item.crafting.PlacementInfo placementInfo() {
		return net.minecraft.world.item.crafting.PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public net.minecraft.world.item.crafting.RecipeBookCategory recipeBookCategory() {
		return net.minecraft.world.item.crafting.RecipeBookCategories.CRAFTING_MISC;
	}

	@Override
	public RecipeSerializer<CatalystRecipe> getSerializer() {
		return ModRecipes.CATALYST_RECIPE_SERIALIZER.get();
	}

	@Override
	public RecipeType<CatalystRecipe> getType() {
		return Type.INSTANCE;
	}

    public static class Type implements RecipeType<CatalystRecipe> {
        public static final Type INSTANCE = new Type();
    }

	public static class Serializer {
        private static final MapCodec<CatalystRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		        Ingredient.CODEC.fieldOf("catalyst").forGetter(CatalystRecipe::getInput),
            Codec.intRange(-1, Integer.MAX_VALUE).fieldOf("uses").forGetter(CatalystRecipe::getUses),
            Codec.floatRange(0, Float.MAX_VALUE).fieldOf("multiplier").forGetter(CatalystRecipe::getMultiplier)
        ).apply(instance, CatalystRecipe::new));
		public static final RecipeSerializer<CatalystRecipe> INSTANCE = new RecipeSerializer<>(CODEC,
				net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(CODEC.codec()));
    }
}
