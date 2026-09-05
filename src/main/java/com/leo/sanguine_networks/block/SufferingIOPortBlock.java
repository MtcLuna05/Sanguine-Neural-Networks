package com.leo.sanguine_networks.block;

import com.leo.sanguine_networks.block.entity.SufferingIOPortBlockEntity;
import com.leo.sanguine_networks.init.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class SufferingIOPortBlock extends BaseEntityBlock {
    public enum Mode implements StringRepresentable {
        ENERGY, MODELS, CATALYSTS, OUTPUT;
        @Override public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }
        public Component label() { return Component.translatable("sanguine_networks.port." + getSerializedName()); }
    }
    public static final EnumProperty<Mode> MODE = EnumProperty.create("mode", Mode.class);
    public static final MapCodec<SufferingIOPortBlock> CODEC = simpleCodec(SufferingIOPortBlock::new);
    public SufferingIOPortBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(MODE, Mode.ENERGY));
    }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(MODE); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SufferingIOPortBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.SUFFERING_PORT_BE.get(), (world, pos, blockState, port) -> port.tick());
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            if (player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof SufferingIOPortBlockEntity port && port.getAltarPos() != null) {
                port.setAltar(null);
                player.displayClientMessage(Component.translatable("sanguine_networks.port.unlinked"), true);
            } else {
                BlockState next = state.cycle(MODE);
                level.setBlockAndUpdate(pos, next);
                level.invalidateCapabilities(pos);
                player.displayClientMessage(next.getValue(MODE).label(), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
