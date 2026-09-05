package com.leo.sanguine_networks.block.entity;

import com.leo.sanguine_networks.Config;
import com.leo.sanguine_networks.compat.ExtraHnnCompat;
import com.leo.sanguine_networks.SanguineNeuralNetworks;
import com.leo.sanguine_networks.init.ModBlockEntities;
import com.leo.sanguine_networks.block.menu.VSacrificerMenu;
import com.leo.sanguine_networks.recipe.CatalystRecipe;
import com.leo.sanguine_networks.recipe.ModelRecipe;
import com.leo.sanguine_networks.util.Pair;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.shadowsoffire.hostilenetworks.data.ModelTier;
import dev.shadowsoffire.hostilenetworks.data.ModelTierRegistry;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.placebo.cap.ModifiableEnergyStorage;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;

import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;

import java.util.List;

public class VSBlockEntity extends BlockEntity implements MenuProvider {

    private final ItemStackHandler itemHandler = new ItemStackHandler(2){
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == 0) VSBlockEntity.this.progress = 0;
            VSBlockEntity.this.sync();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch(slot){
                case 0 -> acceptsModel(stack);
                default -> true;
            };

        }
    };
    private final ModifiableEnergyStorage energyStorage;

    private int catalystUses = 0, maxCatalystUses = 0;
    private int progress = 0, maxProgress = 0;
    private int toProduce = 0;
    private float catalystMult = 0f, altarMultiplier;
    private boolean missingModel = false;

    private BlockPos altarPos;
    private AraVitaeTile bloodAltar;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int pIndex) {
            return switch (pIndex){
                case 0 -> VSBlockEntity.this.energyStorage.getEnergyStored();
                case 1 -> VSBlockEntity.this.energyStorage.getMaxEnergyStored();
                case 2 -> catalystUses;
                case 3 -> maxCatalystUses;
                case 4 -> progress;
                case 5 -> maxProgress;
                case 6 -> toProduce;
                case 7 -> (int) (catalystMult * 1000);
                case 8 -> bloodAltar != null ? 1: 0;
                case 9 -> missingModel ? 1: 0;
                case 10 -> (int) (altarMultiplier * 1000);
                default -> -1;
            };
        }

        @Override
        public void set(int pIndex, int pValue) {}

        @Override
        public int getCount() {
            return 11;
        }
    };

    public VSBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.V_SACRIFICER_BE.get(), pPos, pBlockState);
        energyStorage  = new ModifiableEnergyStorage(Config.sacrificerEnergy, Config.sacrificerEnergy, 0, 0);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(SanguineNeuralNetworks.MODID + ".container.virtual_sacrificer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new VSacrificerMenu(i, inventory, this, containerData);
    }

    public static boolean acceptsModel(ItemStack stack) {
        return ExtraHnnCompat.isCombined(stack) ? Config.extraHnnModelsEnabled : stack.getItem() instanceof DataModelItem;
    }

    public IEnergyStorage getEnergyStorage() { return energyStorage; }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);
        pTag.put("inventory", itemHandler.serializeNBT(registries));

        pTag.putInt("energy", energyStorage.getEnergyStored());

        pTag.putInt("catalystUses", catalystUses);
        pTag.putInt("maxCatalystUses", maxCatalystUses);

        pTag.putFloat("catalystMult", catalystMult);
        pTag.putInt("toProduce", toProduce);

        pTag.putInt("progress", progress);
        pTag.putInt("maxProgress", maxProgress);

        if(altarPos != null){
            pTag.put("altarPos", NbtUtils.writeBlockPos(altarPos));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);

        CompoundTag inv = pTag.getCompound("inventory");
        itemHandler.deserializeNBT(registries, inv);

        energyStorage.setEnergy(pTag.getInt("energy"));

        catalystUses = pTag.getInt("catalystUses");
        maxCatalystUses = pTag.getInt("maxCatalystUses");

        catalystMult = pTag.getFloat("catalystMult");
        toProduce = pTag.getInt("toProduce");

        progress = pTag.getInt("progress");
        maxProgress = pTag.getInt("maxProgress");

        altarPos = NbtUtils.readBlockPos(pTag, "altarPos").orElse(null);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void drops() {
        SimpleContainer container = new SimpleContainer(itemHandler.getSlots());

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            container.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(getLevel(), getBlockPos(), container);
    }

    public void tick() {
        if(altarPos != null && level.hasChunkAt(altarPos) && level.getBlockEntity(altarPos) instanceof AraVitaeTile altar){
            bloodAltar = altar;
        } else {
            bloodAltar = null;
            altarMultiplier = 0;
        }

        if(maxProgress != Config.sacrificerSpeed) maxProgress = Config.sacrificerSpeed;

        boolean hasCatalyst = catalystUses > 0 || catalystUses == -1;
        Pair<Integer, Integer> modelStats = getModelFromStack(getModelStack());
        missingModel = getModelStack().isEmpty() || (modelStats.first == 0 && modelStats.second == 0);

        if(missingModel) {
            progress = 0;
            toProduce = 0;
            sync();
            return;
        }

        Pair<Float, Integer> catalyst = hasCatalyst ? Pair.of(0f, 0) : getCatalystFromStack(getCatalystStack());

        if (!hasCatalyst && catalyst.second != 0) {
            maxCatalystUses = catalyst.second;
            catalystMult = catalyst.first;
            catalystUses = maxCatalystUses;
            hasCatalyst = true;
            getCatalystStack().shrink(1);
            sync();
        }

        if (!hasCatalyst && (catalystUses <= 0 && catalystUses != -1)) {
            catalystMult = 1;
        }

        toProduce = (int) (modelStats.first * catalystMult);

        if(energyStorage.getEnergyStored() < modelStats.second) {
            sync();
            return;
        }

        if(bloodAltar == null) {
            sync();
            return;
        }

        altarMultiplier = 1 + bloodAltar.getSacrificeBonus();
        int baseProduction = toProduce;
        toProduce = (int) ((double) altarMultiplier * baseProduction);

        if(toProduce > bloodAltar.getCapacity() - bloodAltar.getCurrentBlood()) {
            sync();
            return;
        }

        progress++;
        energyStorage.setEnergy(energyStorage.getEnergyStored() - modelStats.second);

        if(progress < maxProgress) {
            sync();
            return;
        }

        if(hasCatalyst && catalystUses > 0) {
            catalystUses--;
        }

        progress = 0;

        bloodAltar.addSacrificeEV(baseProduction, true);

        if (ExtraHnnCompat.isCombined(getModelStack())) {
            ExtraHnnCompat.completeCycle(getModelStack());
            sync();
            return;
        }
        int data = DataModelItem.getData(getModelStack());

        DataModel model = DataModelItem.getStoredModel(getModelStack()).get();
        ModelTier tier = ModelTierRegistry.getByData(model, data);

        if(!tier.isMin() || Config.faultyData) {
            data = (int) Math.min(Integer.MAX_VALUE, (long) data + Config.sacrificerData);
        }

        DataModelItem.setData(getModelStack(), data);
        sync();
    }

    public void sync(){
        setChanged();
        if (level != null) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public Pair<Float, Integer> getCatalystFromStack(ItemStack stack){
        if(level == null || stack.isEmpty()) return Pair.of(0f, 0);
        if(getModelStack().isEmpty()) return Pair.of(0f, 0);
        List<CatalystRecipe> catalystRecipes = level.getRecipeManager().getAllRecipesFor(CatalystRecipe.Type.INSTANCE).stream().map(net.minecraft.world.item.crafting.RecipeHolder::value).toList();

        for (CatalystRecipe recipe : catalystRecipes) {
            if(recipe.getInput().test(stack)) {
                return Pair.of(recipe.getMultiplier(), recipe.getUses());
            }
        }

        return Pair.of(0f, 0);
    }

    public ItemStack getModelStack(){
        return itemHandler.getStackInSlot(0);
    }

    public Pair<Integer, Integer> getModelFromStack(ItemStack stack){
        if (ExtraHnnCompat.isCombined(stack)) return Config.extraHnnModelsEnabled ? ExtraHnnCompat.getStats(stack, level) : Pair.of(0, 0);
        if (level == null || !(stack.getItem() instanceof DataModelItem)) return Pair.of(0, 0);
        DynamicHolder<DataModel> model = DataModelItem.getStoredModel(stack);
        if (!model.isBound()) return Pair.of(0, 0);
        ModelTier tier = ModelTierRegistry.getByData(model.get(), DataModelItem.getData(stack));
        for (var holder : level.getRecipeManager().getAllRecipesFor(ModelRecipe.Type.INSTANCE)) {
            ModelRecipe recipe = holder.value();
            var entity = BuiltInRegistries.ENTITY_TYPE.getOptional(recipe.getEntity());
            if (entity.isEmpty()) continue;
            var candidates = DataModelRegistry.INSTANCE.getForEntity(entity.get());
            if (candidates.contains(model.get())) {
                return Pair.of(recipe.getBlood(tier), recipe.getEnergy());
            }
        }
        return Pair.of(0, 0);
    }

    public ItemStack getCatalystStack(){
        return itemHandler.getStackInSlot(1);
    }

    public ItemStackHandler getInventory() {
        return itemHandler;
    }

    public int getRFTick(){
        return getModelFromStack(getModelStack()).second;
    }

    public void setBloodAltar(BlockPos pos){
        if (!(level.getBlockEntity(pos) instanceof AraVitaeTile altar)) return;
        altarPos = pos;
        bloodAltar = altar;
        sync();
    }
}
