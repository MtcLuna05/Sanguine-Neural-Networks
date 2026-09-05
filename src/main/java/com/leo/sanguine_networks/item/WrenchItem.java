package com.leo.sanguine_networks.item;

import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.block.entity.VSBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;

import java.util.List;

public class WrenchItem extends Item {
    public WrenchItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();

        if(level.isClientSide) {
            return InteractionResult.CONSUME;
        }

        BlockPos pos = pContext.getClickedPos();
        ItemStack stack = pContext.getItemInHand();
        Player player = pContext.getPlayer();

        BlockEntity be = level.getBlockEntity(pos);
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (player == null) return InteractionResult.PASS;

        if(be instanceof AraVitaeTile) {
            tag.put("altar", NbtUtils.writeBlockPos(pos));
            tag.putString("dimension", level.dimension().location().toString());
            player.displayClientMessage(
                Component.translatable("item." + SanguineNeuralNetworks.MODID + ".savedAltar", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.DARK_RED),
                true
            );
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
            return InteractionResult.CONSUME;
        }

        if (be instanceof VSBlockEntity || be instanceof com.leo.sanguine_networks.block.entity.SufferingIOPortBlockEntity) {
            if (be instanceof com.leo.sanguine_networks.block.entity.SufferingIOPortBlockEntity port
                && port.getMode() != com.leo.sanguine_networks.block.SufferingIOPortBlock.Mode.OUTPUT) {
                player.displayClientMessage(Component.translatable("sanguine_networks.port.require_output"), true);
                return InteractionResult.CONSUME;
            }
            if(!tag.contains("altar")) {
                player.displayClientMessage(
                    Component.translatable("item." + SanguineNeuralNetworks.MODID + ".emptyAltar").withStyle(ChatFormatting.DARK_RED),
                    true
                );
                return InteractionResult.CONSUME;
            }

            BlockPos altarPos = NbtUtils.readBlockPos(tag, "altar").orElse(null);
            if (altarPos == null || !tag.getString("dimension").equals(level.dimension().location().toString())
                || !level.hasChunkAt(altarPos) || !(level.getBlockEntity(altarPos) instanceof AraVitaeTile)) {
                player.displayClientMessage(Component.translatable("item." + SanguineNeuralNetworks.MODID + ".unavailableAltar"), true);
                return InteractionResult.CONSUME;
            }
            if (be instanceof VSBlockEntity vs) vs.setBloodAltar(altarPos);
            else ((com.leo.sanguine_networks.block.entity.SufferingIOPortBlockEntity) be).setAltar(altarPos);
            player.displayClientMessage(
                Component.translatable("item." + SanguineNeuralNetworks.MODID + (be instanceof VSBlockEntity ? ".setSacrificer" : ".setPort")).withStyle(ChatFormatting.DARK_RED),
                true
            );
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(
            Component.translatable("item." + SanguineNeuralNetworks.MODID + ".invalidPos", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.DARK_RED),
            true
        );

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        pTooltipComponents.add(
            Component.translatable("item." + SanguineNeuralNetworks.MODID + ".wandUse")
        );
    }
}
