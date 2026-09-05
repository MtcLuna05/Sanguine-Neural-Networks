package com.leo.sanguine_networks.init;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.item.WrenchItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, SanguineNeuralNetworks.MODID);

    public static final DeferredHolder<Item, Item> WRENCH = ITEMS.register("wrench",
        () -> new WrenchItem(
            new Item.Properties()
        )
    );

}
