package com.leo.sanguine_networks.block.entity;

import com.breakinblocks.neovitae.common.fluid.NVFluids;
import com.leo.sanguine_networks.Config;
import com.leo.sanguine_networks.block.menu.SufferingMenu;
import com.leo.sanguine_networks.init.ModBlockEntities;
import com.leo.sanguine_networks.multiblock.SufferingShell;
import com.leo.sanguine_networks.recipe.CatalystRecipe;
import com.leo.sanguine_networks.recipe.ModelRecipe;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.shadowsoffire.hostilenetworks.data.ModelTierRegistry;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.hostilenetworks.multiblock.DataCenterShell;
import dev.shadowsoffire.placebo.cap.ModifiableEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

public class SufferingBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MODEL_SLOTS = 25;
    public static final int CATALYST_SLOTS = 4;
    public static final int TOTAL_SLOTS = MODEL_SLOTS + CATALYST_SLOTS;
    public static final int DATA_COUNT = 69;
    public static final int EMPTY = 0, RUNNING = 1, NOT_SELF_AWARE = 2, NO_RECIPE = 3, NO_SHELL = 4, NO_ENERGY = 5, TANK_FULL = 6, REDSTONE = 7, DISABLED = 8;

    private final int[] progress = new int[MODEL_SLOTS];
    private final int[] status = new int[MODEL_SLOTS];
    private final int[] uses = new int[CATALYST_SLOTS];
    private final int[] maxUses = new int[CATALYST_SLOTS];
    private final float[] boosts = new float[CATALYST_SLOTS];
    private final Map<DataModel, ModelRecipe> modelRecipes = new IdentityHashMap<>();
    private final Set<BlockPos> ports = new HashSet<>();
    private DataCenterShell.Layout layout;
    private int ticks, running, energyPerTick, syncedActiveMask;
    private dev.shadowsoffire.hostilenetworks.util.RedstoneState redstoneState = dev.shadowsoffire.hostilenetworks.util.RedstoneState.IGNORED;
    public void setRedstoneState(dev.shadowsoffire.hostilenetworks.util.RedstoneState state) { redstoneState = state; sync(); }
    public int getActiveSlotsMask() {
        int mask = 0;
        for (int i = 0; i < MODEL_SLOTS; i++) if (status[i] == RUNNING && progress[i] > 0) mask |= 1 << i;
        return mask;
    }
    private void syncActiveMask() {
        int mask = getActiveSlotsMask();
        if (mask != syncedActiveMask) { syncedActiveMask = mask; sync(); }
    }

    private final ItemStackHandler inventory = new ItemStackHandler(TOTAL_SLOTS) {
        @Override public int getSlotLimit(int slot) { return slot < MODEL_SLOTS ? 1 : 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot < MODEL_SLOTS ? stack.getItem() instanceof DataModelItem : findCatalyst(stack) != null;
        }
        @Override protected void onContentsChanged(int slot) {
            if (slot < MODEL_SLOTS) progress[slot] = 0;
            setChanged();
            if (level != null && !level.isClientSide) sync();
        }
    };
    private final ModifiableEnergyStorage energy = new ModifiableEnergyStorage(Config.sufferingEnergy, Config.sufferingEnergy, 0, 0) {
        @Override public int receiveEnergy(int amount, boolean simulate) {
            int received = super.receiveEnergy(amount, simulate);
            if (!simulate && received > 0) setChanged();
            return received;
        }
    };
    private final FluidTank tank = new FluidTank(Config.sufferingTank, fluid -> fluid.is(NVFluids.ESSENTIA_VITAE_SOURCE.get())) {
        @Override protected void onContentsChanged() { setChanged(); }
    };
    private final IFluidHandler output = new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int index) { return tank.getFluidInTank(index).copy(); }
        @Override public int getTankCapacity(int index) { return tank.getTankCapacity(index); }
        @Override public boolean isFluidValid(int index, FluidStack fluid) { return false; }
        @Override public int fill(FluidStack fluid, FluidAction action) { return 0; }
        @Override public FluidStack drain(FluidStack fluid, FluidAction action) { return tank.drain(fluid, action); }
        @Override public FluidStack drain(int amount, FluidAction action) { return tank.drain(amount, action); }
    };
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            if (index >= 10 && index < 35) return progress[index - 10];
            if (index >= 35 && index < 39) return uses[index - 35];
            if (index >= 39 && index < 43) return maxUses[index - 39];
            if (index >= 43 && index < 68) return status[index - 43];
            return switch (index) {
                case 0 -> !Config.sufferingEnabled ? -1 : layout != null ? 1 : 0;
                case 1 -> energy.getEnergyStored();
                case 2 -> energy.getMaxEnergyStored();
                case 3 -> tank.getFluidAmount();
                case 4 -> tank.getCapacity();
                case 5 -> running;
                case 6 -> energyPerTick;
                case 7 -> (int) (getCatalystMultiplier() * 1000);
                case 8 -> activeCatalysts();
                case 9 -> Config.sufferingSpeed;
                case 68 -> redstoneState.ordinal();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public SufferingBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.SUFFERING_BE.get(), pos, state); }
    public ItemStackHandler getInventory() { return inventory; }
    public IEnergyStorage getEnergyStorage() { return energy; }
    public IFluidHandler getFluidHandler() { return output; }
    public DataCenterShell.Layout getLayout() { return layout; }
    public ContainerData getData() { return data; }
    public int getProgress(int slot) { return progress[slot]; }
    public int getStatus(int slot) { return status[slot]; }
    public int getCatalystUses(int slot) { return uses[slot]; }

    public static double energyMultiplier(int catalysts) { return 1 + 0.5 * Math.max(0, catalysts - 1); }
    public static int energyCost(int base, int catalysts) { return (int) Math.min(Integer.MAX_VALUE, Math.ceil(base * energyMultiplier(catalysts))); }
    private int activeCatalysts() {
        int count = 0;
        for (int value : uses) if (value > 0 || value == -1) count++;
        return count;
    }
    public float getCatalystMultiplier() {
        float sum = 0;
        int active = 0;
        for (int i = 0; i < CATALYST_SLOTS; i++) if (uses[i] > 0 || uses[i] == -1) { sum += boosts[i]; active++; }
        return active == 0 ? 1 : sum;
    }

    private CatalystRecipe findCatalyst(ItemStack stack) {
        if (stack.isEmpty() || level == null) return null;
        // Also used by client-side slots and automation before the first server tick.
        for (var holder : level.getRecipeManager().getAllRecipesFor(CatalystRecipe.Type.INSTANCE)) {
            var recipe = holder.value();
            if (recipe.getUses() != 0 && recipe.getInput().test(stack)) return recipe;
        }
        return null;
    }
    private void loadCatalysts() {
        for (int i = 0; i < CATALYST_SLOTS; i++) {
            if (uses[i] != 0) continue;
            ItemStack stack = inventory.getStackInSlot(MODEL_SLOTS + i);
            CatalystRecipe recipe = findCatalyst(stack);
            if (recipe == null) continue;
            uses[i] = maxUses[i] = recipe.getUses();
            boosts[i] = recipe.getMultiplier();
            stack.shrink(1);
            setChanged();
        }
    }
    public boolean ownsPort(BlockPos pos) {
        return Config.sufferingEnabled && !isRemoved() && layout != null && ports.contains(pos)
            && level != null && level.hasChunksAt(layout.shellMin(), layout.shellMax());
    }
    public void clearPorts() {
        if (level != null) for (BlockPos pos : ports) {
            if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof SufferingIOPortBlockEntity port
                && worldPosition.equals(port.getOwnerPos())) port.setOwner(null);
        }
        ports.clear();
        layout = null;
    }
    public void refreshStructure() {
        if (level == null || level.isClientSide) return;
        if (!Config.sufferingEnabled) { clearPorts(); sync(); return; }
        var found = SufferingShell.find(worldPosition, getBlockState(), level);
        Set<BlockPos> nextPorts = new HashSet<>();
        if (found != null) found.forEachCell((pos, kind) -> {
            if (level.getBlockEntity(pos) instanceof SufferingIOPortBlockEntity) nextPorts.add(pos.immutable());
        });
        for (BlockPos pos : ports) if (!nextPorts.contains(pos) && level.hasChunkAt(pos)
            && level.getBlockEntity(pos) instanceof SufferingIOPortBlockEntity port
            && worldPosition.equals(port.getOwnerPos())) port.setOwner(null);
        layout = found;
        ports.clear();
        ports.addAll(nextPorts);
        for (BlockPos pos : ports) ((SufferingIOPortBlockEntity) level.getBlockEntity(pos)).setOwner(worldPosition);
        modelRecipes.clear();
        for (var holder : level.getRecipeManager().getAllRecipesFor(ModelRecipe.Type.INSTANCE)) {
            var recipe = holder.value();
            BuiltInRegistries.ENTITY_TYPE.getOptional(recipe.getEntity()).ifPresent(entity ->
                DataModelRegistry.INSTANCE.getForEntity(entity).forEach(model -> modelRecipes.putIfAbsent(model, recipe)));
        }
        sync();
    }
    public void tick() {
        if (level == null || level.isClientSide) return;
        if (!Config.sufferingEnabled) {
            boolean wasFormed = layout != null;
            clearPorts();
            running = energyPerTick = 0;
            Arrays.fill(status, DISABLED);
            syncActiveMask();
            if (wasFormed) sync();
            ticks = 0; // Revalidate immediately after re-enabling.
            return;
        }
        if (ticks++ % 20 == 0) refreshStructure();
        if (layout != null && !level.hasChunksAt(layout.shellMin(), layout.shellMax())) layout = null;
        running = energyPerTick = 0;
        if (layout == null) {
            Arrays.fill(progress, 0);
            Arrays.fill(status, NO_SHELL);
            syncActiveMask();
            setChanged();
            return;
        }
        if (!redstoneState.matches(level.hasNeighborSignal(worldPosition))) {
            for (int i = 0; i < MODEL_SLOTS; i++) status[i] = inventory.getStackInSlot(i).isEmpty() ? EMPTY : REDSTONE;
            syncActiveMask();
            return;
        }
        boolean completed = false;
        for (int i = 0; i < MODEL_SLOTS; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) { progress[i] = 0; status[i] = EMPTY; continue; }
            // Also reject forced/NBT-inserted combined models, not only GUI and port insertion.
            if (!(stack.getItem() instanceof DataModelItem)
                || com.leo.sanguine_networks.compat.ExtraHnnCompat.isCombined(stack)) {
                progress[i] = 0; status[i] = NO_RECIPE; continue;
            }
            var model = DataModelItem.getStoredModel(stack);
            if (!model.isBound()) { progress[i] = 0; status[i] = NO_RECIPE; continue; }
            var tier = ModelTierRegistry.getByData(model.get(), DataModelItem.getData(stack));
            if (!tier.canSim() || tier.accuracy() < 1.0f) { progress[i] = 0; status[i] = NOT_SELF_AWARE; continue; }
            ModelRecipe recipe = modelRecipes.get(model.get());
            if (recipe == null) { progress[i] = 0; status[i] = NO_RECIPE; continue; }
            loadCatalysts();
            int amount = (int) Math.min(Integer.MAX_VALUE, (double) recipe.getBlood(tier) * getCatalystMultiplier());
            int cost = energyCost(recipe.getEnergy(), activeCatalysts());
            if (amount > tank.getSpace()) { status[i] = TANK_FULL; continue; }
            if (cost > energy.getEnergyStored()) { status[i] = NO_ENERGY; continue; }
            energy.setEnergy(energy.getEnergyStored() - cost);
            energyPerTick = (int) Math.min(Integer.MAX_VALUE, (long) energyPerTick + cost);
            status[i] = RUNNING;
            running++;
            if (++progress[i] < Config.sufferingSpeed) continue;
            progress[i] = 0;
            tank.fill(new FluidStack(NVFluids.ESSENTIA_VITAE_SOURCE.get(), amount), IFluidHandler.FluidAction.EXECUTE);
            for (int c = 0; c < CATALYST_SLOTS; c++) if (uses[c] > 0) uses[c]--;
            int oldData = DataModelItem.getData(stack);
            DataModelItem.setData(stack, (int) Math.min(Integer.MAX_VALUE, (long) oldData + Config.sacrificerData));
            completed = true;
        }
        setChanged();
        if (completed || ticks % 20 == 0) sync();
        syncActiveMask();
    }

    @Override public Component getDisplayName() { return Component.translatable("block.sanguine_networks.suffering_incorporated"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) { return new SufferingMenu(id, playerInventory, this, data); }
    public void dropInventory() {
        for (int i = 0; i < TOTAL_SLOTS; i++) Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), inventory.getStackInSlot(i));
    }
    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("energy", energy.getEnergyStored());
        tag.putInt("redstoneState", redstoneState.ordinal());
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putIntArray("progress", progress.clone());
        tag.putIntArray("uses", uses.clone());
        tag.putIntArray("maxUses", maxUses.clone());
        for (int i = 0; i < CATALYST_SLOTS; i++) tag.putFloat("boost" + i, boosts[i]);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        redstoneState = dev.shadowsoffire.hostilenetworks.util.RedstoneState.values()[Math.clamp(tag.getInt("redstoneState"), 0, 2)];
        energy.setEnergy(Math.clamp(tag.getInt("energy"), 0, energy.getMaxEnergyStored()));
        tank.readFromNBT(registries, tag.getCompound("tank"));
        readArray(tag, "progress", progress);
        readArray(tag, "uses", uses);
        readArray(tag, "maxUses", maxUses);
        readArray(tag, "status", status);
        for (int i = 0; i < CATALYST_SLOTS; i++) boosts[i] = tag.getFloat("boost" + i);
        layout = tag.contains("shellMinX") ? DataCenterShell.Layout.readLayout(tag, worldPosition) : null;
    }
    private static void readArray(CompoundTag tag, String key, int[] target) {
        Arrays.fill(target, 0);
        int[] saved = tag.getIntArray(key);
        System.arraycopy(saved, 0, target, 0, Math.min(saved.length, target.length));
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = saveWithoutMetadata(registries);
        tag.putIntArray("status", status.clone());
        if (layout != null) DataCenterShell.Layout.writeLayout(tag, layout);
        return tag;
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
