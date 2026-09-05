package com.leo.sanguine_networks.compat;

import com.leo.sanguine_networks.Config;
import com.leo.sanguine_networks.recipe.ModelRecipe;
import com.leo.sanguine_networks.util.Pair;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

/** All references to the optional mod stay behind the loaded-mod check. */
public final class ExtraHnnCompat {
    private ExtraHnnCompat() {}

    public static boolean isCombined(ItemStack stack) {
        return ModList.get().isLoaded("extrahnn") && Bridge.isCombined(stack);
    }

    public static Pair<Integer, Integer> getStats(ItemStack stack, Level level) {
        return level != null && isCombined(stack) ? Bridge.getStats(stack, level) : Pair.of(0, 0);
    }

    public static void completeCycle(ItemStack stack) {
        if (isCombined(stack)) Bridge.completeCycle(stack);
    }

    private static final class Bridge {
        static boolean isCombined(ItemStack stack) {
            return stack.getItem() instanceof net.lmor.extrahnn.item.ExtraDataModelItem;
        }

        static Pair<Integer, Integer> getStats(ItemStack stack, Level level) {
            var models = net.lmor.extrahnn.item.ExtraDataModelItem.getStoredModels(stack);
            if (models.size() != 4 || models.stream().anyMatch(model -> !model.isBound())) return Pair.of(0, 0);
            var tier = net.lmor.extrahnn.data.ExtraModelTier.getByData(net.lmor.extrahnn.item.ExtraDataModelItem.getData(stack));
            long blood = 0, energy = 0;
            for (var model : models) {
                ModelRecipe recipe = recipeFor(model.get(), level);
                // A combined cycle must have a valid blood recipe for every inner model.
                if (recipe == null) return Pair.of(0, 0);
                blood += recipe.getExtraBlood(tier.ordinal());
                energy += recipe.getEnergy();
            }
            energy *= 10;
            if (energy > Integer.MAX_VALUE) return Pair.of(0, 0);
            return Pair.of((int) Math.min(Integer.MAX_VALUE, blood), (int) energy);
        }

        private static ModelRecipe recipeFor(DataModel model, Level level) {
            for (var recipe : level.getRecipeManager().getAllRecipesFor(ModelRecipe.Type.INSTANCE)) {
                var entity = BuiltInRegistries.ENTITY_TYPE.getOptional(recipe.getEntity());
                if (entity.isPresent() && model.equals(DataModelRegistry.INSTANCE.getForEntity(entity.get()))) return recipe;
            }
            return null;
        }

        static void completeCycle(ItemStack stack) {
            int oldData = net.lmor.extrahnn.item.ExtraDataModelItem.getData(stack);
            net.lmor.extrahnn.item.ExtraDataModelItem.setData(stack,
                (int) Math.min(Integer.MAX_VALUE, (long) oldData + Config.sacrificerData));
        }
    }
}
