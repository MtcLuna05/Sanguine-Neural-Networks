package com.luna.sanguine_networks.recipe;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.init.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ModelRecipe implements Recipe<RecipeInput> {
	private final Identifier entity;
	private final Identifier block;
    private final int[] blood;
    private final int energy;

	public ModelRecipe(Identifier entity, int[] blood, int energy) {
        this(entity, null, blood, energy);
    }

	private ModelRecipe(Identifier entity, Identifier block, int[] blood, int energy) {
        this.entity = entity;
        this.block = block;
        this.blood = blood;
        this.energy = energy;
    }

	public Identifier getEntity() {
		return entity;
	}

	public Identifier getBlock() {
		return block;
	}
    public boolean isBlockRecipe() { return block != null; }

	public static ModelRecipe forBlock(Identifier block, int[] blood, int energy) {
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
    public int getEnergy() { return energy; }

	public static Builder create(Identifier entity, int[] blood, int energy) {
		return new Builder(new ModelRecipe(entity, blood, energy), Identifier.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "blood/" + entity.getPath()));
    }

	public static Builder createBlock(Identifier block, int[] blood, int energy) {
		return new Builder(forBlock(block, blood, energy), Identifier.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "blood/blocks/" + block.getPath()));
    }

	public record Builder(ModelRecipe recipe, Identifier id) {
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
	public RecipeSerializer<ModelRecipe> getSerializer() {
		return ModRecipes.BLOOD_RECIPE_SERIALIZER.get();
	}

	@Override
	public RecipeType<ModelRecipe> getType() {
		return Type.INSTANCE;
	}

    public static class Type implements RecipeType<ModelRecipe> {
        public static final Type INSTANCE = new Type();
    }

	public static class Serializer {
        private static final MapCodec<ModelRecipe> CODEC = RecordCodecBuilder.<ModelRecipe>mapCodec(instance -> instance.group(
				        Identifier.CODEC.optionalFieldOf("entity").forGetter(recipe -> java.util.Optional.ofNullable(recipe.entity)),
				        Identifier.CODEC.optionalFieldOf("block").forGetter(recipe -> java.util.Optional.ofNullable(recipe.block)),
            Codec.INT.listOf().validate(values -> values.size() == 5 && values.stream().allMatch(v -> v >= 0)
                ? DataResult.success(values) : DataResult.error(() -> "blood must contain five non-negative tier amounts"))
                .xmap(values -> values.stream().mapToInt(Integer::intValue).toArray(), values -> java.util.Arrays.stream(values).boxed().toList())
                .fieldOf("blood").forGetter(ModelRecipe::getBlood),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("energy").forGetter(ModelRecipe::getEnergy)
        ).apply(instance, (entity, block, blood, energy) -> new ModelRecipe(entity.orElse(null), block.orElse(null), blood, energy)))
            .validate(recipe -> (recipe.entity == null) != (recipe.block == null)
                ? DataResult.success(recipe) : DataResult.error(() -> "Specify exactly one of entity or block"));
		public static final RecipeSerializer<ModelRecipe> INSTANCE = new RecipeSerializer<>(CODEC,
				net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(CODEC.codec()));
    }
}
