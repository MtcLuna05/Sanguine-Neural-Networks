package com.leo.sanguine_networks.datagen;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.init.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, SanguineNeuralNetworks.MODID, existingFileHelper);
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
