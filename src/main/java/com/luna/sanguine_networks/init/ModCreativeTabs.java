package com.luna.sanguine_networks.init;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SanguineNeuralNetworks.MODID);

    public static final RegistryObject<CreativeModeTab> ITEMS = CREATIVE_MODE_TABS.register("items", () ->
        CreativeModeTab.builder()
            .title(Component.translatable(SanguineNeuralNetworks.MODID + ".itemGroup.main"))
            .icon(() -> ModBlocks.VIRTUAL_SACRIFICER.get().asItem().getDefaultInstance())
            .displayItems((idp, output) -> {
                Iterable<Item> items = ModItems.ITEMS.getEntries().stream().map(RegistryObject::get)::iterator;
                items.forEach(output::accept);

                Iterable<Block> blocks = ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
                blocks.forEach(output::accept);
            })
            .build()
    );
}
