package com.luna.sanguine_networks.client.screen;

import com.breakinblocks.neovitae.common.fluid.NVFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.neoforge.fluids.FluidStack;


/** Draws the actual animated NeoVitae fluid sprite, clipped to the filled tank area. */
final class FluidDisplay {
    private FluidDisplay() {}

	static void draw(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int amount, int capacity) {
        if (amount <= 0 || capacity <= 0) return;
        int filled = (int) Math.clamp((long) amount * height / capacity, 1, height);
		var fluid = NVFluids.ESSENTIA_VITAE_SOURCE.get();
		var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
		var sprite = model.stillMaterial().sprite();
		int tint = model.fluidTintSource() == null ? -1 : model.fluidTintSource().colorAsStack(new FluidStack(fluid, amount));
        graphics.enableScissor(x, y + height - filled, x + width, y + height);
        try {
            for (int dx = 0; dx < width; dx += 16) for (int dy = 0; dy < filled; dy += 16)
	            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + dx, y + height - dy - 16, 16, 16, tint);
        } finally { graphics.disableScissor(); }
    }
}
