package com.leo.sanguine_networks.block.menu;

import com.leo.sanguine_networks.block.entity.SufferingBlockEntity;
import com.leo.sanguine_networks.init.ModBlocks;
import com.leo.sanguine_networks.init.ModMenuTypes;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SufferingMenu extends AbstractContainerMenu {
    public static final int PANEL_HEIGHT = 128, PLAYER_PANEL_Y = PANEL_HEIGHT + 4;
    public static final int INVENTORY_Y = PLAYER_PANEL_Y + 8, HOTBAR_Y = INVENTORY_Y + 58;
    public static final int WIDTH = 230, HEIGHT = 223;
    private final SufferingBlockEntity entity;
    private final ContainerData data;

    public SufferingMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, (SufferingBlockEntity) inventory.player.level().getBlockEntity(buffer.readBlockPos()),
            new SimpleContainerData(SufferingBlockEntity.DATA_COUNT));
    }
    public SufferingMenu(int id, Inventory inventory, SufferingBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.SUFFERING_MENU.get(), id);
        this.entity = entity;
        this.data = data;
        for (int slot = 0; slot < 25; slot++)
            addSlot(new SlotItemHandler(entity.getInventory(), slot, 9 + slot % 5 * 18, 21 + slot / 5 * 20));
        for (int slot = 0; slot < 4; slot++)
            addSlot(new SlotItemHandler(entity.getInventory(), 25 + slot, 132 + slot * 18, 20));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, 9 + row * 9 + col, 35 + col * 18, INVENTORY_Y + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 35 + col * 18, HOTBAR_Y));
        addDataSlots(data);
    }
    public dev.shadowsoffire.hostilenetworks.util.RedstoneState getRedstoneState() {
        return dev.shadowsoffire.hostilenetworks.util.RedstoneState.values()[Math.clamp(data.get(68), 0, 2)];
    }
    @Override public boolean clickMenuButton(Player player, int id) {
        if (!stillValid(player) || id < 2000 || id > 2002) return false;
        entity.setRedstoneState(dev.shadowsoffire.hostilenetworks.util.RedstoneState.values()[id - 2000]);
        return true;
    }
    public ContainerData getData() { return data; }
    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos()), player, ModBlocks.SUFFERING_INCORPORATED.get());
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        boolean moved = index < 29 ? moveItemStackTo(stack, 29, 65, true)
            : stack.getItem() instanceof DataModelItem ? moveItemStackTo(stack, 0, 25, false)
            : moveItemStackTo(stack, 25, 29, false);
        if (!moved) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }
}
