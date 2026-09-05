package com.leo.sanguine_networks.init;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.block.VSBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, SanguineNeuralNetworks.MODID);

    public static final DeferredHolder<Block, Block> VIRTUAL_SACRIFICER = registerBlock("virtual_sacrificer",
        () -> new VSBlock(
            BlockBehaviour.Properties.of()
                .strength(4.0f, 3000f)
                .noOcclusion()
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
        )
    );

    public static final DeferredHolder<Block, Block> SUFFERING_INCORPORATED = registerBlock("suffering_incorporated",
        () -> new com.leo.sanguine_networks.block.SufferingBlock(BlockBehaviour.Properties.of()
            .strength(5.0f, 3000f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    public static final DeferredHolder<Block, Block> SUFFERING_IO_PORT = registerBlock("suffering_io_port",
        () -> new com.leo.sanguine_networks.block.SufferingIOPortBlock(BlockBehaviour.Properties.of()
            .strength(5.0f, 3000f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    public static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, Supplier<T> block) {
        DeferredHolder<Block, T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredHolder<Block, T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
