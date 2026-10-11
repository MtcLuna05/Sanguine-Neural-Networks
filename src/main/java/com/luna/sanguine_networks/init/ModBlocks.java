package com.luna.sanguine_networks.init;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.block.VSBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModBlocks {

	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SanguineNeuralNetworks.MODID);

    public static final DeferredHolder<Block, Block> VIRTUAL_SACRIFICER = registerBlock("virtual_sacrificer",
		    properties -> new VSBlock(
				    properties
                .strength(4.0f, 3000f)
                .noOcclusion()
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
        )
    );

    public static final DeferredHolder<Block, Block> SUFFERING_INCORPORATED = registerBlock("suffering_incorporated",
		    properties -> new com.luna.sanguine_networks.block.SufferingBlock(properties
            .strength(5.0f, 3000f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    public static final DeferredHolder<Block, Block> SUFFERING_IO_PORT = registerBlock("suffering_io_port",
		    properties -> new com.luna.sanguine_networks.block.SufferingIOPortBlock(properties
            .strength(5.0f, 3000f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

	public static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, java.util.function.Function<BlockBehaviour.Properties, T> block) {
		DeferredHolder<Block, T> toReturn = BLOCKS.registerBlock(name, properties -> block.apply(properties));
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredHolder<Block, T> block) {
	    ModItems.ITEMS.registerSimpleBlockItem(name, block);
    }
}
