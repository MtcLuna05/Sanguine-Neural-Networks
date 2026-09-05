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
    private final ResourceLocation block;
    private final int[] blood;
    private final int energy;

    public ModelRecipe(ResourceLocation entity, int[] blood, int energy) {
        this(entity, null, blood, energy);
    }
    private ModelRecipe(ResourceLocation entity, ResourceLocation block, int[] blood, int energy) {
        this.entity = entity;
        this.block = block;
        this.blood = blood;
        this.energy = energy;
    }
    public ResourceLocation getEntity() { return entity; }
    public ResourceLocation getBlock() { return block; }
    public boolean isBlockRecipe() { return block != null; }
    public static ModelRecipe forBlock(ResourceLocation block, int[] blood, int energy) {
        return new ModelRecipe(null, block, blood, energy);
    }
    /** Use HNN's synchronized registry: block models absent on the server stay absent on clients. */
    public java.util.Collection<? extends dev.shadowsoffire.hostilenetworks.data.DataModel> getModels() {
        if (isBlockRecipe()) return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOptional(block)
            .map(dev.shadowsoffire.hostilenetworks.data.DataModelRegistry.INSTANCE::getForBlock).orElse(java.util.List.of());
        return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(entity)
            .map(dev.shadowsoffire.hostilenetworks.data.DataModelRegistry.INSTANCE::getForEntity).orElse(java.util.List.of());
    }
    public boolean matchesModel(dev.shadowsoffire.hostilenetworks.data.DataModel model) {
        return getModels().contains(model);
    }
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
    public static Builder createBlock(ResourceLocation block, int[] blood, int energy) {
        return new Builder(forBlock(block, blood, energy), ResourceLocation.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "blood/blocks/" + block.getPath()));
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
        private static final MapCodec<ModelRecipe> CODEC = RecordCodecBuilder.<ModelRecipe>mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("entity").forGetter(recipe -> java.util.Optional.ofNullable(recipe.entity)),
            ResourceLocation.CODEC.optionalFieldOf("block").forGetter(recipe -> java.util.Optional.ofNullable(recipe.block)),
            Codec.INT.listOf().validate(values -> values.size() == 5 && values.stream().allMatch(v -> v >= 0)
                ? DataResult.success(values) : DataResult.error(() -> "blood must contain five non-negative tier amounts"))
                .xmap(values -> values.stream().mapToInt(Integer::intValue).toArray(), values -> java.util.Arrays.stream(values).boxed().toList())
                .fieldOf("blood").forGetter(ModelRecipe::getBlood),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("energy").forGetter(ModelRecipe::getEnergy)
        ).apply(instance, (entity, block, blood, energy) -> new ModelRecipe(entity.orElse(null), block.orElse(null), blood, energy)))
            .validate(recipe -> (recipe.entity == null) != (recipe.block == null)
                ? DataResult.success(recipe) : DataResult.error(() -> "Specify exactly one of entity or block"));
        private static final StreamCodec<RegistryFriendlyByteBuf, ModelRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override public ModelRecipe decode(RegistryFriendlyByteBuf buffer) { boolean block = buffer.readBoolean();
                ResourceLocation target = buffer.readResourceLocation();
                int[] blood = buffer.readVarIntArray(5);
                int energy = buffer.readInt();
                return block ? ModelRecipe.forBlock(target, blood, energy) : new ModelRecipe(target, blood, energy); }
            @Override public void encode(RegistryFriendlyByteBuf buffer, ModelRecipe recipe) { buffer.writeBoolean(recipe.isBlockRecipe()); buffer.writeResourceLocation(recipe.isBlockRecipe() ? recipe.block : recipe.entity); buffer.writeVarIntArray(recipe.blood); buffer.writeInt(recipe.energy); }
        };
        @Override public MapCodec<ModelRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, ModelRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
