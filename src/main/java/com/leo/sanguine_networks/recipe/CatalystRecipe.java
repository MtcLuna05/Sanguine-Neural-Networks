package com.leo.sanguine_networks.recipe;

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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CatalystRecipe implements Recipe<Container> {
    private final Ingredient input;
    private final int uses;
    private final float multiplier;
    private final ResourceLocation id;

    public CatalystRecipe(Ingredient input, int uses, float multiplier, ResourceLocation id) {
        this.input = input;
        this.uses = uses;
        this.multiplier = multiplier;
        this.id = id;
    }

    public static CatalystRecipe.Builder create(Ingredient input, int uses, float multiplier) {
        Item item = input.getItems()[0].getItem();
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);

        ResourceLocation recipeId = new ResourceLocation(SanguineNeuralNetworks.MODID, "catalyst/" + key.getPath());
        return new CatalystRecipe.Builder(input, uses, multiplier, recipeId);
    }

    public Ingredient getInput() {
        return input;
    }

    public int getUses() {
        return uses;
    }

    public float getMultiplier() {
        return multiplier;
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
        return ModRecipes.CATALYST_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<CatalystRecipe> {
        public static final Type INSTANCE = new Type();
    }

    public static class Serializer implements RecipeSerializer<CatalystRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public CatalystRecipe fromJson(ResourceLocation id, JsonObject recipe) {
            Ingredient input = Ingredient.fromJson(recipe.get("catalyst"));
            int uses = recipe.get("uses").getAsInt();
            float multiplier = recipe.get("multiplier").getAsFloat();

            return new CatalystRecipe(input, uses, multiplier, id);
        }

        @Override
        public @Nullable CatalystRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            int uses = buffer.readInt();
            float multiplier = buffer.readFloat();

            return new CatalystRecipe(input, uses, multiplier, id);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, CatalystRecipe pRecipe) {
            pRecipe.input.toNetwork(pBuffer);
            pBuffer.writeInt(pRecipe.uses);
            pBuffer.writeFloat(pRecipe.multiplier);
        }
    }

    public static class Builder implements RecipeBuilder, FinishedRecipe {
        private final Ingredient input;
        private final int uses;
        private final float multiplier;
        private final ResourceLocation id;

        public Builder(Ingredient input, int uses, float multiplier, ResourceLocation id) {
            this.input = input;
            this.uses = uses;
            this.multiplier = multiplier;
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
            return this.input.getItems()[0].getItem();
        }

        @Override
        public void save(Consumer<FinishedRecipe> finishedRecipeConsumer, ResourceLocation recipeId) {
            finishedRecipeConsumer.accept(this);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("catalyst", input.toJson());
            json.addProperty("uses",  uses);
            json.addProperty("multiplier", multiplier);
        }

        @Override
        public @NotNull ResourceLocation getId() {
            return this.id;
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return ModRecipes.CATALYST_RECIPE_SERIALIZER.get();
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
