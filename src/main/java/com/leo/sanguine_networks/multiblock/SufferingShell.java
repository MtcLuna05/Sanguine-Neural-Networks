package com.leo.sanguine_networks.multiblock;

import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.leo.sanguine_networks.block.SufferingBlock;
import dev.shadowsoffire.hostilenetworks.multiblock.DataCenterShell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Uses HNN's shell geometry, but never reads or applies altar rune modifiers. */
public final class SufferingShell {
    public static final TagKey<Block> RUNES = TagKey.create(Registries.BLOCK, ResourceLocation.parse("neovitae:altar/runes"));
    public static final int SIZE = DataCenterShell.SHELL_SIZE;

    private SufferingShell() {}

    public static DataCenterShell.Layout find(BlockPos controller, BlockState state, Level level) {
        Direction inward = state.getValue(SufferingBlock.FACING).getOpposite();
        for (int offset = 0; offset < SIZE; offset++) {
            int x = inward.getAxis() == Direction.Axis.X
                ? controller.getX() - (inward == Direction.WEST ? SIZE - 1 : 0) : controller.getX() - offset;
            int z = inward.getAxis() == Direction.Axis.Z
                ? controller.getZ() - (inward == Direction.NORTH ? SIZE - 1 : 0) : controller.getZ() - offset;
            BlockPos min = new BlockPos(x, controller.getY() - 1, z);
            var layout = new DataCenterShell.Layout(controller, inward, min, min.offset(6, 6, 6), min.offset(3, 3, 3));
            if (validate(layout, level)) return layout;
        }
        return null;
    }

    private static boolean validPort(BlockPos pos, BlockPos controller, Level level) {
        if (!(level.getBlockEntity(pos) instanceof com.leo.sanguine_networks.block.entity.SufferingIOPortBlockEntity port)) return false;
        var owner = port.resolveOwner();
        return owner == null || owner.getBlockPos().equals(controller);
    }

    public static boolean validate(DataCenterShell.Layout layout, Level level) {
        return !layout.forEachCellUntil((pos, kind) -> {
            if (!level.hasChunkAt(pos)) return true;
            BlockState state = level.getBlockState(pos);
            return switch (kind) {
                case FLOOR -> !state.is(NVBlocks.RUNE_BLANK.block().get()) && !state.is(RUNES);
                case WALL, CEILING -> !state.is(NVBlocks.BLOOD_STAINED_GLASS.block().get()) && !validPort(pos, layout.controllerPos(), level);
                case INTERIOR -> !state.isAir();
                case CONTROLLER -> !(state.getBlock() instanceof SufferingBlock);
            };
        });
    }
}
