package com.luna.sanguine_networks.datagen;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.block.SufferingIOPortBlock;
import com.luna.sanguine_networks.init.ModBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModBlockStateProvider extends ModelProvider {
	public ModBlockStateProvider(PackOutput output) {
		super(output, SanguineNeuralNetworks.MODID);
	}

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
	    horizontalBlockWithItem(blockModels, ModBlocks.VIRTUAL_SACRIFICER.get(), "virtual_sacrificer");
	    horizontalBlockWithItem(blockModels, ModBlocks.SUFFERING_INCORPORATED.get(), "suffering_incorporated");
	    var variants = PropertyDispatch.initial(SufferingIOPortBlock.MODE);
	    for (var mode : SufferingIOPortBlock.Mode.values())
		    variants.select(mode, BlockModelGenerators.plainVariant(model("suffering_io_port_" + mode.getSerializedName())));
	    blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(ModBlocks.SUFFERING_IO_PORT.get()).with(variants));
	    blockModels.registerSimpleItemModel(ModBlocks.SUFFERING_IO_PORT.get(), model("suffering_io_port_energy"));
    }

	private void horizontalBlockWithItem(BlockModelGenerators models, Block block, String name) {
		models.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model(name)))
				.with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
		models.registerSimpleItemModel(block, model(name));
    }

	private static Identifier model(String name) {
		return Identifier.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "block/" + name);
    }

	@Override
	protected java.util.stream.Stream<? extends Holder<Item>> getKnownItems() {
		return ModBlocks.BLOCKS.getEntries().stream().map(block -> net.minecraft.core.registries.BuiltInRegistries.ITEM.wrapAsHolder(block.get().asItem()));
    }
}
