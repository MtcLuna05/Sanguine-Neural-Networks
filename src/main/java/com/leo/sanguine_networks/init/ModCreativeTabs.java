package com.leo.sanguine_networks.init;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SanguineNeuralNetworks.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS = CREATIVE_MODE_TABS.register("items", () ->
        CreativeModeTab.builder()
            .title(Component.translatable(SanguineNeuralNetworks.MODID + ".itemGroup.main"))
            .icon(() -> ModBlocks.VIRTUAL_SACRIFICER.get().asItem().getDefaultInstance())
            .displayItems((idp, output) -> {
                ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
            })
            .build()
    );
}
