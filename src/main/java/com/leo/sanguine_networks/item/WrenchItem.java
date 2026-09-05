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
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.tile.TileAltar;

import java.util.List;

public class WrenchItem extends Item {
    public WrenchItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();

        if (pContext.getPlayer() == null) return InteractionResult.PASS;
        if(level.isClientSide) {
            return InteractionResult.CONSUME;
        }

        BlockPos pos = pContext.getClickedPos();
        ItemStack stack = pContext.getItemInHand();
        Player player = pContext.getPlayer();

        BlockEntity be = level.getBlockEntity(pos);
        CompoundTag tag = stack.getOrCreateTag();

        if(be instanceof TileAltar) {
            tag.put("altar", NbtUtils.writeBlockPos(pos));
            tag.putString("altarDimension", level.dimension().location().toString());
            player.displayClientMessage(
                Component.translatable("item." + SanguineNeuralNetworks.MODID + ".savedAltar", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.DARK_RED),
                true
            );
            stack.setTag(tag);
            return InteractionResult.CONSUME;
        }

        if(be instanceof VSBlockEntity vs) {
            if(!tag.contains("altar")) {
                player.displayClientMessage(
                    Component.translatable("item." + SanguineNeuralNetworks.MODID + ".emptyAltar").withStyle(ChatFormatting.DARK_RED),
                    true
                );
                return InteractionResult.CONSUME;
            }

            BlockPos altarPos = NbtUtils.readBlockPos(tag.getCompound("altar"));
            if ((tag.contains("altarDimension") && !tag.getString("altarDimension").equals(level.dimension().location().toString()))
                || !level.hasChunkAt(altarPos) || !(level.getBlockEntity(altarPos) instanceof TileAltar)) {
                player.displayClientMessage(Component.translatable("item.sanguine_networks.unavailableAltar").withStyle(ChatFormatting.DARK_RED), true);
                return InteractionResult.CONSUME;
            }
            vs.setBloodAltar(altarPos);
            player.displayClientMessage(
                Component.translatable("item." + SanguineNeuralNetworks.MODID + ".setSacrificer").withStyle(ChatFormatting.DARK_RED),
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
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        pTooltipComponents.add(
            Component.translatable("item." + SanguineNeuralNetworks.MODID + ".wandUse")
        );
    }
}
