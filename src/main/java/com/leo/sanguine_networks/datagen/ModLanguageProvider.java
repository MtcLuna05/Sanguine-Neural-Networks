package com.leo.sanguine_networks.datagen;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.init.ModBlocks;
import com.leo.sanguine_networks.init.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, SanguineNeuralNetworks.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("jei.sanguine_networks.combined_models", "Virtual Sacrificer only: sums blood from all four inner models at the Extra HNN tier. Power: 10× the sum of their recipe costs. Consumes one catalyst use per completed cycle.");

        this.add("jei.sanguine_networks.optional_catalyst", "Optional catalyst (blank = no bonus)");
        this.add("jei.sanguine_networks.lp_conversion", "1 mB Life Essence = 1 LP");
        this.add("jei.sanguine_networks.before_runes", "Before altar rune bonuses");
        this.add("jei.sanguine_networks.catalyst_effect", "Multiplies blood production; amount depends on the model");
        this.add("item.sanguine_networks.unavailableAltar", "Saved Blood Altar is missing, unloaded, or in another dimension");
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
            "Blood / Operation: %s LP"
        );

        this.add(
            "gui." + SanguineNeuralNetworks.MODID + ".noAltar",
            "Blood Altar is missing!"
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
            "Saved Blood Altar Pos: %s x, %s y, %s z"
        );

        this.add(
            "item." + SanguineNeuralNetworks.MODID + ".emptyAltar",
            "Blood Altar Pos is empty, select an altar before"
        );

        this.add(
            "item." + SanguineNeuralNetworks.MODID + ".setSacrificer",
            "Successfully saved Blood Altar in the virtual sacrificer"
        );

        this.add(
            "item." + SanguineNeuralNetworks.MODID + ".invalidPos",
            "Position is not a valid selection: %s x, %s y, %s z"
        );

        this.add(
            "item." + SanguineNeuralNetworks.MODID + ".wandUse",
            "First shift r-click on the Blood Altar, then shift r-click on a Virtual Sacrificer"
        );

        this.add(
            "jei."+ SanguineNeuralNetworks.MODID + ".blood",
            "Blood: %s LP"
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
