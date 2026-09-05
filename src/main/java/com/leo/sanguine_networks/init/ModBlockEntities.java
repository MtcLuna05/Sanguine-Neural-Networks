package com.leo.sanguine_networks.init;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.block.entity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlockEntities {
    public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, V_SACRIFICER_BE.get(), (be, side) -> be.getInventory());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, V_SACRIFICER_BE.get(), (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, SUFFERING_PORT_BE.get(), (be, side) -> be.getItemHandler());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, SUFFERING_PORT_BE.get(), (be, side) -> be.getEnergyHandler());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, SUFFERING_PORT_BE.get(), (be, side) -> be.getFluidHandler());
    }

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, SanguineNeuralNetworks.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SufferingBlockEntity>> SUFFERING_BE =
        BLOCK_ENTITIES.register("suffering_incorporated", () -> BlockEntityType.Builder.of(SufferingBlockEntity::new, ModBlocks.SUFFERING_INCORPORATED.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SufferingIOPortBlockEntity>> SUFFERING_PORT_BE =
        BLOCK_ENTITIES.register("suffering_io_port", () -> BlockEntityType.Builder.of(SufferingIOPortBlockEntity::new, ModBlocks.SUFFERING_IO_PORT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VSBlockEntity>> V_SACRIFICER_BE = BLOCK_ENTITIES.register("vsacrificer_be",
        () -> BlockEntityType.Builder.of(
            VSBlockEntity::new,
            ModBlocks.VIRTUAL_SACRIFICER.get()
        ).build(null)
    );
}
