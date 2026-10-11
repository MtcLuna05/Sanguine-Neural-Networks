package com.luna.sanguine_networks.compat.jei;

import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.fluid.NVFluids;
import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.init.ModBlocks;
import com.luna.sanguine_networks.recipe.CatalystRecipe;
import com.luna.sanguine_networks.recipe.ModelRecipe;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.data.ModelTier;
import dev.shadowsoffire.hostilenetworks.data.ModelTierRegistry;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
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

import java.util.ArrayList;
import java.util.List;

public class VirtualSacrificer implements IRecipeCategory<ModelRecipe> {
	public static final Identifier UID = Identifier.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "virtual_sacrificer/recipe");
	public static final IRecipeType<ModelRecipe> RECIPE_TYPE = IRecipeType.create(UID, ModelRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final java.util.Map<IRecipeSlotDrawable, Long> displayedFrames = new java.util.WeakHashMap<>();

    public VirtualSacrificer(IGuiHelper helper) {
        background = new HnnJeiStyle(helper);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.VIRTUAL_SACRIFICER.get()));
    }

	@Override
	public IRecipeType<ModelRecipe> getRecipeType() {
		return RECIPE_TYPE;
	}
    @Override public Component getTitle() { return Component.translatable("sanguine_networks.container.virtual_sacrificer"); }
    @Override public int getWidth() { return background.getWidth(); }
    @Override public int getHeight() { return background.getHeight(); }
    @Override public IDrawable getIcon() { return icon; }

    private static ModelTier displayedTier() {
        var tiers = ModelTierRegistry.getSortedTiers().stream().filter(ModelTier::canSim).toList();
        long time = Minecraft.getInstance().level.getGameTime();
        return tiers.get((int) ((time / 50) % tiers.size()));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ModelRecipe recipe, IFocusGroup focuses) {
        var models = recipe.getModels().stream().map(model -> {
            var stack = new ItemStack(Hostile.Items.DATA_MODEL);
            DataModelItem.setStoredModel(stack, model);
            return stack;
        }).toList();
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 4).setSlotName("model").addItemStacks(models);

        List<ItemStack> catalysts = new ArrayList<>();
        catalysts.add(ItemStack.EMPTY);
	    for (var holder : com.luna.sanguine_networks.init.ModRecipes.getRecipes(Minecraft.getInstance().level, CatalystRecipe.Type.INSTANCE)) {
		    catalysts.addAll(holder.value().getInput().display().resolveForStacks(net.minecraft.world.item.crafting.display.SlotDisplayContext.fromLevel(Minecraft.getInstance().level)));
        }
	    builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 28, 4).setSlotName("catalyst")
            .addRichTooltipCallback((slot, tooltip) -> tooltip.add(Component.translatable("jei.sanguine_networks.optional_catalyst")))
            .addItemStacks(catalysts);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 96, 4).setSlotName("blood")
            .setFluidRenderer(1, false, 16, 16)
            .addRichTooltipCallback((slot, tooltip) -> {
                tooltip.add(Component.translatable("jei.sanguine_networks.ev_conversion"));
                tooltip.add(Component.translatable("jei.sanguine_networks.before_runes"));
                tooltip.add(Component.translatable("jei.sanguine_networks.energy", recipe.getEnergy()));
            })
		        .add(NVFluids.ESSENTIA_VITAE_SOURCE.get(), Math.max(1, recipe.getBlood(ModelTierRegistry.getMaxTier())));
	    builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 66, 26).add(NVBlocks.ARA_VITAE);
    }

    @Override
    public void onDisplayedIngredientsUpdate(ModelRecipe recipe, List<IRecipeSlotDrawable> slots, IFocusGroup focuses) {
        slots.forEach(displayedFrames::remove);
        updateDisplay(recipe, slots);
    }

    private void updateDisplay(ModelRecipe recipe, List<IRecipeSlotDrawable> slots) {
        if (slots.isEmpty()) return;
        long frame = Minecraft.getInstance().level.getGameTime() / 50;
        if (java.util.Objects.equals(displayedFrames.put(slots.getFirst(), frame), frame)) return;
        var tier = displayedTier();
        float multiplier = 1;
        for (var slot : slots) {
            if (slot.getSlotName().orElse("").equals("catalyst")) {
                var stack = slot.getDisplayedItemStack().orElse(ItemStack.EMPTY);
	            for (var holder : com.luna.sanguine_networks.init.ModRecipes.getRecipes(Minecraft.getInstance().level, CatalystRecipe.Type.INSTANCE)) {
                    if (holder.value().getInput().test(stack)) {
                        multiplier = holder.value().getMultiplier();
                        break;
                    }
                }
            }
        }
        for (var slot : slots) {
            switch (slot.getSlotName().orElse("")) {
                case "model" -> {
                    slot.clearDisplayOverrides();
                    slot.getDisplayedItemStack().ifPresent(original -> {
                        ItemStack stack = original.copy();
                        var model = DataModelItem.getStoredModel(stack);
                        if (model.isBound()) DataModelItem.setData(stack, model.get().getRequiredData(tier));
	                    slot.createDisplayOverrides().add(stack);
                    });
                }
                case "blood" -> {
                    int amount = (int) (recipe.getBlood(tier) * multiplier);
                    slot.clearDisplayOverrides();
                    var overrides = slot.createDisplayOverrides();
	                if (amount > 0) overrides.add(NVFluids.ESSENTIA_VITAE_SOURCE.get(), amount);
                }
            }
        }
    }

    @Override
    public void draw(ModelRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        background.draw(graphics);
        HnnJeiStyle.drawProgress(graphics);
        updateDisplay(recipe, slots.getSlotViews().stream().filter(IRecipeSlotDrawable.class::isInstance).map(IRecipeSlotDrawable.class::cast).toList());
        var font = Minecraft.getInstance().font;
        Component tier = displayedTier().getComponent().copy();
	    graphics.text(font, tier, 33 - font.width(tier) / 2, 30, 0xFFFFFFFF);
    }
}
