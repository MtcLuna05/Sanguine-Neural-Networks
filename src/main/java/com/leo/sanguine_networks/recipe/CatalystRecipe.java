package com.leo.sanguine_networks.recipe;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.init.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
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
        return new Builder(new CatalystRecipe(input, uses, multiplier), ResourceLocation.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "catalyst/" + BuiltInRegistries.ITEM.getKey(input.getItems()[0].getItem()).getPath()));
    }
    public record Builder(CatalystRecipe recipe, ResourceLocation id) {
        public void save(RecipeOutput output) { output.accept(id, recipe, null); }
    }
    @Override public boolean matches(RecipeInput input, Level level) { return false; }
    @Override public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.CATALYST_RECIPE_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return Type.INSTANCE; }

    public static class Type implements RecipeType<CatalystRecipe> {
        public static final Type INSTANCE = new Type();
    }
    public static class Serializer implements RecipeSerializer<CatalystRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        private static final MapCodec<CatalystRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(CatalystRecipe::getInput),
            Codec.intRange(-1, Integer.MAX_VALUE).fieldOf("uses").forGetter(CatalystRecipe::getUses),
            Codec.floatRange(0, Float.MAX_VALUE).fieldOf("multiplier").forGetter(CatalystRecipe::getMultiplier)
        ).apply(instance, CatalystRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, CatalystRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override public CatalystRecipe decode(RegistryFriendlyByteBuf buffer) { return new CatalystRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), buffer.readInt(), buffer.readFloat()); }
            @Override public void encode(RegistryFriendlyByteBuf buffer, CatalystRecipe recipe) { Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input); buffer.writeInt(recipe.uses); buffer.writeFloat(recipe.multiplier); }
        };
        @Override public MapCodec<CatalystRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, CatalystRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
