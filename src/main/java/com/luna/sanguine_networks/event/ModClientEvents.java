package com.luna.sanguine_networks.event;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.client.screen.VSacrificerScreen;
import com.luna.sanguine_networks.init.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = SanguineNeuralNetworks.MODID, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void registerRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(com.luna.sanguine_networks.init.ModBlockEntities.SUFFERING_BE.get(),
                com.luna.sanguine_networks.client.renderer.SufferingRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(final RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SUFFERING_MENU.get(), com.luna.sanguine_networks.client.screen.SufferingScreen::new);
        event.register(ModMenuTypes.V_SACRIFICER_MENU.get(), VSacrificerScreen::new);
    }

	@SubscribeEvent
	public static void recipesReceived(net.neoforged.neoforge.client.event.RecipesReceivedEvent event) {
		com.luna.sanguine_networks.init.ModRecipes.setClientRecipes(event.getRecipeMap());
	}

	@SubscribeEvent
	public static void loggedOut(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
		com.luna.sanguine_networks.init.ModRecipes.setClientRecipes(net.minecraft.world.item.crafting.RecipeMap.EMPTY);
	}

}
