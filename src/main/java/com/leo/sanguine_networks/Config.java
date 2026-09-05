package com.leo.sanguine_networks;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = SanguineNeuralNetworks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue V_SACRIFICER_SPEED = BUILDER
        .comment("How much time in ticks should the virtual sacrificer wait before producing blood [100]")
        .defineInRange("sacrificer_speed", 100, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue V_SACRIFICER_ENERGY = BUILDER
        .comment("The energy capacity of the virtual sacrificer [1000000]")
        .defineInRange("sacrificer_energy", 1000000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue ITERATION_DATA = BUILDER
        .comment("How much data to give to the model each iteration [1]")
        .comment("Set to 0 to disable")
        .defineInRange("sacrificer_data", 1, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue FAULTY_DATA = BUILDER
        .comment("Should data be given to faulty models? [true]")
        .define("faulty_data", true);

    private static final ModConfigSpec.BooleanValue EXTRA_HNN_MODELS_ENABLED = BUILDER
        .comment("Allow Extra HNN combined models in the Virtual Sacrificer. Disabling also stops already inserted combined models.")
        .define("extra_hnn_models_enabled", true);
    private static final ModConfigSpec.BooleanValue SUFFERING_ENABLED = BUILDER
        .comment("Enable the Suffering Incorporated multiblock and its I/O ports. Disabling preserves existing blocks and stored contents.")
        .define("suffering_enabled", true);
    public static boolean extraHnnModelsEnabled = true;
    public static boolean sufferingEnabled = true;

    private static final ModConfigSpec.IntValue SUFFERING_SPEED = BUILDER
        .comment("Ticks per Suffering Incorporated model operation (HNN Data Center default: 300)")
        .defineInRange("suffering_speed", 300, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUFFERING_ENERGY = BUILDER
        .defineInRange("suffering_energy", 25000000, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUFFERING_TANK = BUILDER
        .comment("Essentia Vitae tank capacity in mB (1 mB = 1 EV)")
        .defineInRange("suffering_tank", 4000000, 1, Integer.MAX_VALUE);
    public static int sufferingSpeed = 300;
    public static int sufferingEnergy = 25000000;
    public static int sufferingTank = 4000000;

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int sacrificerSpeed;
    public static int sacrificerEnergy;
    public static int sacrificerData;

    public static boolean faultyData;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;
        extraHnnModelsEnabled = EXTRA_HNN_MODELS_ENABLED.get();
        sufferingEnabled = SUFFERING_ENABLED.get();
        sufferingSpeed = SUFFERING_SPEED.get();
        sufferingEnergy = SUFFERING_ENERGY.get();
        sufferingTank = SUFFERING_TANK.get();
        sacrificerSpeed = V_SACRIFICER_SPEED.get();
        sacrificerEnergy = V_SACRIFICER_ENERGY.get();
        sacrificerData = ITERATION_DATA.get();

        faultyData = FAULTY_DATA.get();
    }
}
