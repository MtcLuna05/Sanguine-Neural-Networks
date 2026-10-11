package com.luna.sanguine_networks.block.entity;

import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.luna.sanguine_networks.block.SufferingIOPortBlock;
import com.luna.sanguine_networks.block.SufferingIOPortBlock.Mode;
import com.luna.sanguine_networks.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;


/** Proxies re-resolve ownership on every operation, including calls through cached capabilities. */
public class SufferingIOPortBlockEntity extends BlockEntity {
    private BlockPos ownerPos;
    private BlockPos altarPos;
	private final ResourceHandler<ItemResource> models = new Items(Mode.MODELS, 0, SufferingBlockEntity.MODEL_SLOTS);
	private final ResourceHandler<ItemResource> catalysts = new Items(Mode.CATALYSTS, SufferingBlockEntity.MODEL_SLOTS, SufferingBlockEntity.CATALYST_SLOTS);
	private final EnergyHandler energy = new EnergyHandler() {
		private EnergyHandler delegate() {
			var owner = owner(Mode.ENERGY);
			return owner == null ? null : owner.getEnergyStorage();
		}

		@Override
		public int insert(int amount, TransactionContext transaction) {
			var d = delegate();
			return d == null ? 0 : d.insert(amount, transaction);
		}

		@Override
		public int extract(int amount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public long getAmountAsLong() {
			var d = delegate();
			return d == null ? 0 : d.getAmountAsLong();
		}

		@Override
		public long getCapacityAsLong() {
			var d = delegate();
			return d == null ? 0 : d.getCapacityAsLong();
		}
    };
	private final ResourceHandler<FluidResource> fluids = new ResourceHandler<>() {
		private ResourceHandler<FluidResource> delegate() {
			var owner = owner(Mode.OUTPUT);
			return owner == null ? null : owner.getFluidHandler();
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public FluidResource getResource(int index) {
			java.util.Objects.checkIndex(index, size());
			var d = delegate();
			return d == null ? FluidResource.EMPTY : d.getResource(index);
		}

		@Override
		public long getAmountAsLong(int index) {
			java.util.Objects.checkIndex(index, size());
			var d = delegate();
			return d == null ? 0 : d.getAmountAsLong(index);
		}

		@Override
		public long getCapacityAsLong(int index, FluidResource resource) {
			java.util.Objects.checkIndex(index, size());
			var d = delegate();
			return d == null ? 0 : d.getCapacityAsLong(index, resource);
		}

		@Override
		public boolean isValid(int index, FluidResource resource) {
			java.util.Objects.checkIndex(index, size());
			return false;
		}

		@Override
		public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
			java.util.Objects.checkIndex(index, size());
			return 0;
		}

		@Override
		public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
			java.util.Objects.checkIndex(index, size());
			var d = delegate();
			return d == null ? 0 : d.extract(index, resource, amount, transaction);
		}
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
	    if (isRemoved() || level == null || ownerPos == null || !level.isLoaded(ownerPos)) return null;
        if (!(level.getBlockEntity(ownerPos) instanceof SufferingBlockEntity owner) || !owner.ownsPort(worldPosition)) return null;
        return owner;
    }
    private SufferingBlockEntity owner(Mode mode) { return getMode() == mode ? resolveOwner() : null; }

	public EnergyHandler getEnergyHandler() {
		return getMode() == Mode.ENERGY ? energy : null;
	}

	public ResourceHandler<ItemResource> getItemHandler() {
		return switch (getMode()) {
			case MODELS -> models;
			case CATALYSTS -> catalysts;
			default -> null;
		};
	}

	public ResourceHandler<FluidResource> getFluidHandler() {
		return getMode() == Mode.OUTPUT ? fluids : null;
	}

    public void tick() {
	    if (getMode() != Mode.OUTPUT || level == null || level.isClientSide() || altarPos == null || !level.isLoaded(altarPos))
		    return;
        var owner = resolveOwner();
        if (owner == null || !(level.getBlockEntity(altarPos) instanceof AraVitaeTile altar)) return;
        double bonus = 1.0f + altar.getSacrificeBonus();
        int space = altar.getCapacity() - altar.getCurrentBlood();
        if (!Double.isFinite(bonus) || bonus <= 0 || space <= 0) return;
	    int available = owner.getFluidHandler().getAmountAsInt(0);
        int transfer = (int) Math.min(available, Math.floor(space / bonus));
        if (transfer <= 0) return;
        // Same sacrifice route as Virtual Sacrificer. Only the receiving altar's runes apply.
	    try (var transaction = Transaction.openRoot()) {
		    var handler = owner.getFluidHandler();
		    int extracted = handler.extract(0, handler.getResource(0), transfer, transaction);
		    if (extracted != transfer) return;
		    altar.addSacrificeEV(extracted, true);
		    transaction.commit();
	    }
    }

	private class Items implements ResourceHandler<ItemResource> {
        private final Mode mode;
        private final int start, count;
        private Items(Mode mode, int start, int count) { this.mode = mode; this.start = start; this.count = count; }

		private ResourceHandler<ItemResource> delegate(int slot) {
			java.util.Objects.checkIndex(slot, count);
			var o = owner(mode);
			return o == null ? null : o.getInventory();
		}

		@Override
		public int size() {
			return count;
		}

		@Override
		public ItemResource getResource(int slot) {
			var d = delegate(slot);
			return d == null ? ItemResource.EMPTY : d.getResource(start + slot);
		}

		@Override
		public long getAmountAsLong(int slot) {
			var d = delegate(slot);
			return d == null ? 0 : d.getAmountAsLong(start + slot);
		}

		@Override
		public long getCapacityAsLong(int slot, ItemResource resource) {
			var d = delegate(slot);
			return d == null ? 0 : d.getCapacityAsLong(start + slot, resource);
		}

		@Override
		public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
			var d = delegate(slot);
			return d == null ? 0 : d.insert(start + slot, resource, amount, transaction);
		}

		@Override
		public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
			var d = delegate(slot);
			return d == null || mode == Mode.MODELS ? 0 : d.extract(start + slot, resource, amount, transaction);
		}

		@Override
		public boolean isValid(int slot, ItemResource resource) {
			var d = delegate(slot);
			return d != null && d.isValid(start + slot, resource);
		}
    }
    private void sync() {
        setChanged();
	    if (level != null && !level.isClientSide())
		    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

	@Override
	protected void saveAdditional(ValueOutput tag) {
		super.saveAdditional(tag);
        if (ownerPos != null) tag.putLong("owner", ownerPos.asLong());
        if (altarPos != null) tag.putLong("altar", altarPos.asLong());
    }

	@Override
	protected void loadAdditional(ValueInput tag) {
		super.loadAdditional(tag);
		ownerPos = tag.getLong("owner").map(BlockPos::of).orElse(null);
		altarPos = tag.getLong("altar").map(BlockPos::of).orElse(null);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
