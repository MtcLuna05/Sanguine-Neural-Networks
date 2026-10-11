package com.luna.sanguine_networks.item;

import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.block.entity.VSBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


@net.neoforged.fml.common.EventBusSubscriber(modid = SanguineNeuralNetworks.MODID)
public class WrenchItem extends Item {
    public WrenchItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();

	    if (level.isClientSide()) {
            return InteractionResult.CONSUME;
        }

        BlockPos pos = pContext.getClickedPos();
        ItemStack stack = pContext.getItemInHand();
        Player player = pContext.getPlayer();

        BlockEntity be = level.getBlockEntity(pos);
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (player == null) return InteractionResult.PASS;

        if(be instanceof AraVitaeTile) {
	        tag.store("altar", BlockPos.CODEC, pos);
	        tag.putString("dimension", level.dimension().identifier().toString());
	        player.sendOverlayMessage(
			        Component.translatable("item." + SanguineNeuralNetworks.MODID + ".savedAltar", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.DARK_RED)
            );
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
            return InteractionResult.CONSUME;
        }

        if (be instanceof VSBlockEntity || be instanceof com.luna.sanguine_networks.block.entity.SufferingIOPortBlockEntity) {
            if (be instanceof com.luna.sanguine_networks.block.entity.SufferingIOPortBlockEntity port
                    && port.getMode() != com.luna.sanguine_networks.block.SufferingIOPortBlock.Mode.OUTPUT) {
	            player.sendOverlayMessage(Component.translatable("sanguine_networks.port.require_output"));
                return InteractionResult.CONSUME;
            }
            if(!tag.contains("altar")) {
	            player.sendOverlayMessage(
			            Component.translatable("item." + SanguineNeuralNetworks.MODID + ".emptyAltar").withStyle(ChatFormatting.DARK_RED)
                );
                return InteractionResult.CONSUME;
            }

	        BlockPos altarPos = tag.read("altar", BlockPos.CODEC).orElse(null);
	        if (altarPos == null || !tag.getStringOr("dimension", "").equals(level.dimension().identifier().toString())
			        || !level.isLoaded(altarPos) || !(level.getBlockEntity(altarPos) instanceof AraVitaeTile)) {
		        player.sendOverlayMessage(Component.translatable("item." + SanguineNeuralNetworks.MODID + ".unavailableAltar"));
                return InteractionResult.CONSUME;
            }
            if (be instanceof VSBlockEntity vs) vs.setBloodAltar(altarPos);
            else ((com.luna.sanguine_networks.block.entity.SufferingIOPortBlockEntity) be).setAltar(altarPos);
	        player.sendOverlayMessage(
			        Component.translatable("item." + SanguineNeuralNetworks.MODID + (be instanceof VSBlockEntity ? ".setSacrificer" : ".setPort")).withStyle(ChatFormatting.DARK_RED)
            );
            return InteractionResult.CONSUME;
        }

	    player.sendOverlayMessage(
			    Component.translatable("item." + SanguineNeuralNetworks.MODID + ".invalidPos", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.DARK_RED)
	    );

        return InteractionResult.CONSUME;
    }

	@net.neoforged.bus.api.SubscribeEvent
	public static void registerTooltips(net.neoforged.neoforge.event.RegisterTooltipAppendersEvent event) {
		event.registerAppender(net.neoforged.neoforge.common.tooltip.TooltipLocation.POST_CUSTOM,
				(stack, context, display, player, flag, tooltip) -> {
					if (stack.getItem() instanceof WrenchItem)
						tooltip.accept(Component.translatable("item." + SanguineNeuralNetworks.MODID + ".wandUse"));
				});
    }
}
