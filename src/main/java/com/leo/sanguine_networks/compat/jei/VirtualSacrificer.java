package com.leo.sanguine_networks.compat.jei;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.init.ModBlocks;
import com.leo.sanguine_networks.recipe.ModelRecipe;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.shadowsoffire.hostilenetworks.data.ModelTier;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.hostilenetworks.jei.TickingDataModelWrapper;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Iterator;
import java.util.List;

public class VirtualSacrificer implements IRecipeCategory<ModelRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(SanguineNeuralNetworks.MODID, "virtual_sacrificer/recipe");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SanguineNeuralNetworks.MODID, "textures/gui/virtual_sacrificer_jei.png");
    public static final RecipeType<ModelRecipe> RECIPE_TYPE = new RecipeType<>(UID, ModelRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    private ModelTier currentTier;
    private int ticks = 0;
    private long lastTickTime = 0L;

    public VirtualSacrificer(IGuiHelper guiHelper) {
        this.currentTier = ModelTier.FAULTY;
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 116, 54);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, ModBlocks.VIRTUAL_SACRIFICER.get().asItem().getDefaultInstance());
    }

    @Override
    public RecipeType<ModelRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(SanguineNeuralNetworks.MODID + ".container.virtual_sacrificer");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ModelRecipe recipe, IFocusGroup iFocusGroup) {
        DataModelItem dataModelItem = Hostile.Items.DATA_MODEL.get();
        ItemStack modelStack = new ItemStack(dataModelItem);
        EntityType<?> value = ForgeRegistries.ENTITY_TYPES.getValue(recipe.getEntity());

        DataModel model = DataModelRegistry.INSTANCE.getForEntity(value);
        DataModelItem.setStoredModel(modelStack, model);

        builder.addSlot(
            RecipeIngredientRole.INPUT,
            61,
            1
        ).addItemStack(modelStack);
    }

    @Override
    public void onDisplayedIngredientsUpdate(ModelRecipe recipe, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
        if(++ticks % 20 != 0) return;

        ModelTier next = this.currentTier.next();
        if (next == this.currentTier) {
            next = ModelTier.BASIC;
        }

        this.currentTier = next;
    }

    @Override
    public void draw(ModelRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        Component energy = Component.translatable("jei."+ SanguineNeuralNetworks.MODID + ".energy", recipe.getEnergy());
        guiGraphics.drawString(font, energy, 2, 44, 0xFFFF0000);

        Component tier = currentTier.getComponent();
        Component blood = Component.translatable("jei."+ SanguineNeuralNetworks.MODID + ".blood", recipe.getBlood()[currentTier.ordinal()]);
        guiGraphics.drawString(font, blood, 2, 26, 0xFFFF0000);
        guiGraphics.drawString(font, tier, 2, 7, currentTier.color());
    }
}
