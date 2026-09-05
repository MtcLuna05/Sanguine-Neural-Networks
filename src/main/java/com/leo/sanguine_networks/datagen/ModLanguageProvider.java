package com.leo.sanguine_networks.datagen;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.init.ModBlocks;
import com.leo.sanguine_networks.init.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, SanguineNeuralNetworks.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("sanguine_networks.suffering.status.8", "Suffering Incorporated is disabled in the config");
        add("jei.sanguine_networks.combined_models", "Virtual Sacrificer only: sums blood from all four inner models at the Extra HNN tier. Power: 10× the sum of their recipe costs. Consumes one catalyst use per completed cycle. Cannot be used in Suffering Incorporated.");
        add("sanguine_networks.suffering.status.7", "Paused by redstone");
        add("block.sanguine_networks.suffering_incorporated", "Suffering Inc©");
        add("block.sanguine_networks.suffering_io_port", "Suffering Inc© I/O Port");
        add("sanguine_networks.suffering.short", "Suffering Inc");
        add("sanguine_networks.suffering.catalysts", "Catalysts");
        add("sanguine_networks.suffering.vitae", "Essentia Vitae");
        add("sanguine_networks.suffering.incomplete", "Incomplete shell");
        add("sanguine_networks.suffering.formed", "Formed");
        add("sanguine_networks.suffering.structure", "Hollow 7×7×7: 49 runes on the floor, blood-stained glass walls and ceiling. Controller in a wall directly above the floor; ports replace glass.");
        add("sanguine_networks.suffering.runes", "Any NeoVitae altar runes may replace blank runes. All shell runes are decorative.");
        add("sanguine_networks.suffering.models", "25 model slots; Self-Aware models required.");
        add("sanguine_networks.suffering.boost_rule", "Four catalyst slots. Boosts add together: four 5× catalysts give 20×. Each slot spends one use per completed model cycle.");
        add("sanguine_networks.suffering.energy_rule", "Each active catalyst slot after the first adds 50% energy cost.");
        add("sanguine_networks.suffering.output", "Extract Essentia Vitae from an output port, or link the port to an Ara Vitae with a wrench. Receiving altar sacrifice bonuses apply.");
        add("sanguine_networks.port.energy", "Energy Input");
        add("sanguine_networks.port.models", "Model Input");
        add("sanguine_networks.port.catalysts", "Catalyst Input / Output");
        add("sanguine_networks.port.output", "Essentia Vitae Output");
        add("sanguine_networks.port.mode", "Port mode: %s");
        add("sanguine_networks.port.unlinked", "Ara Vitae link cleared");
        add("sanguine_networks.port.require_output", "Set this port to output mode before linking an Ara Vitae");
        add("sanguine_networks.port.instructions", "Replace shell glass with ports. Right-click with an empty hand to cycle modes; shift-right-click with an empty hand to unlink an altar.");
        add("item.sanguine_networks.setPort", "Successfully linked Ara Vitae to the output port");
        add("sanguine_networks.suffering.status.0", "Empty model slot");
        add("sanguine_networks.suffering.status.1", "Simulating");
        add("sanguine_networks.suffering.status.2", "Self-Aware model required");
        add("sanguine_networks.suffering.status.3", "No blood recipe");
        add("sanguine_networks.suffering.status.4", "Incomplete shell");
        add("sanguine_networks.suffering.status.5", "Insufficient energy");
        add("sanguine_networks.suffering.status.6", "Essentia Vitae tank full");

        this.add("jei.sanguine_networks.optional_catalyst", "Optional catalyst (blank = no bonus)");
        this.add("jei.sanguine_networks.ev_conversion", "1 mB Essentia Vitae = 1 EV");
        this.add("jei.sanguine_networks.before_runes", "Before altar rune bonuses");
        this.add("jei.sanguine_networks.catalyst_effect", "Multiplies blood production; amount depends on the model");
        this.add("item.sanguine_networks.unavailableAltar", "Saved Ara Vitae is missing, unloaded, or in another dimension");
        this.add(SanguineNeuralNetworks.MODID + ".itemGroup.main", "Sanguine Neural Networks");

        this.add(
            ModBlocks.VIRTUAL_SACRIFICER.get(), "Virtual Sacrificer"
        );

        this.add(
            ModItems.WRENCH.get(), "Wrench"
        );

        this.add(
            SanguineNeuralNetworks.MODID + ".container.virtual_sacrificer",
            "Virtual Sacrificer"
        );

        this.add(
            "gui." + SanguineNeuralNetworks.MODID + ".energy",
            "Energy: %s / %s"
        );

        this.add(
            "gui." + SanguineNeuralNetworks.MODID + ".consume",
            "Consuming: %s RF/t"
        );

        this.add(
            "gui." + SanguineNeuralNetworks.MODID + ".uses",
            "Catalyst Uses: %s / %s"
        );

        this.add(
            "gui." + SanguineNeuralNetworks.MODID + ".produce",
            "Blood / Operation: %s EV"
        );

        this.add(
            "gui." + SanguineNeuralNetworks.MODID + ".noAltar",
            "Ara Vitae is missing!"
        );

        this.add(
            "gui." + SanguineNeuralNetworks.MODID + ".noModel",
            "Missing data model!"
        );

        this.add(
            "gui." + SanguineNeuralNetworks.MODID + ".cModifier",
            "Mult: %sx"
        );

        this.add(
            "gui." + SanguineNeuralNetworks.MODID + ".aModifier",
            "Altar: %sx"
        );

        this.add(
            "item." + SanguineNeuralNetworks.MODID + ".savedAltar",
            "Saved Ara Vitae Pos: %s x, %s y, %s z"
        );

        this.add(
            "item." + SanguineNeuralNetworks.MODID + ".emptyAltar",
            "Ara Vitae Pos is empty, select an altar before"
        );

        this.add(
            "item." + SanguineNeuralNetworks.MODID + ".setSacrificer",
            "Successfully saved Ara Vitae in the virtual sacrificer"
        );

        this.add(
            "item." + SanguineNeuralNetworks.MODID + ".invalidPos",
            "Position is not a valid selection: %s x, %s y, %s z"
        );

        this.add(
            "item." + SanguineNeuralNetworks.MODID + ".wandUse",
            "First shift r-click on the Ara Vitae, then shift r-click on a Virtual Sacrificer or an output port"
        );

        this.add(
            "jei."+ SanguineNeuralNetworks.MODID + ".blood",
            "Blood: %s EV"
        );

        this.add(
            "jei."+ SanguineNeuralNetworks.MODID + ".energy",
            "Energy: %sRF/t"
        );

        this.add(
            "jei."+ SanguineNeuralNetworks.MODID + ".mult",
            "Mult: %sx"
        );

        this.add(
            "jei."+ SanguineNeuralNetworks.MODID + ".uses",
            "Uses: %s"
        );

        this.add(
            SanguineNeuralNetworks.MODID +  ".container.vsacrificer.catalyst",
            "Virtual Sacrificer Catalysts"
        );
    }

    /**
     * Capitalizes first letter of a string
     *
     * @param input the string to capitalize e.g. "alpha"
     * @return the string capitalized e.g. "Alpha"
     */
    public static String cFL(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}
