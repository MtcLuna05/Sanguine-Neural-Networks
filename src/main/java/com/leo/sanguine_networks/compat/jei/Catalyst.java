package com.leo.sanguine_networks.compat.jei;

import com.breakinblocks.neovitae.common.fluid.NVFluids;
import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.init.ModBlocks;
import com.leo.sanguine_networks.recipe.CatalystRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class Catalyst implements IRecipeCategory<CatalystRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "virtual_sacrificer/catalyst");
    public static final RecipeType<CatalystRecipe> RECIPE_TYPE = new RecipeType<>(UID, CatalystRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;

    public Catalyst(IGuiHelper helper) {
        background = new HnnJeiStyle(helper);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.VIRTUAL_SACRIFICER.get()));
    }

    @Override public RecipeType<CatalystRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("sanguine_networks.container.vsacrificer.catalyst"); }
    @Override public int getWidth() { return background.getWidth(); }
    @Override public int getHeight() { return background.getHeight(); }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CatalystRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 28, 4).addIngredients(recipe.getInput());
        // These describe where the catalyst applies; they are not fabricated outputs.
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 4, 4).addItemLike(ModBlocks.VIRTUAL_SACRIFICER.get());
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 96, 4)
            .setFluidRenderer(1, false, 16, 16)
            .addRichTooltipCallback((slot, tooltip) -> tooltip.add(Component.translatable("jei.sanguine_networks.catalyst_effect")))
            .addFluidStack(NVFluids.ESSENTIA_VITAE_SOURCE.get(), 1);
        builder.addSlot(RecipeIngredientRole.CATALYST, 66, 26).addItemLike(com.breakinblocks.neovitae.common.block.NVBlocks.ARA_VITAE);
    }

    @Override
    public void draw(CatalystRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics);
        HnnJeiStyle.drawProgress(graphics);
        var font = Minecraft.getInstance().font;
        Component uses = Component.translatable("jei.sanguine_networks.uses", recipe.getUses() == -1 ? "∞" : recipe.getUses());
        graphics.drawString(font, uses, 33 - font.width(uses) / 2, 30, HnnJeiStyle.RED);
        String multiplier = new java.text.DecimalFormat("0.##").format(recipe.getMultiplier()) + "x";
        graphics.drawString(font, multiplier, 114 - font.width(multiplier), 30, HnnJeiStyle.RED, true);
    }
}
