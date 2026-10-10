package com.luna.sanguine_networks.init;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.block.entity.VSBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SanguineNeuralNetworks.MODID);

    public static final RegistryObject<BlockEntityType<VSBlockEntity>> V_SACRIFICER_BE = BLOCK_ENTITIES.register("vsacrificer_be",
        () -> BlockEntityType.Builder.of(
            VSBlockEntity::new,
            ModBlocks.VIRTUAL_SACRIFICER.get()
        ).build(null)
    );
}
