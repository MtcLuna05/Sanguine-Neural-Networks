package com.luna.sanguine_networks.init;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.block.entity.SufferingBlockEntity;
import com.luna.sanguine_networks.block.entity.SufferingIOPortBlockEntity;
import com.luna.sanguine_networks.block.entity.VSBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
	    event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK, V_SACRIFICER_BE.get(), (be, side) -> be.getInventory());
	    event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK, V_SACRIFICER_BE.get(), (be, side) -> be.getEnergyStorage());
	    event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK, SUFFERING_PORT_BE.get(), (be, side) -> be.getItemHandler());
	    event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK, SUFFERING_PORT_BE.get(), (be, side) -> be.getEnergyHandler());
	    event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.Fluid.BLOCK, SUFFERING_PORT_BE.get(), (be, side) -> be.getFluidHandler());
    }

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, SanguineNeuralNetworks.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SufferingBlockEntity>> SUFFERING_BE =
		    BLOCK_ENTITIES.register("suffering_incorporated", () -> new BlockEntityType<>(SufferingBlockEntity::new, ModBlocks.SUFFERING_INCORPORATED.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SufferingIOPortBlockEntity>> SUFFERING_PORT_BE =
		    BLOCK_ENTITIES.register("suffering_io_port", () -> new BlockEntityType<>(SufferingIOPortBlockEntity::new, ModBlocks.SUFFERING_IO_PORT.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VSBlockEntity>> V_SACRIFICER_BE = BLOCK_ENTITIES.register("vsacrificer_be",
		    () -> new BlockEntityType<>(
            VSBlockEntity::new,
            ModBlocks.VIRTUAL_SACRIFICER.get()
		    )
    );
}
