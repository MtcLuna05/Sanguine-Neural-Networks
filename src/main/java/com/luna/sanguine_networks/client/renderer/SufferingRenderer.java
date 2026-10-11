package com.luna.sanguine_networks.client.renderer;

import com.luna.sanguine_networks.block.entity.SufferingBlockEntity;
import com.luna.sanguine_networks.client.SnnPalette;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.shadowsoffire.hostilenetworks.client.ghost.GhostCollector;
import dev.shadowsoffire.hostilenetworks.client.ghost.GhostEntityStates;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.hostilenetworks.multiblock.DataCenterShell;
import dev.shadowsoffire.hostilenetworks.util.ClientEntityCache;
import dev.shadowsoffire.hostilenetworks.util.DisplayEntity;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class SufferingRenderer implements BlockEntityRenderer<SufferingBlockEntity, SufferingRenderer.State> {

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

	private static final int OUTLINE_COLOR = ARGB.opaque(SnnPalette.PRIMARY);
	private static final int GHOST_ALPHA = 0xBB;

    private static final float WORLD_SCALE_FACTOR = 1.5f;
    private static final float DISPLAY_SCALE_FACTOR = 0.55f;
    private static final float CYCLE_TICKS = 100f;
    private static final float RAMP_FRACTION = 0.15f;
    private static final float HOLD_END_FRACTION = 0.45f;
    private static final float ALIVE_END_FRACTION = 0.60f;

    private static final long SEED_MIX_SLOT = 0x9E3779B97F4A7C15L;
    private static final long SEED_MIX_CYCLE = 0xC6BC279692B5C323L;

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
    public State createRenderState() {
	    return new State();
    }

	@Override
	public void extractRenderState(SufferingBlockEntity tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, crumbling);
		state.outline = null;
		state.displays.clear();

        DataCenterShell.Layout layout = tile.getLayout();
        if (layout == null) return;

        BlockPos here = tile.getBlockPos();
		state.outline = new AABB(
				layout.shellMin().getX() - here.getX(), layout.shellMin().getY() - here.getY(), layout.shellMin().getZ() - here.getZ(),
				layout.shellMax().getX() - here.getX() + 1, layout.shellMax().getY() - here.getY() + 1, layout.shellMax().getZ() - here.getZ() + 1);

		this.extractDisplaySlots(tile, state, layout, partialTicks);
    }

	private void extractDisplaySlots(SufferingBlockEntity tile, State state, DataCenterShell.Layout layout, float partialTick) {
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

		for (int slotIdx = 0; slotIdx < DISPLAY_SLOT_COUNT; slotIdx++) {
			float localTime = now - state(tile).displaySlotPhaseOffsets[slotIdx];
			int cycleIdx = Mth.floor(localTime / CYCLE_TICKS);
			float t = (localTime - cycleIdx * CYCLE_TICKS) / CYCLE_TICKS;
			float envelope = envelopeAt(t);
			if (envelope <= 0.001f) continue;

			if (state(tile).displaySlotCycleIdx[slotIdx] != cycleIdx) {
				state(tile).displaySlotCycleIdx[slotIdx] = cycleIdx;
				state(tile).displaySlotEntityIdx[slotIdx] = pickEntityForCycle(activeMask, here, slotIdx, cycleIdx);
			}
			int entityIdx = state(tile).displaySlotEntityIdx[slotIdx];
			if (entityIdx < 0 || entityIdx >= SufferingBlockEntity.MODEL_SLOTS) continue;

			ItemStack stack = ItemUtil.getStack(tile.getInventory(), entityIdx);
			DynamicHolder<DataModel> model = DataModelItem.getStoredModel(stack);
			if (!model.isBound()) continue;
			DisplayEntity display = model.get().displayEntity(mc.level);
			Entity ent = ClientEntityCache.computeIfAbsent(display, mc.level);
			if (ent == null) continue;

			EntityRenderState renderState = GhostEntityStates.extract(ent, partialTick);
			if (renderState == null) continue;

			float[] off = state(tile).displaySlotPositions[slotIdx];
			float scale = WORLD_SCALE_FACTOR * DISPLAY_SCALE_FACTOR * display.scale() * envelope;
			float spin = baseSpin + (slotIdx * 360f / DISPLAY_SLOT_COUNT);
			state.displays.add(new SlotDisplay(renderState, cx + off[0], cy + off[1], cz + off[2], scale, spin, display));
		}
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.outline != null) {
			AABB box = state.outline;
			collector.submitCustomGeometry(pose, RenderTypes.lines(), (p, buffer) -> {
				PoseStack stack = new PoseStack();
				stack.last().pose().set(p.pose());
				stack.last().normal().set(p.normal());
				ShapeRenderer.renderShape(stack, buffer, Shapes.create(box), 0, 0, 0, OUTLINE_COLOR, 1.0F);
			});
		}

		if (state.displays.isEmpty()) return;
		EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		RedGhostCollector ghost = new RedGhostCollector(collector, GHOST_ALPHA);
		for (SlotDisplay slot : state.displays) {
			pose.pushPose();
			pose.translate(slot.x(), slot.y(), slot.z());
			pose.scale(slot.scale(), slot.scale(), slot.scale());
			pose.mulPose(Axis.YP.rotationDegrees(slot.spin()));
			dispatcher.submit(slot.state(), camera, slot.display().xOffset(), slot.display().yOffset(), slot.display().zOffset(), pose, ghost);
			pose.popPose();
		}
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

	public record SlotDisplay(EntityRenderState state, float x, float y, float z, float scale, float spin,
	                          DisplayEntity display) {
	}

	public static class State extends BlockEntityRenderState {
		@Nullable
		public AABB outline;
		public final List<SlotDisplay> displays = new ArrayList<>();
	}

	private static final class RedGhostCollector implements SubmitNodeCollector {

		private final SubmitNodeCollector root;
		private final OrderedSubmitNodeCollector inner;
		private final int alpha;

		public RedGhostCollector(SubmitNodeCollector root, int alpha) {
			this(root, root, alpha);
		}

		private RedGhostCollector(SubmitNodeCollector root, OrderedSubmitNodeCollector inner, int alpha) {
			this.root = root;
			this.inner = inner;
			this.alpha = alpha & 0xFF;
		}

		@Override
		public OrderedSubmitNodeCollector order(int order) {
			return new RedGhostCollector(this.root, this.root.order(order), this.alpha);
		}

		@Override
		public <S> void submitModel(Model<? super S> model, S state, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords, int tintedColor,
		                            @Nullable TextureAtlasSprite sprite, int outlineColor, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
			this.inner.submitModel(model, state, poseStack, GhostCollector.ghostType(renderType), lightCoords, overlayCoords, this.ghostColor(tintedColor), sprite, 0, null);
		}

		@Override
		public void submitModelPart(ModelPart modelPart, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords, @Nullable TextureAtlasSprite sprite,
		                            boolean sheeted, boolean hasFoil, int tintedColor, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, int outlineColor) {
			this.inner.submitModelPart(modelPart, poseStack, GhostCollector.ghostType(renderType), lightCoords, overlayCoords, sprite, sheeted, false, this.ghostColor(tintedColor), null, 0);
		}

		@Override
		public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer) {
			int alpha = this.alpha;
			this.inner.submitCustomGeometry(poseStack, GhostCollector.ghostType(renderType), (pose, buffer) -> customGeometryRenderer.render(pose, new RedGhost(buffer, alpha)));
		}

		@Override
		public void submitBlockModel(PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> parts, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
			this.inner.submitBlockModel(poseStack, GhostCollector.ghostType(renderType), parts, this.ghostTints(tintLayers), lightCoords, overlayCoords, 0);
		}

		@Override
		public void submitMultiLayerBlockModel(PoseStack poseStack, List<BlockStateModelPart> modelParts, boolean translucent, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
			this.inner.submitMultiLayerBlockModel(poseStack, modelParts, translucent, this.ghostTints(tintLayers), lightCoords, overlayCoords, 0);
		}

		@Override
		public void submitItem(PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads,
		                       ItemStackRenderState.FoilType foilType) {

			Map<RenderType, List<BakedQuad>> groups = new LinkedHashMap<>();
			for (BakedQuad quad : quads) {
				groups.computeIfAbsent(ghostItemType(quad), k -> new ArrayList<>()).add(quad);
			}

			for (Map.Entry<RenderType, List<BakedQuad>> entry : groups.entrySet()) {
				List<BakedQuad> group = entry.getValue();
				this.inner.submitCustomGeometry(poseStack, entry.getKey(), (pose, buffer) -> {
					QuadInstance instance = new QuadInstance();
					instance.setLightCoords(lightCoords);
					instance.setOverlayCoords(overlayCoords);
					for (BakedQuad quad : group) {
						BakedQuad.MaterialInfo material = quad.materialInfo();
						int tint = material.isTinted() && material.tintIndex() >= 0 && material.tintIndex() < tintLayers.length ? tintLayers[material.tintIndex()] : -1;
						instance.setColor(this.ghostColor(tint));
						buffer.putBakedQuad(pose, quad, instance);
					}
				});
			}
		}

		@Override
		public void submitShadow(PoseStack poseStack, float radius, List<EntityRenderState.ShadowPiece> pieces) {
		}

		@Override
		public void submitNameTag(PoseStack poseStack, @Nullable Vec3 nameTagAttachment, int offset, Component name, boolean seeThrough, int lightCoords, double distanceToCameraSq,
		                          CameraRenderState camera) {
		}

		@Override
		public void submitText(PoseStack poseStack, float x, float y, FormattedCharSequence string, boolean dropShadow, Font.DisplayMode displayMode, int lightCoords, int color,
		                       int backgroundColor, int outlineColor) {
		}

		@Override
		public void submitFlame(PoseStack poseStack, EntityRenderState renderState, Quaternionf rotation) {
		}

		@Override
		public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
		}

		@Override
		public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState) {
		}

		@Override
		public void submitBreakingBlockModel(PoseStack poseStack, BlockStateModel model, long seed, int progress) {
		}

		@Override
		public void submitParticleGroup(SubmitNodeCollector.ParticleGroupRenderer particleGroupRenderer) {
		}

		private int ghostColor(int color) {
			return ARGB.multiply(color, ARGB.color(this.alpha, SnnPalette.HIGHLIGHT));
		}

		private int @Nullable [] ghostTints(int @Nullable [] tintLayers) {
			if (tintLayers == null) return null;
			int[] copy = new int[tintLayers.length];
			for (int i = 0; i < tintLayers.length; i++) {
				copy[i] = this.ghostColor(tintLayers[i]);
			}
			return copy;
		}

		private static RenderType ghostItemType(BakedQuad quad) {
			RenderType original = quad.materialInfo().itemRenderType();
			if (original.hasBlending()) return original;
			boolean blockAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.BLOCKS).location().equals(quad.materialInfo().sprite().atlasLocation());
			return blockAtlas ? Sheets.translucentBlockItemSheet() : Sheets.translucentItemSheet();
		}

	}

	private record RedGhost(VertexConsumer delegate, int alpha) implements VertexConsumer {
        @Override public VertexConsumer addVertex(float x, float y, float z) { delegate.addVertex(x, y, z); return this; }

		@Override
		public VertexConsumer setColor(int r, int g, int b, int a) {
			delegate.setColor(r * ((SnnPalette.HIGHLIGHT >> 16) & 255) / 255, g * ((SnnPalette.HIGHLIGHT >> 8) & 255) / 255, b * (SnnPalette.HIGHLIGHT & 255) / 255, a * alpha / 255);
			return this;
		}

		@Override
		public VertexConsumer setColor(int color) {
			return setColor(ARGB.red(color), ARGB.green(color), ARGB.blue(color), ARGB.alpha(color));
		}
        @Override public VertexConsumer setUv(float u, float v) { delegate.setUv(u, v); return this; }
        @Override public VertexConsumer setUv1(int u, int v) { delegate.setUv1(u, v); return this; }
        @Override public VertexConsumer setUv2(int u, int v) { delegate.setUv2(u, v); return this; }

		@Override
		public VertexConsumer setLineWidth(float width) {
			delegate.setLineWidth(width);
			return this;
		}
        @Override public VertexConsumer setNormal(float x, float y, float z) { delegate.setNormal(x, y, z); return this; }
    }
}
