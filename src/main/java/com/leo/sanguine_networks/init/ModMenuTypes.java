package com.leo.sanguine_networks.init;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.block.menu.VSacrificerMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, SanguineNeuralNetworks.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<VSacrificerMenu>> V_SACRIFICER_MENU = registerMenuType(VSacrificerMenu::new, "vsacrificer_menu");

    public static final DeferredHolder<MenuType<?>, MenuType<com.leo.sanguine_networks.block.menu.SufferingMenu>> SUFFERING_MENU =
        registerMenuType(com.leo.sanguine_networks.block.menu.SufferingMenu::new, "suffering_incorporated");

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }
}
