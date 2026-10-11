package com.luna.sanguine_networks.datagen;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.init.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
	public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, SanguineNeuralNetworks.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(BlockTags.NEEDS_STONE_TOOL).add(
            ModBlocks.VIRTUAL_SACRIFICER.get(), ModBlocks.SUFFERING_INCORPORATED.get(), ModBlocks.SUFFERING_IO_PORT.get()
        );

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
            ModBlocks.VIRTUAL_SACRIFICER.get(), ModBlocks.SUFFERING_INCORPORATED.get(), ModBlocks.SUFFERING_IO_PORT.get()
        );
    }
}
