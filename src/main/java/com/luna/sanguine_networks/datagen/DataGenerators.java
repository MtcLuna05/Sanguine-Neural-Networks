package com.luna.sanguine_networks.datagen;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = SanguineNeuralNetworks.MODID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
	    PackOutput output = event.getGenerator().getPackOutput();
	    event.addProvider(new ModBlockStateProvider(output));
	    event.addProvider(new ModItemModelProvider(output));
	    event.addProvider(new ModLanguageProvider(output, "en_us"));
    }

	@SubscribeEvent
	public static void gatherServerData(GatherDataEvent.Server event) {
		PackOutput output = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();
		event.addProvider(ModLootTableProvider.create(output, provider));
		event.addProvider(new ModBlockTagGenerator(output, provider));
		event.addProvider(new ModRecipeProvider.Runner(output, provider));
    }
}
