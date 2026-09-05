package com.leo.sanguine_networks.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.init.ModRecipes;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ModelRecipe implements Recipe<Container> {
    private final ResourceLocation entity;
    private final int[] blood;
    private final int energy;
    private final ResourceLocation id;

    public ModelRecipe(ResourceLocation entity, int[] blood, int energy, ResourceLocation id) {
        if (blood.length != 5 || java.util.Arrays.stream(blood).anyMatch(value -> value < 0) || energy < 0)
            throw new IllegalArgumentException("Blood recipes require five non-negative tier amounts and non-negative energy");
        this.entity = entity;
        this.blood = blood;
        this.energy = energy;
        this.id = id;
    }

    public static ModelRecipe.Builder create(ResourceLocation entity, int[] blood, int energy) {
        ResourceLocation recipeId = new ResourceLocation(SanguineNeuralNetworks.MODID, "blood/" + entity.getPath());
        return new ModelRecipe.Builder(entity, blood, energy, recipeId);
    }

    public ResourceLocation getEntity() {
        return entity;
    }

    public int[] getBlood() {
        return blood;
    }

    public int getExtraBlood(int extraTier) {
        if (extraTier < 0) throw new IllegalArgumentException("Extra tier must be non-negative");
        long previous = blood[blood.length - 2];
        long current = blood[blood.length - 1];
        for (int i = 0; i <= extraTier; i++) {
            long next = Math.max(0, Math.min(Integer.MAX_VALUE, current + 2 * (current - previous)));
            previous = current;
            current = next;
            if (current == previous || current == Integer.MAX_VALUE) break;
        }
        return (int) current;
    }
    public int getBlood(dev.shadowsoffire.hostilenetworks.data.ModelTier tier) {
        return blood[Math.min(tier.ordinal(), blood.length - 1)];
    }

    public int getEnergy() {
        return energy;
    }

    @Override
    public boolean isSpecial() { return true; }

    @Override
    public boolean matches(Container pContainer, Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BLOOD_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<ModelRecipe> {
        public static final Type INSTANCE = new Type();
    }

    public static class Serializer implements RecipeSerializer<ModelRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public ModelRecipe fromJson(ResourceLocation id, JsonObject recipe) {
            ResourceLocation entity = new ResourceLocation(recipe.get("entity").getAsString());
            int energy = recipe.get("energy").getAsInt();

            List<JsonElement> bloodList = recipe.getAsJsonArray("blood").asList();
            int[] blood = new int[bloodList.size()];
            for (int i = 0; i < bloodList.size(); i++) {
                blood[i] = bloodList.get(i).getAsInt();
            }

            return new ModelRecipe(entity, blood, energy, id);
        }

        @Override
        public @Nullable ModelRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ResourceLocation entity = buffer.readResourceLocation();
            int[] blood = buffer.readVarIntArray();
            int energy = buffer.readInt();

            return new ModelRecipe(entity, blood, energy, id);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, ModelRecipe pRecipe) {
            pBuffer.writeResourceLocation(pRecipe.entity);
            pBuffer.writeVarIntArray(pRecipe.blood);
            pBuffer.writeInt(pRecipe.energy);
        }
    }

    public static class Builder implements RecipeBuilder, FinishedRecipe {
        private final ResourceLocation entity;
        private final int[] blood;
        private final int energy;
        private final ResourceLocation id;

        public Builder(ResourceLocation entity, int[] blood, int energy, ResourceLocation id) {
            this.entity = entity;
            this.blood = blood;
            this.energy = energy;
            this.id = id;
        }

        @Override
        public RecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
            return this;
        }

        @Override
        public RecipeBuilder group(@Nullable String pGroupName) {
            return this;
        }

        @Override
        public @NotNull Item getResult() {
            return Items.AIR;
        }

        @Override
        public void save(Consumer<FinishedRecipe> finishedRecipeConsumer, ResourceLocation recipeId) {
            finishedRecipeConsumer.accept(this);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("entity", entity.toString());
            JsonArray array = new JsonArray();
            for (int i : blood) {
                array.add(i);
            }
            json.add("blood",  array);
            json.addProperty("energy", energy);
        }

        @Override
        public @NotNull ResourceLocation getId() {
            return this.id;
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return ModRecipes.BLOOD_RECIPE_SERIALIZER.get();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
