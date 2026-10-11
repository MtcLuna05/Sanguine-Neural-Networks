package com.luna.sanguine_networks.block.entity;

import com.breakinblocks.neovitae.common.fluid.NVFluids;
import com.luna.sanguine_networks.Config;
import com.luna.sanguine_networks.block.menu.SufferingMenu;
import com.luna.sanguine_networks.init.ModBlockEntities;
import com.luna.sanguine_networks.multiblock.SufferingShell;
import com.luna.sanguine_networks.recipe.CatalystRecipe;
import com.luna.sanguine_networks.recipe.ModelRecipe;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.ModelTierRegistry;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.hostilenetworks.multiblock.DataCenterShell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.*;

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

    private final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(TOTAL_SLOTS) {
        @Override
        protected int getCapacity(int slot, ItemResource resource) {
            return slot < MODEL_SLOTS ? 1 : super.getCapacity(slot, resource);
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return slot < MODEL_SLOTS ? resource.getItem() instanceof DataModelItem : findCatalyst(resource.toStack()) != null;
        }

        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {
            if (slot < MODEL_SLOTS) progress[slot] = 0;
            setChanged();
            if (level != null && !level.isClientSide()) sync();
        }
    };
    private final SimpleEnergyHandler energy = new SimpleEnergyHandler(Config.sufferingEnergy, Config.sufferingEnergy, 0) {
        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    };
    private final FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1, Config.sufferingTank) {
        @Override
        public boolean isValid(int index, FluidResource resource) {
            return resource.getFluid() == NVFluids.ESSENTIA_VITAE_SOURCE.get();
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            setChanged();
        }
    };
    private final ResourceHandler<FluidResource> output = new DelegatingResourceHandler<>(tank) {
        @Override
        public boolean isValid(int index, FluidResource resource) {
            return false;
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    };
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            if (index >= 10 && index < 35) return progress[index - 10];
            if (index >= 35 && index < 39) return uses[index - 35];
            if (index >= 39 && index < 43) return maxUses[index - 39];
            if (index >= 43 && index < 68) return status[index - 43];
            return switch (index) {
                case 0 -> !Config.sufferingEnabled ? -1 : layout != null ? 1 : 0;
                case 1 -> energy.getAmountAsInt();
                case 2 -> energy.getCapacityAsInt();
                case 3 -> tank.getAmountAsInt(0);
                case 4 -> tank.getCapacityAsInt(0, FluidResource.of(NVFluids.ESSENTIA_VITAE_SOURCE.get()));
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

    public ItemStacksResourceHandler getInventory() {
        return inventory;
    }

    public EnergyHandler getEnergyStorage() {
        return energy;
    }

    public ResourceHandler<FluidResource> getFluidHandler() {
        return output;
    }
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
        for (var holder : com.luna.sanguine_networks.init.ModRecipes.getRecipes(level, CatalystRecipe.Type.INSTANCE)) {
            var recipe = holder.value();
            if (recipe.getUses() != 0 && recipe.getInput().test(stack)) return recipe;
        }
        return null;
    }
    private void loadCatalysts() {
        for (int i = 0; i < CATALYST_SLOTS; i++) {
            if (uses[i] != 0) continue;
            ItemStack stack = ItemUtil.getStack(inventory, MODEL_SLOTS + i);
            CatalystRecipe recipe = findCatalyst(stack);
            if (recipe == null) continue;
            uses[i] = maxUses[i] = recipe.getUses();
            boosts[i] = recipe.getMultiplier();
            stack.shrink(1);
            inventory.set(MODEL_SLOTS + i, ItemResource.of(stack), stack.getCount());
            setChanged();
        }
    }
    public boolean ownsPort(BlockPos pos) {
        return Config.sufferingEnabled && !isRemoved() && layout != null && ports.contains(pos)
                && level != null && level.isAreaLoaded(layout.centerPos(), 3);
    }
    public void clearPorts() {
        if (level != null) for (BlockPos pos : ports) {
            if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof SufferingIOPortBlockEntity port
                && worldPosition.equals(port.getOwnerPos())) port.setOwner(null);
        }
        ports.clear();
        layout = null;
    }
    public void refreshStructure() {
        if (level == null || level.isClientSide()) return;
        if (!Config.sufferingEnabled) { clearPorts(); sync(); return; }
        var found = SufferingShell.find(worldPosition, getBlockState(), level);
        Set<BlockPos> nextPorts = new HashSet<>();
        if (found != null) found.forEachCell((pos, kind) -> {
            if (level.getBlockEntity(pos) instanceof SufferingIOPortBlockEntity) nextPorts.add(pos.immutable());
        });
        for (BlockPos pos : ports)
            if (!nextPorts.contains(pos) && level.isLoaded(pos)
            && level.getBlockEntity(pos) instanceof SufferingIOPortBlockEntity port
            && worldPosition.equals(port.getOwnerPos())) port.setOwner(null);
        layout = found;
        ports.clear();
        ports.addAll(nextPorts);
        for (BlockPos pos : ports) ((SufferingIOPortBlockEntity) level.getBlockEntity(pos)).setOwner(worldPosition);
        modelRecipes.clear();
        for (var holder : com.luna.sanguine_networks.init.ModRecipes.getRecipes(level, ModelRecipe.Type.INSTANCE)) {
            var recipe = holder.value();
            recipe.getModels().forEach(model -> modelRecipes.putIfAbsent(model, recipe));
        }
        sync();
    }
    public void tick() {
        if (level == null || level.isClientSide()) return;
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
        if (layout != null && !level.isAreaLoaded(layout.centerPos(), 3)) layout = null;
        running = energyPerTick = 0;
        if (layout == null) {
            Arrays.fill(progress, 0);
            Arrays.fill(status, NO_SHELL);
            syncActiveMask();
            setChanged();
            return;
        }
        if (!redstoneState.matches(level.hasNeighborSignal(worldPosition))) {
            for (int i = 0; i < MODEL_SLOTS; i++)
                status[i] = ItemUtil.getStack(inventory, i).isEmpty() ? EMPTY : REDSTONE;
            syncActiveMask();
            return;
        }
        boolean completed = false;
        for (int i = 0; i < MODEL_SLOTS; i++) {
            ItemStack stack = ItemUtil.getStack(inventory, i);
            if (stack.isEmpty()) { progress[i] = 0; status[i] = EMPTY; continue; }
            if (!(stack.getItem() instanceof DataModelItem)) {
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
            if (amount > (tank.getCapacityAsInt(0, FluidResource.of(NVFluids.ESSENTIA_VITAE_SOURCE.get())) - tank.getAmountAsInt(0))) {
                status[i] = TANK_FULL;
                continue;
            }
            if (cost > energy.getAmountAsInt()) {
                status[i] = NO_ENERGY;
                continue;
            }
            energy.set(energy.getAmountAsInt() - cost);
            energyPerTick = (int) Math.min(Integer.MAX_VALUE, (long) energyPerTick + cost);
            status[i] = RUNNING;
            running++;
            if (++progress[i] < Config.sufferingSpeed) continue;
            progress[i] = 0;
            try (var transaction = Transaction.openRoot()) {
                tank.insert(0, FluidResource.of(NVFluids.ESSENTIA_VITAE_SOURCE.get()), amount, transaction);
                transaction.commit();
            }
            for (int c = 0; c < CATALYST_SLOTS; c++) if (uses[c] > 0) uses[c]--;
            int oldData = DataModelItem.getData(stack);
            DataModelItem.setData(stack, (int) Math.min(Integer.MAX_VALUE, (long) oldData + Config.sacrificerData));
            inventory.set(i, ItemResource.of(stack), stack.getCount());
            completed = true;
        }
        setChanged();
        if (completed || ticks % 20 == 0) sync();
        syncActiveMask();
    }

    @Override public Component getDisplayName() { return Component.translatable("block.sanguine_networks.suffering_incorporated"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) { return new SufferingMenu(id, playerInventory, this, data); }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        clearPorts();
        dropInventory();
        super.preRemoveSideEffects(pos, state);
    }

    public void dropInventory() {
        for (int i = 0; i < TOTAL_SLOTS; i++)
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), ItemUtil.getStack(inventory, i));
    }
    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide())
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        inventory.serialize(tag.child("inventory"));
        tag.putInt("energy", energy.getAmountAsInt());
        tag.putInt("redstoneState", redstoneState.ordinal());
        tank.serialize(tag.child("tank"));
        tag.putIntArray("progress", progress.clone());
        tag.putIntArray("uses", uses.clone());
        tag.putIntArray("maxUses", maxUses.clone());
        for (int i = 0; i < CATALYST_SLOTS; i++) tag.putFloat("boost" + i, boosts[i]);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        inventory.deserialize(tag.childOrEmpty("inventory"));
        for (var entry : tag.childOrEmpty("inventory").listOrEmpty("Items",
                com.mojang.serialization.Codec.mapPair(com.mojang.serialization.Codec.INT.fieldOf("Slot"), ItemStack.MAP_CODEC).codec())) {
            int slot = entry.getFirst();
            ItemStack stack = entry.getSecond();
            if (slot >= 0 && slot < inventory.size()) inventory.set(slot, ItemResource.of(stack), stack.getCount());
        }
        redstoneState = dev.shadowsoffire.hostilenetworks.util.RedstoneState.values()[Math.clamp(tag.getIntOr("redstoneState", 0), 0, 2)];
        energy.set(Math.clamp(tag.getIntOr("energy", 0), 0, energy.getCapacityAsInt()));
        tank.deserialize(tag.childOrEmpty("tank"));
        if (!tag.childOrEmpty("tank").keySet().contains("stacks"))
            tag.read("tank", FluidStack.OPTIONAL_CODEC).ifPresent(stack -> tank.set(0, FluidResource.of(stack), stack.getAmount()));
        readArray(tag, "progress", progress);
        readArray(tag, "uses", uses);
        readArray(tag, "maxUses", maxUses);
        readArray(tag, "status", status);
        for (int i = 0; i < CATALYST_SLOTS; i++) boosts[i] = tag.getFloatOr("boost" + i, 0);
        layout = null;
        if (tag.getInt("shellMinX").isPresent()) {
            BlockPos min = new BlockPos(tag.getIntOr("shellMinX", 0), tag.getIntOr("shellMinY", 0), tag.getIntOr("shellMinZ", 0));
            layout = new DataCenterShell.Layout(worldPosition, net.minecraft.core.Direction.from3DDataValue(tag.getIntOr("wallFaceOrd", 0)),
                    min, min.offset(6, 6, 6), min.offset(3, 3, 3));
        }
    }

    private static void readArray(ValueInput tag, String key, int[] target) {
        Arrays.fill(target, 0);
        int[] saved = tag.getIntArray(key).orElseGet(() -> new int[0]);
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
