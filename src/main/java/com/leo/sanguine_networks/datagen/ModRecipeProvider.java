package com.leo.sanguine_networks.datagen;

import com.leo.sanguine_networks.init.ModBlocks;
import com.leo.sanguine_networks.init.ModItems;
import com.leo.sanguine_networks.recipe.CatalystRecipe;
import com.leo.sanguine_networks.recipe.ModelRecipe;
import dev.shadowsoffire.hostilenetworks.Hostile;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import com.breakinblocks.neovitae.common.item.NVItems;
import net.minecraft.core.HolderLookup;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(pOutput, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput pWriter) {

        ShapedRecipeBuilder.shaped(
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
        .save(pWriter);

        ShapedRecipeBuilder.shaped(
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
            .save(pWriter, ResourceLocation.fromNamespaceAndPath("sanguine_networks", "wrench"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SUFFERING_INCORPORATED.get())
            .pattern("RGR").pattern("VSV").pattern("RGR")
            .define('R', com.breakinblocks.neovitae.common.block.NVBlocks.RUNE_BLANK)
            .define('G', com.breakinblocks.neovitae.common.block.NVBlocks.BLOOD_STAINED_GLASS)
            .define('V', ModBlocks.VIRTUAL_SACRIFICER.get())
            .define('S', NVItems.TABULA_SPIRITUS.get())
            .unlockedBy("has_sacrificer", has(ModBlocks.VIRTUAL_SACRIFICER.get())).save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SUFFERING_IO_PORT.get(), 4)
            .pattern("RGR").pattern("GCG").pattern("RGR")
            .define('R', com.breakinblocks.neovitae.common.block.NVBlocks.RUNE_BLANK)
            .define('G', com.breakinblocks.neovitae.common.block.NVBlocks.BLOOD_STAINED_GLASS)
            .define('C', Items.COMPARATOR)
            .unlockedBy("has_sacrificer", has(ModBlocks.VIRTUAL_SACRIFICER.get())).save(pWriter);

        CatalystRecipe.create(Ingredient.of(Hostile.Items.OVERWORLD_PREDICTION.value()), 10, 1.5f).save(pWriter);
        CatalystRecipe.create(Ingredient.of(Hostile.Items.NETHER_PREDICTION.value()), 25, 1.75f).save(pWriter);
        CatalystRecipe.create(Ingredient.of(Hostile.Items.END_PREDICTION.value()), 50, 2.25f).save(pWriter);
        CatalystRecipe.create(Ingredient.of(Hostile.Items.TWILIGHT_PREDICTION.value()), 50, 2f).save(pWriter);
        CatalystRecipe.create(Ingredient.of(Items.NETHER_STAR), 100, 5f).save(pWriter);
        CatalystRecipe.create(Ingredient.of(Items.BARRIER), -1, 5f).save(pWriter);

        ModelRecipe.create(ResourceLocation.parse("minecraft:blaze"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:breeze"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:wither_skeleton"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:witch"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:vindicator"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:evoker"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:hoglin"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:enderman"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:shulker"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:ghast"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:squid"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:chicken"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:cod"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:cow"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:pig"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:rabbit"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:sheep"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:polar_bear"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:guardian"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:mooshroom"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:glow_squid"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:zombie"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:zombified_piglin"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:skeleton"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:slime"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:magma_cube"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:snow_golem"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:spider"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:creeper"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:drowned"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:phantom"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:wither"), getBlood(1000), 2500).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:ender_dragon"), getBlood(1000), 2500).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:elder_guardian"), getBlood(1000), 2500).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:iron_golem"), getBlood(1000), 2500).save(pWriter);
        ModelRecipe.create(ResourceLocation.parse("minecraft:warden"), getBlood(1500), 3000).save(pWriter);

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

}
