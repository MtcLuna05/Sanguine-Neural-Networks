package com.leo.sanguine_networks.client.screen;

import com.leo.sanguine_networks.block.entity.SufferingBlockEntity;
import com.leo.sanguine_networks.block.menu.SufferingMenu;
import com.leo.sanguine_networks.client.SnnPalette;
import dev.shadowsoffire.placebo.screen.PlaceboContainerScreen;
import dev.shadowsoffire.placebo.util.DrawsOnLeft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** HNN Data Center layout and artwork, with catalysts in its input row and fluid in its output area.
 * Adapted from HNN 6.5.1; see META-INF/licenses/Hostile-Neural-Networks.txt.
 */
public class SufferingScreen extends PlaceboContainerScreen<SufferingMenu> implements DrawsOnLeft {
    private static final ResourceLocation BASE = ResourceLocation.parse("sanguine_networks:textures/gui/suffering_incorporated.png");
    private static final ResourceLocation PLAYER = ResourceLocation.parse("sanguine_networks:textures/gui/player_inventory.png");
    private static final int ENERGY_X = 213, ENERGY_Y = 19, ENERGY_W = 7, ENERGY_H = 100;
    public SufferingScreen(SufferingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = SufferingMenu.WIDTH;
        imageHeight = SufferingMenu.HEIGHT;
        inventoryLabelX = 35;
        inventoryLabelY = 134;
    }
    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        g.blit(BASE, x, y, 0, 0, 230, 128, 256, 256);
        int energy = scaled(menu.getData().get(1), menu.getData().get(2), ENERGY_H);
        g.blit(BASE, x + ENERGY_X, y + ENERGY_Y + ENERGY_H - energy, 231, 0, ENERGY_W, energy, 256, 256);
        for (int i = 0; i < 25; i++) {
            int status = menu.getData().get(43 + i);
            if (status == SufferingBlockEntity.EMPTY) continue;
            int progress = menu.getData().get(10 + i);
            if (status == SufferingBlockEntity.RUNNING && progress == 0) continue;
            int width = status == SufferingBlockEntity.RUNNING ? scaled(progress, menu.getData().get(9), 16) : 16;
            g.blit(BASE, x + 9 + i % 5 * 18, y + 37 + i / 5 * 20,
                0, status == SufferingBlockEntity.RUNNING ? 129 : 131, width, 2, 256, 256);
        }
        // The four inputs occupy HNN's exact coordinates; remaining charges use its progress sprite.
        for (int i = 0; i < 4; i++) {
            int uses = menu.getData().get(35 + i);
            int width = uses == -1 ? 16 : scaled(uses, menu.getData().get(39 + i), 16);
            g.blit(BASE, x + 132 + 18 * i, y + 38, 0, 129, width, 2, 256, 256);
        }
        FluidDisplay.draw(g, x + 132, y + 48, 70, 70, menu.getData().get(3), menu.getData().get(4));
        g.blit(BASE, x - 22, y, 18, 138, 18, 18, 256, 256);
        g.blit(menu.getRedstoneState().getResourceLocation(), x - 21, y + 1, 0, 0, 16, 16, 16, 16);
        g.blit(PLAYER, x + 27, y + SufferingMenu.PLAYER_PANEL_Y, 0, 0, 176, 90, 256, 256);
    }
    private static int scaled(int amount, int capacity, int pixels) {
        return capacity <= 0 ? 0 : (int) Math.clamp((long) amount * pixels / capacity, 0, pixels);
    }
    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, Component.translatable("sanguine_networks.suffering.short"), 8, 6, SnnPalette.HIGHLIGHT, false);
        g.drawString(font, Component.translatable("hostilenetworks.gui.data_center.io"), 130, 6, SnnPalette.HIGHLIGHT, false);
    }
    @Override protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        if (isHovering(ENERGY_X, ENERGY_Y, ENERGY_W, ENERGY_H, mouseX, mouseY))
            g.renderTooltip(font, List.of(Component.translatable("hostilenetworks.gui.energy", menu.getData().get(1), menu.getData().get(2)),
                Component.literal(menu.getData().get(6) + " FE/t"), Component.translatable("sanguine_networks.suffering.energy_rule")),
                Optional.empty(), mouseX, mouseY);
        if (isHovering(-22, 0, 18, 18, mouseX, mouseY))
            g.renderTooltip(font, Component.translatable(menu.getRedstoneState().getKey()), mouseX, mouseY);
        if (isHovering(132, 48, 70, 70, mouseX, mouseY))
            g.renderTooltip(font, List.of(Component.translatable("sanguine_networks.suffering.vitae"),
                Component.literal(menu.getData().get(3) + " / " + menu.getData().get(4) + " mB"),
                Component.translatable("sanguine_networks.suffering.output")), Optional.empty(), mouseX, mouseY);
        List<Component> details = new ArrayList<>();
        if (menu.getData().get(0) == -1) {
            details.add(Component.translatable("sanguine_networks.suffering.status.8").withColor(SnnPalette.ERROR));
        } else if (menu.getData().get(0) == 0) {
            details.add(Component.translatable("hostilenetworks.fail.shell_broken").withColor(SnnPalette.ERROR));
            details.add(Component.translatable("sanguine_networks.suffering.structure"));
            details.add(Component.translatable("sanguine_networks.suffering.runes"));
        } else {
            for (int i = 0; i < 29; i++) {
                var slot = menu.slots.get(i);
                if (!isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) continue;
                if (slot.hasItem()) details.add(slot.getItem().getHoverName());
                if (i < 25 && slot.hasItem()) {
                    int status = menu.getData().get(43 + i);
                    details.add(Component.translatable("sanguine_networks.suffering.status." + status)
                        .withColor(status == SufferingBlockEntity.RUNNING ? SnnPalette.HIGHLIGHT : SnnPalette.ERROR));
                    details.add(Component.translatable("hostilenetworks.gui.data_center.progress",
                        String.format(Locale.ROOT, "%.1f", menu.getData().get(10 + i) / 20f),
                        String.format(Locale.ROOT, "%.1f", menu.getData().get(9) / 20f)).withColor(SnnPalette.HIGHLIGHT));
                    details.add(Component.translatable("gui.sanguine_networks.cModifier", menu.getData().get(7) / 1000f).withColor(SnnPalette.HIGHLIGHT));
                } else if (i >= 25) {
                    int uses = menu.getData().get(35 + i - 25);
                    details.add(Component.translatable("jei.sanguine_networks.uses", uses == -1 ? "∞" : uses).withColor(SnnPalette.HIGHLIGHT));
                    details.add(Component.translatable("sanguine_networks.suffering.boost_rule"));
                }
                break;
            }
        }
        if (!details.isEmpty()) {
            g.pose().pushPose();
            g.pose().translate(-3, 0, 0);
            drawOnLeft(g, details, topPos + 40, Math.min(leftPos, 240));
            g.pose().popPose();
        }
        super.renderTooltip(g, mouseX, mouseY);
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        if (isHovering(-22, 0, 18, 18, x, y)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2000 + menu.getRedstoneState().next().ordinal());
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            return true;
        }
        return super.mouseClicked(x, y, button);
    }
}
