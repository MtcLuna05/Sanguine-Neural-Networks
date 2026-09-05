package com.leo.sanguine_networks.datagen;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.init.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SanguineNeuralNetworks.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        simpleItem(ModItems.WRENCH);
    }

    private void simpleItem(DeferredHolder<Item, ? extends Item> item) {
        simpleItem(item.getId().getPath());
    }

    private void simpleItem(String name) {
        withExistingParent(name,
            ResourceLocation.parse("item/generated")).texture("layer0",
            ResourceLocation.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "item/" + name));
    }
}
