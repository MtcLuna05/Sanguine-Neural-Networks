package com.luna.sanguine_networks.compat.jei;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.client.screen.VSacrificerScreen;
import com.luna.sanguine_networks.init.ModBlocks;
import com.luna.sanguine_networks.recipe.CatalystRecipe;
import com.luna.sanguine_networks.recipe.ModelRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
	    return Identifier.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
            new VirtualSacrificer(registration.getJeiHelpers().getGuiHelper()),
            new Catalyst(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
	    registration.addItemStackInfo(ModBlocks.SUFFERING_INCORPORATED.get().asItem().getDefaultInstance(),
            net.minecraft.network.chat.Component.translatable("sanguine_networks.suffering.structure"),
            net.minecraft.network.chat.Component.translatable("sanguine_networks.suffering.runes"),
            net.minecraft.network.chat.Component.translatable("sanguine_networks.suffering.models"),
            net.minecraft.network.chat.Component.translatable("sanguine_networks.suffering.boost_rule"),
            net.minecraft.network.chat.Component.translatable("sanguine_networks.suffering.energy_rule"));
	    registration.addItemStackInfo(ModBlocks.SUFFERING_IO_PORT.get().asItem().getDefaultInstance(),
            net.minecraft.network.chat.Component.translatable("sanguine_networks.port.instructions"),
            net.minecraft.network.chat.Component.translatable("sanguine_networks.suffering.output"));
	    List<ModelRecipe> bloodRecipes = com.luna.sanguine_networks.init.ModRecipes.getRecipes(Minecraft.getInstance().level, ModelRecipe.Type.INSTANCE).stream()
            .map(net.minecraft.world.item.crafting.RecipeHolder::value)
            .filter(recipe -> !recipe.getModels().isEmpty())
            .toList();

        registration.addRecipes(
            VirtualSacrificer.RECIPE_TYPE,
            bloodRecipes
        );

	    List<CatalystRecipe> catalystRecipes = com.luna.sanguine_networks.init.ModRecipes.getRecipes(Minecraft.getInstance().level, CatalystRecipe.Type.INSTANCE).stream().map(net.minecraft.world.item.crafting.RecipeHolder::value).toList();

        registration.addRecipes(
            Catalyst.RECIPE_TYPE,
            catalystRecipes
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
	    registration.addCraftingStation(VirtualSacrificer.RECIPE_TYPE, ModBlocks.VIRTUAL_SACRIFICER.get().asItem().getDefaultInstance());

	    registration.addCraftingStation(Catalyst.RECIPE_TYPE, ModBlocks.VIRTUAL_SACRIFICER.get().asItem().getDefaultInstance());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
            VSacrificerScreen.class,
            40,
            40,
            113,
            9,
            VirtualSacrificer.RECIPE_TYPE
        );
    }
}
