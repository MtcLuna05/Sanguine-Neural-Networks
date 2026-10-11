package com.luna.sanguine_networks.datagen;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.init.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModItemModelProvider extends ModelProvider {
	public ModItemModelProvider(PackOutput output) {
		super(output, SanguineNeuralNetworks.MODID);
	}

	@Override
	public String getName() {
		return "Item Models: " + SanguineNeuralNetworks.MODID;
	}

	@Override
	protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
		itemModels.generateFlatItem(ModItems.WRENCH.get(), ModelTemplates.FLAT_ITEM);
    }

	@Override
	protected java.util.stream.Stream<? extends Holder<Block>> getKnownBlocks() {
		return java.util.stream.Stream.empty();
	}

	@Override
	protected java.util.stream.Stream<? extends Holder<Item>> getKnownItems() {
		return java.util.stream.Stream.of(ModItems.WRENCH);
	}
}
