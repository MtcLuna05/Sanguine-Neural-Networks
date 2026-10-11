package com.luna.sanguine_networks.datagen;

import com.breakinblocks.neovitae.common.item.NVItems;
import com.luna.sanguine_networks.init.ModBlocks;
import com.luna.sanguine_networks.init.ModItems;
import com.luna.sanguine_networks.recipe.CatalystRecipe;
import com.luna.sanguine_networks.recipe.ModelRecipe;
import dev.shadowsoffire.hostilenetworks.Hostile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
	public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
		super(registries, output);
    }

    @Override
    protected void buildRecipes() {

	    ShapedRecipeBuilder.shaped(items,
                RecipeCategory.MISC,
                ModBlocks.VIRTUAL_SACRIFICER.get(),
                1
        )
        .pattern(" D ")
        .pattern("SOS")
        .pattern("BCB")
        .define('O', Blocks.OBSIDIAN)
        .define('S', NVItems.TABULA_ANIMATA.get())
        .define('D', com.breakinblocks.neovitae.common.block.NVBlocks.RUNE_SACRIFICE)
        .define('C', Items.COMPARATOR)
        .define('B', NVItems.REAGENT_BINDING.get())
        .unlockedBy("hasItem", has(NVItems.TABULA_ANIMATA.get()))
			    .save(output);

	    ShapedRecipeBuilder.shaped(items,
                RecipeCategory.MISC,
                ModItems.WRENCH.get(),
                1
            )
            .pattern(" Rs")
            .pattern(" SR")
            .pattern("S  ")
            .define('R', Items.REDSTONE)
            .define('s', NVItems.TABULA_RASA.get())
            .define('S', Items.STICK)
            .unlockedBy("hasItem", has(NVItems.TABULA_RASA.get()))
			    .save(output);

	    ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModBlocks.SUFFERING_INCORPORATED.get())
            .pattern("RGR").pattern("VSV").pattern("RGR")
            .define('R', com.breakinblocks.neovitae.common.block.NVBlocks.RUNE_BLANK)
            .define('G', com.breakinblocks.neovitae.common.block.NVBlocks.BLOOD_STAINED_GLASS)
            .define('V', ModBlocks.VIRTUAL_SACRIFICER.get())
            .define('S', NVItems.TABULA_SPIRITUS.get())
			    .unlockedBy("has_sacrificer", has(ModBlocks.VIRTUAL_SACRIFICER.get())).save(output);

	    ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModBlocks.SUFFERING_IO_PORT.get(), 4)
            .pattern("RGR").pattern("GCG").pattern("RGR")
            .define('R', com.breakinblocks.neovitae.common.block.NVBlocks.RUNE_BLANK)
            .define('G', com.breakinblocks.neovitae.common.block.NVBlocks.BLOOD_STAINED_GLASS)
            .define('C', Items.COMPARATOR)
			    .unlockedBy("has_sacrificer", has(ModBlocks.VIRTUAL_SACRIFICER.get())).save(output);

	    CatalystRecipe.create(Ingredient.of(Hostile.Items.OVERWORLD_PREDICTION.value()), 10, 1.5f).save(output);
	    CatalystRecipe.create(Ingredient.of(Hostile.Items.NETHER_PREDICTION.value()), 25, 1.75f).save(output);
	    CatalystRecipe.create(Ingredient.of(Hostile.Items.END_PREDICTION.value()), 50, 2.25f).save(output);
	    CatalystRecipe.create(Ingredient.of(Hostile.Items.TWILIGHT_PREDICTION.value()), 50, 2f).save(output);
	    CatalystRecipe.create(Ingredient.of(Items.NETHER_STAR), 100, 5f).save(output);
	    CatalystRecipe.create(Ingredient.of(Items.BARRIER), -1, 5f).save(output);

	    ModelRecipe.create(Identifier.parse("minecraft:blaze"), getBlood(250), 1000).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:breeze"), getBlood(250), 1000).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:wither_skeleton"), getBlood(250), 1000).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:witch"), getBlood(250), 1000).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:vindicator"), getBlood(250), 1000).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:evoker"), getBlood(250), 1000).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:hoglin"), getBlood(250), 1000).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:enderman"), getBlood(250), 1000).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:shulker"), getBlood(250), 1000).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:ghast"), getBlood(250), 1000).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:squid"), getBlood(75), 100).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:chicken"), getBlood(75), 100).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:cod"), getBlood(75), 100).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:cow"), getBlood(75), 100).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:pig"), getBlood(75), 100).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:rabbit"), getBlood(75), 100).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:sheep"), getBlood(75), 100).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:polar_bear"), getBlood(75), 100).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:guardian"), getBlood(75), 100).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:mooshroom"), getBlood(75), 100).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:glow_squid"), getBlood(75), 100).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:zombie"), getBlood(125), 250).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:zombified_piglin"), getBlood(125), 250).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:skeleton"), getBlood(125), 250).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:slime"), getBlood(125), 250).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:magma_cube"), getBlood(125), 250).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:snow_golem"), getBlood(125), 250).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:spider"), getBlood(125), 250).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:creeper"), getBlood(125), 250).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:drowned"), getBlood(125), 250).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:phantom"), getBlood(125), 250).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:wither"), getBlood(1000), 2500).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:ender_dragon"), getBlood(1000), 2500).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:elder_guardian"), getBlood(1000), 2500).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:iron_golem"), getBlood(1000), 2500).save(output);
	    ModelRecipe.create(Identifier.parse("minecraft:warden"), getBlood(1500), 3000).save(output);

    }

    private int[] getBlood(int base) {
        int[] toRet = new int[5];

        toRet[0] = 0;
        toRet[1] = (int) (base * 0.5f);
        toRet[2] = base;
        toRet[3] = (int) (base * 1.5f);
        toRet[4] = base * 3;

        return toRet;
    }

	public static class Runner extends RecipeProvider.Runner {
		public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
			return new ModRecipeProvider(registries, output);
		}

		@Override
		public String getName() {
			return "Sanguine Neural Networks Recipes";
		}
	}

}
