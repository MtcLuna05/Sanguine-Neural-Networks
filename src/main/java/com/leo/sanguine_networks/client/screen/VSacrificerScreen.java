package com.leo.sanguine_networks.client.screen;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.block.menu.VSacrificerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.Optional;

public class VSacrificerScreen extends AbstractContainerScreen<VSacrificerMenu> {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(SanguineNeuralNetworks.MODID, "textures/gui/virtual_sacrificer_bg.png");

    public VSacrificerScreen(VSacrificerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        imageHeight = VSacrificerMenu.SCREEN_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        inventoryLabelY = 10000;
        titleLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(
            BACKGROUND,
            x,
            y,
            0,
            0,
            176,
            VSacrificerMenu.MACHINE_PANEL_HEIGHT,
            256,
            256
        );

        guiGraphics.blit(new ResourceLocation("sanguine_networks:textures/gui/player_inventory.png"),
            x, y + VSacrificerMenu.PLAYER_PANEL_Y, 0, 0, 176, 90, 256, 256);

        ItemStackHandler inventory = menu.getBlockEntity().getInventory();

        if(inventory.getStackInSlot(0).isEmpty()) {
            guiGraphics.blit(
                BACKGROUND,
                leftPos + 17,
                topPos + 36,
                0,
                178,
                16,
                16,
                256,
                256
            );
        }

        if(inventory.getStackInSlot(1).isEmpty()) {
            guiGraphics.blit(
                BACKGROUND,
                leftPos + 80,
                topPos + 64,
                16,
                178,
                16,
                16,
                256,
                256
            );
        }

        int energy = menu.getData().get(0), maxEnergy = menu.getData().get(1);
        int scaledEnergy = energy != 0 && maxEnergy != 0 ? (int) ((long) energy * 71 / maxEnergy): 0;

        guiGraphics.blit(
            BACKGROUND,
            leftPos + 160,
            topPos + 80 - scaledEnergy,
            176,
            9,
            7,
            scaledEnergy,
            256,
            256
        );

        int progress = menu.getData().get(4), maxProgress = menu.getData().get(5);
        int scaledProgress = progress != 0 && maxProgress != 0? (int) ((long) progress * 111 / maxProgress): 0;

        guiGraphics.blit(
            BACKGROUND,
            leftPos + 41,
            topPos + 41,
            41,
            178,
            scaledProgress,
            7,
            256,
            256
        );

        int uses = menu.getData().get(2), maxUses = menu.getData().get(3);
        int scaledUses = uses != 0 && maxUses != 0? (int) ((long) uses * 18 / maxUses): 0;

        guiGraphics.fill(
            leftPos + 79,
            topPos + 61,
            leftPos + 79 + scaledUses,
            topPos + 62,
            0xFFFF00AA
        );
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        Component toDisplay;

        if(menu.getData().get(9) == 1){
            toDisplay = Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".noModel");
        } else if (menu.getData().get(8) == 0) {
            toDisplay = Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".noAltar");
        } else {
            int toProduce = menu.getData().get(6);
            toDisplay = Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".produce", toProduce);
        }

        pGuiGraphics.drawCenteredString(
            font,
            toDisplay,
            leftPos + 87,
            topPos + 17,
            0xFFFF0000
        );

        pGuiGraphics.drawString(
            font,
            Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".cModifier", menu.getData().get(7) / 1000f),
            leftPos + 101,
            topPos + 69,
            0xFFFF0000
        );

        pGuiGraphics.drawString(
            font,
            Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".aModifier", menu.getData().get(10) / 1000f),
            leftPos + 16,
            topPos + 69,
            0xFFFF0000
        );

        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        if(isMouseHovering(leftPos + 159, topPos + 8, 9, 73, pX, pY)) {
            int energy = menu.getData().get(0), maxEnergy = menu.getData().get(1);
            pGuiGraphics.renderTooltip(
                font,
                List.of(
                    Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".energy", energy, maxEnergy),
                    Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".consume", menu.getBlockEntity().getRFTick())
                ),
                Optional.empty(),
                pX,
                pY
            );
        }

        if(isMouseHovering(leftPos + 79, topPos + 60, 18, 3, pX, pY)){
            int uses = menu.getData().get(2), maxUses = menu.getData().get(3);
            pGuiGraphics.renderTooltip(
                font,
                uses == -1 ? Component.translatable("jei.sanguine_networks.uses", "∞")
                    : Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".uses", uses, maxUses),
                pX,
                pY
            );
        }

        super.renderTooltip(pGuiGraphics, pX, pY);
    }

    public static boolean isMouseHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
