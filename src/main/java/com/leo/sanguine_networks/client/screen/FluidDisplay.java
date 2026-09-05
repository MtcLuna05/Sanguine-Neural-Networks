package com.leo.sanguine_networks.client.screen;

import com.breakinblocks.neovitae.common.fluid.NVFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/** Draws the actual animated NeoVitae fluid sprite, clipped to the filled tank area. */
final class FluidDisplay {
    private FluidDisplay() {}
    static void draw(GuiGraphics graphics, int x, int y, int width, int height, int amount, int capacity) {
        if (amount <= 0 || capacity <= 0) return;
        int filled = (int) Math.clamp((long) amount * height / capacity, 1, height);
        var fluid = IClientFluidTypeExtensions.of(NVFluids.ESSENTIA_VITAE_SOURCE.get());
        var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluid.getStillTexture());
        int tint = fluid.getTintColor();
        graphics.enableScissor(x, y + height - filled, x + width, y + height);
        try {
            for (int dx = 0; dx < width; dx += 16) for (int dy = 0; dy < filled; dy += 16)
                graphics.blit(x + dx, y + height - dy - 16, 0, 16, 16, sprite,
                    ((tint >> 16) & 255) / 255f, ((tint >> 8) & 255) / 255f, (tint & 255) / 255f, ((tint >>> 24) & 255) / 255f);
        } finally { graphics.disableScissor(); }
    }
}
