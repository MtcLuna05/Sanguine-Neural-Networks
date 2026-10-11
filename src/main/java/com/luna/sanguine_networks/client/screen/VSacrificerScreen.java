package com.luna.sanguine_networks.client.screen;

import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.block.menu.VSacrificerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;

import java.util.List;
import java.util.Optional;

public class VSacrificerScreen extends AbstractContainerScreen<VSacrificerMenu> {

	public static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(SanguineNeuralNetworks.MODID, "textures/gui/virtual_sacrificer_bg.png");

    public VSacrificerScreen(VSacrificerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
	    super(pMenu, pPlayerInventory, pTitle, 176, VSacrificerMenu.SCREEN_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        inventoryLabelY = 10000;
        titleLabelY = 10000;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

	    guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
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

	    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse("sanguine_networks:textures/gui/player_inventory.png"),
            x, y + VSacrificerMenu.PLAYER_PANEL_Y, 0, 0, 176, 90, 256, 256);

	    ItemStacksResourceHandler inventory = menu.getBlockEntity().getInventory();

	    if (ItemUtil.getStack(inventory, 0).isEmpty()) {
		    guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
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

	    if (ItemUtil.getStack(inventory, 1).isEmpty()) {
		    guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
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

	    guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
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

	    guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
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
    public void extractRenderState(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
	    super.extractRenderState(pGuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);

        Component toDisplay;

        if(menu.getData().get(9) == 1){
            toDisplay = Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".noModel");
        } else if (menu.getData().get(8) == 0) {
            toDisplay = Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".noAltar");
        } else {
            int toProduce = menu.getData().get(6);
            toDisplay = Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".produce", toProduce);
        }

	    pGuiGraphicsExtractor.centeredText(
            font,
            toDisplay,
            leftPos + 87,
            topPos + 17,
            0xFFFF0000
        );

	    pGuiGraphicsExtractor.text(
            font,
            Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".cModifier", menu.getData().get(7) / 1000f),
            leftPos + 101,
            topPos + 69,
            0xFFFF0000
        );

	    pGuiGraphicsExtractor.text(
            font,
            Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".aModifier", menu.getData().get(10) / 1000f),
            leftPos + 16,
            topPos + 69,
            0xFFFF0000
        );

    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor pGuiGraphicsExtractor, int pX, int pY) {
        if(isMouseHovering(leftPos + 159, topPos + 8, 9, 73, pX, pY)) {
            int energy = menu.getData().get(0), maxEnergy = menu.getData().get(1);
	        pGuiGraphicsExtractor.setTooltipForNextFrame(
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
	        pGuiGraphicsExtractor.setTooltipForNextFrame(
                font,
                uses == -1 ? Component.translatable("jei.sanguine_networks.uses", "∞")
                    : Component.translatable("gui." + SanguineNeuralNetworks.MODID + ".uses", uses, maxUses),
                pX,
                pY
            );
        }

	    super.extractTooltip(pGuiGraphicsExtractor, pX, pY);
    }

    public static boolean isMouseHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
