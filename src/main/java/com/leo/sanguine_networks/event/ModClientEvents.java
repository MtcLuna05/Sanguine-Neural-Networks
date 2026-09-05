package com.leo.sanguine_networks.event;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.client.screen.VSacrificerScreen;
import com.leo.sanguine_networks.init.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = SanguineNeuralNetworks.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void registerRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(com.leo.sanguine_networks.init.ModBlockEntities.SUFFERING_BE.get(),
            com.leo.sanguine_networks.client.renderer.SufferingRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(final RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SUFFERING_MENU.get(), com.leo.sanguine_networks.client.screen.SufferingScreen::new);
        event.register(ModMenuTypes.V_SACRIFICER_MENU.get(), VSacrificerScreen::new);
    }

}
