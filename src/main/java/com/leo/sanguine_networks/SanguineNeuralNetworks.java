package com.leo.sanguine_networks;

import com.leo.sanguine_networks.init.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SanguineNeuralNetworks.MODID)
public class SanguineNeuralNetworks {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "sanguine_networks";

    public SanguineNeuralNetworks(IEventBus modEventBus, ModContainer container) {

        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);
        ModRecipes.TYPES.register(modEventBus);
        modEventBus.addListener(ModBlockEntities::registerCapabilities);

        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "sanguine_networks-common.toml");
    }

}
