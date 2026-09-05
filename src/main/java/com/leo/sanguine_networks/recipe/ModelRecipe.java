package com.leo.sanguine_networks.recipe;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.init.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class ModelRecipe implements Recipe<RecipeInput> {
    private final ResourceLocation entity;
    private final int[] blood;
    private final int energy;

    public ModelRecipe(ResourceLocation entity, int[] blood, int energy) {
        this.entity = entity;
        this.blood = blood;
        this.energy = energy;
    }
    public ResourceLocation getEntity() { return entity; }
    public int[] getBlood() { return blood; }
    public int getBlood(dev.shadowsoffire.hostilenetworks.data.ModelTier tier) {
        int index = dev.shadowsoffire.hostilenetworks.data.ModelTierRegistry.getSortedTiers().indexOf(tier);
        return blood[Math.clamp(index, 0, blood.length - 1)];
    }
    /** Extra HNN tiers continue above the last JSON tier, doubling each preceding increase. */
    public int getExtraBlood(int extraTier) {
        if (extraTier < 0) throw new IllegalArgumentException("Extra tier must be non-negative");
        long previous = blood[blood.length - 2];
        long current = blood[blood.length - 1];
        for (int i = 0; i <= extraTier; i++) {
            long next = Math.clamp(current + 2 * (current - previous), 0, Integer.MAX_VALUE);
            previous = current;
            current = next;
            if (current == previous || current == Integer.MAX_VALUE) break;
        }
        return (int) current;
    }
    public int getEnergy() { return energy; }

    public static Builder create(ResourceLocation entity, int[] blood, int energy) {
        return new Builder(new ModelRecipe(entity, blood, energy), ResourceLocation.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "blood/" + entity.getPath()));
    }
    public record Builder(ModelRecipe recipe, ResourceLocation id) {
        public void save(RecipeOutput output) { output.accept(id, recipe, null); }
    }
    @Override public boolean matches(RecipeInput input, Level level) { return false; }
    @Override public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.BLOOD_RECIPE_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return Type.INSTANCE; }

    public static class Type implements RecipeType<ModelRecipe> {
        public static final Type INSTANCE = new Type();
    }
    public static class Serializer implements RecipeSerializer<ModelRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        private static final MapCodec<ModelRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("entity").forGetter(ModelRecipe::getEntity),
            Codec.INT.listOf().validate(values -> values.size() == 5 && values.stream().allMatch(v -> v >= 0)
                ? DataResult.success(values) : DataResult.error(() -> "blood must contain five non-negative tier amounts"))
                .xmap(values -> values.stream().mapToInt(Integer::intValue).toArray(), values -> java.util.Arrays.stream(values).boxed().toList())
                .fieldOf("blood").forGetter(ModelRecipe::getBlood),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("energy").forGetter(ModelRecipe::getEnergy)
        ).apply(instance, ModelRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, ModelRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override public ModelRecipe decode(RegistryFriendlyByteBuf buffer) { return new ModelRecipe(buffer.readResourceLocation(), buffer.readVarIntArray(5), buffer.readInt()); }
            @Override public void encode(RegistryFriendlyByteBuf buffer, ModelRecipe recipe) { buffer.writeResourceLocation(recipe.entity); buffer.writeVarIntArray(recipe.blood); buffer.writeInt(recipe.energy); }
        };
        @Override public MapCodec<ModelRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, ModelRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
