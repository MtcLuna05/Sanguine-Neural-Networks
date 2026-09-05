package com.leo.sanguine_networks.client.renderer;

// Adapted from HNN 6.5.1 DataCenterRenderer; see META-INF/licenses/Hostile-Neural-Networks.txt.
import dev.shadowsoffire.hostilenetworks.client.WeirdRenderThings;
import com.leo.sanguine_networks.client.SnnPalette;

import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.hostilenetworks.multiblock.DataCenterShell;
import com.leo.sanguine_networks.block.entity.SufferingBlockEntity;
import dev.shadowsoffire.hostilenetworks.util.ClientEntityCache;
import dev.shadowsoffire.hostilenetworks.util.DisplayEntity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class SufferingRenderer implements BlockEntityRenderer<SufferingBlockEntity> {

    private static final int DISPLAY_SLOT_COUNT = 12;
    private final java.util.Map<SufferingBlockEntity, DisplayState> states = new java.util.WeakHashMap<>();
    private DisplayState state(SufferingBlockEntity tile) { return states.computeIfAbsent(tile, ignored -> new DisplayState()); }
    private static final class DisplayState {
        float[][] displaySlotPositions;
        float[] displaySlotPhaseOffsets;
        final int[] displaySlotCycleIdx = new int[DISPLAY_SLOT_COUNT];
        final int[] displaySlotEntityIdx = new int[DISPLAY_SLOT_COUNT];
        DisplayState() {
            java.util.Arrays.fill(displaySlotCycleIdx, Integer.MIN_VALUE);
            java.util.Arrays.fill(displaySlotEntityIdx, -1);
        }
    }

    private static final float OUTLINE_R = ((SnnPalette.PRIMARY >> 16) & 0xFF) / 255f;
    private static final float OUTLINE_G = ((SnnPalette.PRIMARY >> 8) & 0xFF) / 255f;
    private static final float OUTLINE_B = (SnnPalette.PRIMARY & 0xFF) / 255f;
    private static final float OUTLINE_A = 1.0f;

    private static final float WORLD_SCALE_FACTOR = 1.5f;
    private static final float DISPLAY_SCALE_FACTOR = 0.55f;
    private static final float CYCLE_TICKS = 100f;
    private static final float RAMP_FRACTION = 0.15f;
    private static final float HOLD_END_FRACTION = 0.45f;
    private static final float ALIVE_END_FRACTION = 0.60f;

    private static final long SEED_MIX_SLOT = 0x9E3779B97F4A7C15L;
    private static final long SEED_MIX_CYCLE = 0xC6BC279692B5C323L;

    private static final MultiBufferSource.BufferSource GHOST_BUFFER = MultiBufferSource.immediate(new ByteBufferBuilder(256));

    public SufferingRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public AABB getRenderBoundingBox(SufferingBlockEntity tile) {
        DataCenterShell.Layout layout = tile.getLayout();
        if (layout == null) return new AABB(tile.getBlockPos());
        BlockPos min = layout.shellMin();
        BlockPos max = layout.shellMax();
        return new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
    }

    @Override
    public void render(SufferingBlockEntity tile, float partialTick, PoseStack pose, MultiBufferSource bufs, int light, int overlay) {
        if (tile.getLayout() == null) return;
        DataCenterShell.Layout layout = tile.getLayout();
        if (layout == null) return;

        drawShellOutline(tile, layout, pose, bufs);
        drawDisplaySlots(tile, layout, partialTick, pose);
    }

    private void drawShellOutline(SufferingBlockEntity tile, DataCenterShell.Layout layout, PoseStack pose, MultiBufferSource bufs) {
        BlockPos here = tile.getBlockPos();
        double minX = layout.shellMin().getX() - here.getX();
        double minY = layout.shellMin().getY() - here.getY();
        double minZ = layout.shellMin().getZ() - here.getZ();
        double maxX = layout.shellMax().getX() - here.getX() + 1;
        double maxY = layout.shellMax().getY() - here.getY() + 1;
        double maxZ = layout.shellMax().getZ() - here.getZ() + 1;

        VertexConsumer lines = bufs.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(pose, lines, minX, minY, minZ, maxX, maxY, maxZ, OUTLINE_R, OUTLINE_G, OUTLINE_B, OUTLINE_A);
    }

    @SuppressWarnings("deprecation")
    private void drawDisplaySlots(SufferingBlockEntity tile, DataCenterShell.Layout layout, float partialTick, PoseStack pose) {
        if (state(tile).displaySlotPositions == null) generateSlotLayout(tile);

        int activeMask = tile.getActiveSlotsMask();
        if (activeMask == 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        BlockPos here = tile.getBlockPos();
        BlockPos center = layout.centerPos();
        float cx = (center.getX() - here.getX()) + 0.5f;
        float cy = (center.getY() - here.getY()) + 0.5f;
        float cz = (center.getZ() - here.getZ()) + 0.5f;

        float baseSpin = ((mc.player != null ? mc.player.tickCount : 0) + partialTick) * 2f;
        float now = mc.level.getGameTime() + partialTick;

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        WeirdRenderThings.translucent = true;
        try {
            final float pt = partialTick;
            final int finalActiveMask = activeMask;
            RenderSystem.runAsFancy(() -> {
                for (int i = 0; i < DISPLAY_SLOT_COUNT; i++) {
                    drawOneDisplaySlot(tile, i, finalActiveMask, now, baseSpin, cx, cy, cz, pt, pose, dispatcher, mc);
                }
            });
            GHOST_BUFFER.endBatch();
        }
        finally {
            WeirdRenderThings.translucent = false;
            dispatcher.setRenderShadow(true);
        }
    }

    private void drawOneDisplaySlot(SufferingBlockEntity tile, int slotIdx, int activeMask, float now, float baseSpin,
        float cx, float cy, float cz, float partialTick, PoseStack pose, EntityRenderDispatcher dispatcher, Minecraft mc) {

        float localTime = now - state(tile).displaySlotPhaseOffsets[slotIdx];
        int cycleIdx = Mth.floor(localTime / CYCLE_TICKS);
        float t = (localTime - cycleIdx * CYCLE_TICKS) / CYCLE_TICKS;
        float envelope = envelopeAt(t);
        if (envelope <= 0.001f) return;

        // Picked entity index is locked for the full cycle; if the slot empties mid-cycle we just stop rendering it.
        if (state(tile).displaySlotCycleIdx[slotIdx] != cycleIdx) {
            state(tile).displaySlotCycleIdx[slotIdx] = cycleIdx;
            state(tile).displaySlotEntityIdx[slotIdx] = pickEntityForCycle(activeMask, tile.getBlockPos(), slotIdx, cycleIdx);
        }
        int entityIdx = state(tile).displaySlotEntityIdx[slotIdx];
        if (entityIdx < 0 || entityIdx >= SufferingBlockEntity.MODEL_SLOTS) return;

        ItemStack stack = tile.getInventory().getStackInSlot(entityIdx);
        DynamicHolder<DataModel> model = DataModelItem.getStoredModel(stack);
        if (!model.isBound()) return;
        DisplayEntity display = model.get().displayEntity(mc.level);
        Entity ent = ClientEntityCache.computeIfAbsent(display, mc.level);
        if (ent == null) return;

        if (mc.player != null) ent.tickCount = mc.player.tickCount;

        ent.setYRot(0);
        if (ent instanceof LivingEntity living) {
            living.yBodyRot = 0;
            living.yBodyRotO = 0;
            living.yHeadRot = 0;
            living.yHeadRotO = 0;
        }

        float[] off = state(tile).displaySlotPositions[slotIdx];
        float scale = WORLD_SCALE_FACTOR * DISPLAY_SCALE_FACTOR * display.scale() * envelope;
        float spin = baseSpin + (slotIdx * 360f / DISPLAY_SLOT_COUNT);

        pose.pushPose();
        pose.translate(cx + off[0], cy + off[1], cz + off[2]);
        pose.scale(scale, scale, scale);
        pose.mulPose(Axis.YP.rotationDegrees(spin));
        dispatcher.render(ent, display.xOffset(), display.yOffset(), display.zOffset(),
            0f, partialTick, pose, type -> new RedGhost(GHOST_BUFFER.getBuffer(type)), 0xF000F0);
        pose.popPose();
    }

    private static float envelopeAt(float t) {
        if (t < RAMP_FRACTION) return t / RAMP_FRACTION;
        if (t < HOLD_END_FRACTION) return 1f;
        if (t < ALIVE_END_FRACTION) return (ALIVE_END_FRACTION - t) / RAMP_FRACTION;
        return 0f;
    }

    private static int pickEntityForCycle(int activeMask, BlockPos pos, int slotIdx, int cycleIdx) {
        if (activeMask == 0) return -1;
        int count = Integer.bitCount(activeMask);
        long seed = pos.asLong() ^ (slotIdx * SEED_MIX_SLOT) ^ (cycleIdx * SEED_MIX_CYCLE);
        int pick = new Random(seed).nextInt(count);
        int seen = 0;
        for (int i = 0; i < SufferingBlockEntity.MODEL_SLOTS; i++) {
            if ((activeMask & (1 << i)) == 0) continue;
            if (seen == pick) return i;
            seen++;
        }
        return -1;
    }

    private void generateSlotLayout(SufferingBlockEntity tile) {
        BlockPos pos = tile.getBlockPos();
        Random rng = new Random(pos.asLong());
        float[][] positions = new float[DISPLAY_SLOT_COUNT][3];
        float[] phases = new float[DISPLAY_SLOT_COUNT];
        int idx = 0;
        for (int xi = 0; xi < 3; xi++) {
            for (int yi = 0; yi < 2; yi++) {
                for (int zi = 0; zi < 2; zi++) {
                    float cellCx = -1.33f + xi * 1.33f;
                    float cellCy = -1f + yi * 2f;
                    float cellCz = -1f + zi * 2f;
                    positions[idx][0] = cellCx + (rng.nextFloat() - 0.5f) * 0.8f;
                    positions[idx][1] = cellCy + (rng.nextFloat() - 0.5f) * 1.4f;
                    positions[idx][2] = cellCz + (rng.nextFloat() - 0.5f) * 1.4f;
                    phases[idx] = rng.nextFloat() * CYCLE_TICKS;
                    idx++;
                }
            }
        }
        state(tile).displaySlotPositions = positions;
        state(tile).displaySlotPhaseOffsets = phases;
    }
    private record RedGhost(VertexConsumer delegate) implements VertexConsumer {
        @Override public VertexConsumer addVertex(float x, float y, float z) { delegate.addVertex(x, y, z); return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { delegate.setColor(r * ((SnnPalette.HIGHLIGHT >> 16) & 255) / 255, g * ((SnnPalette.HIGHLIGHT >> 8) & 255) / 255, b * (SnnPalette.HIGHLIGHT & 255) / 255, a * 187 / 255); return this; }
        @Override public VertexConsumer setUv(float u, float v) { delegate.setUv(u, v); return this; }
        @Override public VertexConsumer setUv1(int u, int v) { delegate.setUv1(u, v); return this; }
        @Override public VertexConsumer setUv2(int u, int v) { delegate.setUv2(u, v); return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { delegate.setNormal(x, y, z); return this; }
    }
}
