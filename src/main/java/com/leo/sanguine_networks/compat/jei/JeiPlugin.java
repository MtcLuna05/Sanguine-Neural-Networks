package com.leo.sanguine_networks.compat.jei;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.client.screen.VSacrificerScreen;
import com.leo.sanguine_networks.init.ModBlocks;
import com.leo.sanguine_networks.recipe.CatalystRecipe;
import com.leo.sanguine_networks.recipe.ModelRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(SanguineNeuralNetworks.MODID, "jei_plugin");
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
        net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(new ResourceLocation("extrahnn:extra_data_model"))
            .ifPresent(item -> registration.addIngredientInfo(item.getDefaultInstance(), mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                net.minecraft.network.chat.Component.translatable("jei.sanguine_networks.combined_models")));
        List<ModelRecipe> bloodRecipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(ModelRecipe.Type.INSTANCE).stream()
            .filter(recipe -> net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(recipe.getEntity())
                .map(entity -> dev.shadowsoffire.hostilenetworks.data.DataModelRegistry.INSTANCE.getForEntity(entity) != null)
                .orElse(false))
            .toList();

        registration.addRecipes(
            VirtualSacrificer.RECIPE_TYPE,
            bloodRecipes
        );

        List<CatalystRecipe> catalystRecipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(CatalystRecipe.Type.INSTANCE);

        registration.addRecipes(
            Catalyst.RECIPE_TYPE,
            catalystRecipes
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
            ModBlocks.VIRTUAL_SACRIFICER.get().asItem().getDefaultInstance(),
            VirtualSacrificer.RECIPE_TYPE
        );

        registration.addRecipeCatalyst(
            ModBlocks.VIRTUAL_SACRIFICER.get().asItem().getDefaultInstance(),
            Catalyst.RECIPE_TYPE
        );
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
