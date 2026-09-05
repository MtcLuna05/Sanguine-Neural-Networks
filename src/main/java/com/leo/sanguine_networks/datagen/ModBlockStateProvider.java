package com.leo.sanguine_networks.datagen;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.init.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlockStateProvider extends BlockStateProvider {

    ExistingFileHelper existingFileHelper;

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, SanguineNeuralNetworks.MODID, exFileHelper);
        this.existingFileHelper = exFileHelper;
    }

    @Override
    protected void registerStatesAndModels() {
        horizontalBlockWithItem(ModBlocks.VIRTUAL_SACRIFICER);
        horizontalBlockWithItem(ModBlocks.SUFFERING_INCORPORATED);
        for (var mode : com.leo.sanguine_networks.block.SufferingIOPortBlock.Mode.values()) {
            getVariantBuilder(ModBlocks.SUFFERING_IO_PORT.get()).partialState()
                .with(com.leo.sanguine_networks.block.SufferingIOPortBlock.MODE, mode)
                .modelForState().modelFile(model(texture("suffering_io_port_" + mode.getSerializedName()))).addModel();
        }
        simpleBlockItem(ModBlocks.SUFFERING_IO_PORT.get(), model(texture("suffering_io_port_energy")));
    }

    private void horizontalBlockWithItem(DeferredHolder<Block, ? extends Block> block){
        horizontalBlock(
            block.get(),
            model(block)
        );
        simpleBlockItem(
            block.get(),
            model(block)
        );
    }

    private static ResourceLocation texture(DeferredHolder<Block, ? extends Block> block) {
        return texture(block.getId().getPath());
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "block/" + name);
    }

    private static ModelFile model(DeferredHolder<Block, ? extends Block> block) {
        return model(texture(block));
    }

    private static ModelFile model(ResourceLocation model) {
        return new ModelFile.UncheckedModelFile(model);
    }

    private ResourceLocation key(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }
}
