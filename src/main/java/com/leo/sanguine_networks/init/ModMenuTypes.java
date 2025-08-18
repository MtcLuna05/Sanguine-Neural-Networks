package com.leo.sanguine_networks.init;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.block.menu.VSacrificerMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SanguineNeuralNetworks.MODID);

    public static final RegistryObject<MenuType<VSacrificerMenu>> V_SACRIFICER_MENU = registerMenuType(VSacrificerMenu::new, "vsacrificer_menu");

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }
}
