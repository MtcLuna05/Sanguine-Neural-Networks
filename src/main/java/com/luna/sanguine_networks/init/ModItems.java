package com.luna.sanguine_networks.init;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.item.WrenchItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SanguineNeuralNetworks.MODID);

	public static final DeferredHolder<Item, WrenchItem> WRENCH = ITEMS.registerItem("wrench", WrenchItem::new);

}
