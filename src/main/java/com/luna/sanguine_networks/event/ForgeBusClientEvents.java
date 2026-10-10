package com.luna.sanguine_networks.event;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.client.screen.VSacrificerScreen;
import com.luna.sanguine_networks.init.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SanguineNeuralNetworks.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ForgeBusClientEvents {

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.V_SACRIFICER_MENU.get(), VSacrificerScreen::new);
        });
    }

}
