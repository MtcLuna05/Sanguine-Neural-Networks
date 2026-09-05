package com.leo.sanguine_networks.block.entity;

import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.leo.sanguine_networks.block.SufferingIOPortBlock;
import com.leo.sanguine_networks.block.SufferingIOPortBlock.Mode;
import com.leo.sanguine_networks.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

/** Proxies re-resolve ownership on every operation, including calls through cached capabilities. */
public class SufferingIOPortBlockEntity extends BlockEntity {
    private BlockPos ownerPos;
    private BlockPos altarPos;
    private final IItemHandler models = new Items(Mode.MODELS, 0, SufferingBlockEntity.MODEL_SLOTS);
    private final IItemHandler catalysts = new Items(Mode.CATALYSTS, SufferingBlockEntity.MODEL_SLOTS, SufferingBlockEntity.CATALYST_SLOTS);
    private final IEnergyStorage energy = new IEnergyStorage() {
        private IEnergyStorage delegate() { var owner = owner(Mode.ENERGY); return owner == null ? null : owner.getEnergyStorage(); }
        @Override public int receiveEnergy(int amount, boolean simulate) { var d = delegate(); return d == null ? 0 : d.receiveEnergy(amount, simulate); }
        @Override public int extractEnergy(int amount, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { var d = delegate(); return d == null ? 0 : d.getEnergyStored(); }
        @Override public int getMaxEnergyStored() { var d = delegate(); return d == null ? 0 : d.getMaxEnergyStored(); }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return delegate() != null; }
    };
    private final IFluidHandler fluids = new IFluidHandler() {
        private IFluidHandler delegate() { var owner = owner(Mode.OUTPUT); return owner == null ? null : owner.getFluidHandler(); }
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) { var d = delegate(); return d == null ? FluidStack.EMPTY : d.getFluidInTank(tank); }
        @Override public int getTankCapacity(int tank) { var d = delegate(); return d == null ? 0 : d.getTankCapacity(tank); }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return false; }
        @Override public int fill(FluidStack stack, FluidAction action) { return 0; }
        @Override public FluidStack drain(FluidStack stack, FluidAction action) { var d = delegate(); return d == null ? FluidStack.EMPTY : d.drain(stack, action); }
        @Override public FluidStack drain(int amount, FluidAction action) { var d = delegate(); return d == null ? FluidStack.EMPTY : d.drain(amount, action); }
    };

    public SufferingIOPortBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.SUFFERING_PORT_BE.get(), pos, state); }
    public Mode getMode() { return getBlockState().getValue(SufferingIOPortBlock.MODE); }
    public BlockPos getOwnerPos() { return ownerPos; }
    public BlockPos getAltarPos() { return altarPos; }
    public void setOwner(BlockPos pos) {
        if (java.util.Objects.equals(ownerPos, pos)) return;
        ownerPos = pos == null ? null : pos.immutable();
        invalidateCapabilities();
        sync();
    }
    public void setAltar(BlockPos pos) { altarPos = pos == null ? null : pos.immutable(); sync(); }
    public SufferingBlockEntity resolveOwner() {
        if (isRemoved() || level == null || ownerPos == null || !level.hasChunkAt(ownerPos)) return null;
        if (!(level.getBlockEntity(ownerPos) instanceof SufferingBlockEntity owner) || !owner.ownsPort(worldPosition)) return null;
        return owner;
    }
    private SufferingBlockEntity owner(Mode mode) { return getMode() == mode ? resolveOwner() : null; }
    public IEnergyStorage getEnergyHandler() { return getMode() == Mode.ENERGY ? energy : null; }
    public IItemHandler getItemHandler() { return switch (getMode()) { case MODELS -> models; case CATALYSTS -> catalysts; default -> null; }; }
    public IFluidHandler getFluidHandler() { return getMode() == Mode.OUTPUT ? fluids : null; }

    public void tick() {
        if (getMode() != Mode.OUTPUT || level == null || level.isClientSide || altarPos == null || !level.hasChunkAt(altarPos)) return;
        var owner = resolveOwner();
        if (owner == null || !(level.getBlockEntity(altarPos) instanceof AraVitaeTile altar)) return;
        double bonus = 1.0f + altar.getSacrificeBonus();
        int space = altar.getCapacity() - altar.getCurrentBlood();
        if (!Double.isFinite(bonus) || bonus <= 0 || space <= 0) return;
        int available = owner.getFluidHandler().getFluidInTank(0).getAmount();
        int transfer = (int) Math.min(available, Math.floor(space / bonus));
        if (transfer <= 0) return;
        // Same sacrifice route as Virtual Sacrificer. Only the receiving altar's runes apply.
        altar.addSacrificeEV(transfer, true);
        owner.getFluidHandler().drain(transfer, IFluidHandler.FluidAction.EXECUTE);
    }
    private class Items implements IItemHandler {
        private final Mode mode;
        private final int start, count;
        private Items(Mode mode, int start, int count) { this.mode = mode; this.start = start; this.count = count; }
        private IItemHandler delegate(int slot) { if (slot < 0 || slot >= count) throw new IndexOutOfBoundsException(slot); var o = owner(mode); return o == null ? null : o.getInventory(); }
        @Override public int getSlots() { return count; }
        @Override public ItemStack getStackInSlot(int slot) { var d = delegate(slot); return d == null ? ItemStack.EMPTY : d.getStackInSlot(start + slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { var d = delegate(slot); return d == null ? stack : d.insertItem(start + slot, stack, simulate); }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { var d = delegate(slot); return d == null || mode == Mode.MODELS ? ItemStack.EMPTY : d.extractItem(start + slot, amount, simulate); }
        @Override public int getSlotLimit(int slot) { var d = delegate(slot); return d == null ? 0 : d.getSlotLimit(start + slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { var d = delegate(slot); return d != null && d.isItemValid(start + slot, stack); }
    }
    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (ownerPos != null) tag.putLong("owner", ownerPos.asLong());
        if (altarPos != null) tag.putLong("altar", altarPos.asLong());
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ownerPos = tag.contains("owner") ? BlockPos.of(tag.getLong("owner")) : null;
        altarPos = tag.contains("altar") ? BlockPos.of(tag.getLong("altar")) : null;
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
