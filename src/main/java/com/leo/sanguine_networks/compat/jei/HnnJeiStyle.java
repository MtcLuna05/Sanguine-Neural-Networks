package com.leo.sanguine_networks.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** HNN's original simulator artwork, with only its colored pixels replaced by our red palette. */
final class HnnJeiStyle implements IDrawable {
    static final int RED = com.leo.sanguine_networks.client.SnnPalette.HIGHLIGHT;
    private final IDrawable original;

    HnnJeiStyle(IGuiHelper helper) {
        original = helper.createDrawable(ResourceLocation.fromNamespaceAndPath("sanguine_networks", "textures/jei/virtual_sacrificer.png"), 0, 0, 116, 43);
    }

    @Override public int getWidth() { return 116; }
    @Override public int getHeight() { return 43; }
    @Override public void draw(GuiGraphics graphics, int x, int y) {
        original.draw(graphics, x, y);
    }

    static void drawProgress(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        int width = Mth.ceil(35F * (mc.level.getGameTime() % 40 + mc.getTimer().getGameTimeDeltaPartialTick(true)) / 40);
        graphics.blit(ResourceLocation.parse("sanguine_networks:textures/jei/virtual_sacrificer.png"), 52, 9, 0, 43, width, 6, 256, 256);
    }
}
