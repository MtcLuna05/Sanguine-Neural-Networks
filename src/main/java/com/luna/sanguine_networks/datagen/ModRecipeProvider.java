package com.luna.sanguine_networks.datagen;

import com.luna.sanguine_networks.init.ModBlocks;
import com.luna.sanguine_networks.init.ModItems;
import com.luna.sanguine_networks.recipe.CatalystRecipe;
import com.luna.sanguine_networks.recipe.ModelRecipe;
import dev.shadowsoffire.hostilenetworks.Hostile;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import wayoftime.bloodmagic.common.item.BloodMagicItems;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {

        ShapedRecipeBuilder.shaped(
                RecipeCategory.MISC,
                ModBlocks.VIRTUAL_SACRIFICER.get(),
                1
        )
        .pattern(" D ")
        .pattern("SOS")
        .pattern("BCB")
        .define('O', Blocks.OBSIDIAN)
        .define('S', BloodMagicItems.IMBUED_SLATE.get())
        .define('D', BloodMagicItems.DAGGER_OF_SACRIFICE.get())
        .define('C', Items.COMPARATOR)
        .define('B', BloodMagicItems.REAGENT_BINDING.get())
        .unlockedBy("hasItem", has(BloodMagicItems.IMBUED_SLATE.get()))
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
            .define('s', BloodMagicItems.SLATE.get())
            .define('S', Items.STICK)
            .unlockedBy("hasItem", has(BloodMagicItems.SLATE.get()))
            .save(pWriter, "wrench");


        CatalystRecipe.create(Ingredient.of(Hostile.Items.OVERWORLD_PREDICTION.get()), 10, 1.5f).save(pWriter);
        CatalystRecipe.create(Ingredient.of(Hostile.Items.NETHER_PREDICTION.get()), 25, 1.75f).save(pWriter);
        CatalystRecipe.create(Ingredient.of(Hostile.Items.END_PREDICTION.get()), 50, 2.25f).save(pWriter);
        CatalystRecipe.create(Ingredient.of(Hostile.Items.TWILIGHT_PREDICTION.get()), 50, 2f).save(pWriter);
        CatalystRecipe.create(Ingredient.of(Items.NETHER_STAR), 100, 5f).save(pWriter);
        CatalystRecipe.create(Ingredient.of(Items.BARRIER), -1, 5f).save(pWriter);

        ModelRecipe.create(new ResourceLocation("minecraft:blaze"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:wither_skeleton"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:witch"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:vindicator"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:evoker"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:hoglin"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:enderman"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:shulker"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:ghast"), getBlood(250), 1000).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:squid"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:chicken"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:cod"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:cow"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:pig"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:rabbit"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:sheep"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:polar_bear"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:guardian"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:mooshroom"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:glow_squid"), getBlood(75), 100).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:zombie"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:zombified_piglin"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:skeleton"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:slime"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:magma_cube"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:snow_golem"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:spider"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:creeper"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:drowned"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:phantom"), getBlood(125), 250).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:wither"), getBlood(1000), 2500).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:ender_dragon"), getBlood(1000), 2500).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:elder_guardian"), getBlood(1000), 2500).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:iron_golem"), getBlood(1000), 2500).save(pWriter);
        ModelRecipe.create(new ResourceLocation("minecraft:warden"), getBlood(1500), 3000).save(pWriter);

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
