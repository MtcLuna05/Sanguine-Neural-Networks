package com.leo.sanguine_networks.compat.jei;

import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.fluid.NVFluids;
import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.init.ModBlocks;
import com.leo.sanguine_networks.recipe.CatalystRecipe;
import com.leo.sanguine_networks.recipe.ModelRecipe;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
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
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class VirtualSacrificer implements IRecipeCategory<ModelRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "virtual_sacrificer/recipe");
    public static final RecipeType<ModelRecipe> RECIPE_TYPE = new RecipeType<>(UID, ModelRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final java.util.Map<IRecipeSlotDrawable, Long> displayedFrames = new java.util.WeakHashMap<>();

    public VirtualSacrificer(IGuiHelper helper) {
        background = new HnnJeiStyle(helper);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.VIRTUAL_SACRIFICER.get()));
    }

    @Override public RecipeType<ModelRecipe> getRecipeType() { return RECIPE_TYPE; }
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
        catalysts.add(null); // A blank cycle shows the unboosted recipe: catalysts are optional.
        for (var holder : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(CatalystRecipe.Type.INSTANCE)) {
            catalysts.addAll(List.of(holder.value().getInput().getItems()));
        }
        builder.addSlot(RecipeIngredientRole.CATALYST, 28, 4).setSlotName("catalyst")
            .addRichTooltipCallback((slot, tooltip) -> tooltip.add(Component.translatable("jei.sanguine_networks.optional_catalyst")))
            .addItemStacks(catalysts);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 96, 4).setSlotName("blood")
            .setFluidRenderer(1, false, 16, 16)
            .addRichTooltipCallback((slot, tooltip) -> {
                tooltip.add(Component.translatable("jei.sanguine_networks.ev_conversion"));
                tooltip.add(Component.translatable("jei.sanguine_networks.before_runes"));
                tooltip.add(Component.translatable("jei.sanguine_networks.energy", recipe.getEnergy()));
            })
            .addFluidStack(NVFluids.ESSENTIA_VITAE_SOURCE.get(), Math.max(1, recipe.getBlood(ModelTierRegistry.getMaxTier())));
        // The lower HNN slot identifies the receiving altar, not a second product.
        builder.addSlot(RecipeIngredientRole.CATALYST, 66, 26).addItemLike(NVBlocks.ARA_VITAE);
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
                for (var holder : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(CatalystRecipe.Type.INSTANCE)) {
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
                    // Clear the override before reading, preserving JEI's focused model/variant.
                    slot.clearDisplayOverrides();
                    slot.getDisplayedItemStack().ifPresent(original -> {
                        ItemStack stack = original.copy();
                        var model = DataModelItem.getStoredModel(stack);
                        if (model.isBound()) DataModelItem.setData(stack, model.get().getRequiredData(tier));
                        slot.createDisplayOverrides().addItemStack(stack);
                    });
                }
                case "blood" -> {
                    int amount = (int) (recipe.getBlood(tier) * multiplier);
                    slot.clearDisplayOverrides();
                    var overrides = slot.createDisplayOverrides();
                    if (amount > 0) overrides.addFluidStack(NVFluids.ESSENTIA_VITAE_SOURCE.get(), amount);
                }
            }
        }
    }

    @Override
    public void draw(ModelRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics);
        HnnJeiStyle.drawProgress(graphics);
        // Keep both the visible model's components and the fluid tooltip in sync at HNN's 50-tick cadence.
        updateDisplay(recipe, slots.getSlotViews().stream().filter(IRecipeSlotDrawable.class::isInstance).map(IRecipeSlotDrawable.class::cast).toList());
        var font = Minecraft.getInstance().font;
        Component tier = displayedTier().getComponent().copy().withStyle(style -> style.withColor(HnnJeiStyle.RED));
        graphics.drawString(font, tier, 33 - font.width(tier) / 2, 30, HnnJeiStyle.RED);
    }
}
