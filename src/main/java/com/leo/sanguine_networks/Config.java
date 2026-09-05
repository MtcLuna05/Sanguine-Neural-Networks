package com.leo.sanguine_networks;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = SanguineNeuralNetworks.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue V_SACRIFICER_SPEED = BUILDER
        .comment("How much time in ticks should the virtual sacrificer wait before producing blood [100]")
        .defineInRange("sacrificer_speed", 100, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue V_SACRIFICER_ENERGY = BUILDER
        .comment("The energy capacity of the virtual sacrificer [1000000]")
        .defineInRange("sacrificer_energy", 1000000, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue ITERATION_DATA = BUILDER
        .comment("How much data to give to the model each iteration [1]")
        .comment("Set to 0 to disable")
        .defineInRange("sacrificer_data", 1, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.BooleanValue FAULTY_DATA = BUILDER
        .comment("Should data be given to faulty models? [true]")
        .define("faulty_data", true);


    private static final ForgeConfigSpec.BooleanValue EXTRA_HNN_MODELS_ENABLED = BUILDER
        .comment("Allow Extra HNN combined models in the Virtual Sacrificer [true]")
        .define("extra_hnn_models_enabled", true);

    public static boolean extraHnnModelsEnabled = true;

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int sacrificerSpeed;
    public static int sacrificerEnergy;
    public static int sacrificerData;

    public static boolean faultyData;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        sacrificerSpeed = V_SACRIFICER_SPEED.get();
        sacrificerEnergy = V_SACRIFICER_ENERGY.get();
        sacrificerData = ITERATION_DATA.get();

        faultyData = FAULTY_DATA.get();
        extraHnnModelsEnabled = EXTRA_HNN_MODELS_ENABLED.get();
    }
}
