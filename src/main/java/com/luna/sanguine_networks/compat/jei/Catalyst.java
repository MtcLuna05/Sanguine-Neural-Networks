package com.luna.sanguine_networks.compat.jei;

import com.breakinblocks.neovitae.common.fluid.NVFluids;
import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.init.ModBlocks;
import com.luna.sanguine_networks.recipe.CatalystRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class Catalyst implements IRecipeCategory<CatalystRecipe> {
	public static final Identifier UID = Identifier.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "virtual_sacrificer/catalyst");
	public static final IRecipeType<CatalystRecipe> RECIPE_TYPE = IRecipeType.create(UID, CatalystRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;

    public Catalyst(IGuiHelper helper) {
        background = new HnnJeiStyle(helper);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.VIRTUAL_SACRIFICER.get()));
    }

	@Override
	public IRecipeType<CatalystRecipe> getRecipeType() {
		return RECIPE_TYPE;
	}
    @Override public Component getTitle() { return Component.translatable("sanguine_networks.container.vsacrificer.catalyst"); }
    @Override public int getWidth() { return background.getWidth(); }
    @Override public int getHeight() { return background.getHeight(); }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CatalystRecipe recipe, IFocusGroup focuses) {
	    builder.addSlot(RecipeIngredientRole.INPUT, 28, 4).add(recipe.getInput());
        // These describe where the catalyst applies; they are not fabricated outputs.
	    builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 4, 4).add(ModBlocks.VIRTUAL_SACRIFICER.get());
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 96, 4)
            .setFluidRenderer(1, false, 16, 16)
            .addRichTooltipCallback((slot, tooltip) -> tooltip.add(Component.translatable("jei.sanguine_networks.catalyst_effect")))
		        .add(NVFluids.ESSENTIA_VITAE_SOURCE.get(), 1);
	    builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 66, 26).add(com.breakinblocks.neovitae.common.block.NVBlocks.ARA_VITAE);
    }

    @Override
    public void draw(CatalystRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        background.draw(graphics);
        HnnJeiStyle.drawProgress(graphics);
        var font = Minecraft.getInstance().font;
        Component uses = Component.translatable("jei.sanguine_networks.uses", recipe.getUses() == -1 ? "∞" : recipe.getUses());
	    graphics.text(font, uses, 33 - font.width(uses) / 2, 30, HnnJeiStyle.RED);
        String multiplier = new java.text.DecimalFormat("0.##").format(recipe.getMultiplier()) + "x";
	    graphics.text(font, multiplier, 114 - font.width(multiplier), 30, HnnJeiStyle.RED, true);
    }
}
