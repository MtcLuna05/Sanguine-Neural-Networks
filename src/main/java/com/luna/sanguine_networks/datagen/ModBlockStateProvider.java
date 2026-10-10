package com.luna.sanguine_networks.datagen;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.init.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {

    ExistingFileHelper existingFileHelper;

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, SanguineNeuralNetworks.MODID, exFileHelper);
        this.existingFileHelper = exFileHelper;
    }

    @Override
    protected void registerStatesAndModels() {
        horizontalBlockWithItem(
            ModBlocks.VIRTUAL_SACRIFICER
        );
    }

    private void horizontalBlockWithItem(RegistryObject<? extends Block> block){
        horizontalBlock(
            block.get(),
            model(block)
        );
        simpleBlockItem(
            block.get(),
            model(block)
        );
    }

    private static ResourceLocation texture(RegistryObject<? extends Block> block) {
        return texture(block.getId().getPath());
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(SanguineNeuralNetworks.MODID, "block/" + name);
    }

    private static ModelFile model(RegistryObject<? extends Block> block) {
        return model(texture(block));
    }

    private static ModelFile model(ResourceLocation model) {
        return new ModelFile.UncheckedModelFile(model);
    }

    private ResourceLocation key(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }
}
