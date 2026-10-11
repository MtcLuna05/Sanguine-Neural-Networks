package com.luna.sanguine_networks.block.entity;

import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.luna.sanguine_networks.Config;
import com.luna.sanguine_networks.SanguineNeuralNetworks;
import com.luna.sanguine_networks.block.menu.VSacrificerMenu;
import com.luna.sanguine_networks.init.ModBlockEntities;
import com.luna.sanguine_networks.recipe.CatalystRecipe;
import com.luna.sanguine_networks.recipe.ModelRecipe;
import com.luna.sanguine_networks.util.Pair;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.ModelTier;
import dev.shadowsoffire.hostilenetworks.data.ModelTierRegistry;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VSBlockEntity extends BlockEntity implements MenuProvider {

	private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(2) {
        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {
            if (slot == 0) VSBlockEntity.this.progress = 0;
            VSBlockEntity.this.sync();
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return switch(slot){
	            case 0 -> acceptsModel(resource.toStack());
                default -> true;
            };

        }
    };
	private final SimpleEnergyHandler energyStorage;

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
	            case 0 -> VSBlockEntity.this.energyStorage.getAmountAsInt();
	            case 1 -> VSBlockEntity.this.energyStorage.getCapacityAsInt();
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
	    energyStorage = new SimpleEnergyHandler(Config.sacrificerEnergy, Config.sacrificerEnergy, 0) {
		    @Override
		    protected void onEnergyChanged(int previousAmount) {
			    setChanged();
		    }
	    };
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
	    return stack.getItem() instanceof DataModelItem;
    }

	public EnergyHandler getEnergyStorage() {
		return energyStorage;
	}

    @Override
    protected void saveAdditional(ValueOutput pTag) {
	    super.saveAdditional(pTag);
	    itemHandler.serialize(pTag.child("inventory"));

	    pTag.putInt("energy", energyStorage.getAmountAsInt());

        pTag.putInt("catalystUses", catalystUses);
        pTag.putInt("maxCatalystUses", maxCatalystUses);

        pTag.putFloat("catalystMult", catalystMult);
        pTag.putInt("toProduce", toProduce);

        pTag.putInt("progress", progress);
        pTag.putInt("maxProgress", maxProgress);

        if(altarPos != null){
	        pTag.store("altarPos", BlockPos.CODEC, altarPos);
        }
    }

    @Override
    protected void loadAdditional(ValueInput pTag) {
	    super.loadAdditional(pTag);

	    itemHandler.deserialize(pTag.childOrEmpty("inventory"));
	    for (var entry : pTag.childOrEmpty("inventory").listOrEmpty("Items",
			    com.mojang.serialization.Codec.mapPair(com.mojang.serialization.Codec.INT.fieldOf("Slot"), ItemStack.MAP_CODEC).codec())) {
		    int slot = entry.getFirst();
		    ItemStack stack = entry.getSecond();
		    if (slot >= 0 && slot < itemHandler.size()) itemHandler.set(slot, ItemResource.of(stack), stack.getCount());
	    }

	    energyStorage.set(pTag.getIntOr("energy", 0));

	    catalystUses = pTag.getIntOr("catalystUses", 0);
	    maxCatalystUses = pTag.getIntOr("maxCatalystUses", 0);

	    catalystMult = pTag.getFloatOr("catalystMult", 0);
	    toProduce = pTag.getIntOr("toProduce", 0);

	    progress = pTag.getIntOr("progress", 0);
	    maxProgress = pTag.getIntOr("maxProgress", 0);

	    altarPos = pTag.read("altarPos", BlockPos.CODEC).orElse(null);
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

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		drops();
		super.preRemoveSideEffects(pos, state);
	}

    public void drops() {
	    SimpleContainer container = new SimpleContainer(itemHandler.size());

	    for (int i = 0; i < itemHandler.size(); i++) {
		    container.setItem(i, ItemUtil.getStack(itemHandler, i));
        }

        Containers.dropContents(getLevel(), getBlockPos(), container);
    }

    public void tick() {
	    if (altarPos != null && level.isLoaded(altarPos) && level.getBlockEntity(altarPos) instanceof AraVitaeTile altar) {
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
	        ItemStack catalystStack = getCatalystStack();
	        catalystStack.shrink(1);
	        itemHandler.set(1, ItemResource.of(catalystStack), catalystStack.getCount());
            sync();
        }

        if (!hasCatalyst && (catalystUses <= 0 && catalystUses != -1)) {
            catalystMult = 1;
        }

        toProduce = (int) (modelStats.first * catalystMult);

	    if (energyStorage.getAmountAsInt() < modelStats.second) {
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
	    energyStorage.set(energyStorage.getAmountAsInt() - modelStats.second);

        if(progress < maxProgress) {
            sync();
            return;
        }

        if(hasCatalyst && catalystUses > 0) {
            catalystUses--;
        }

        progress = 0;

        bloodAltar.addSacrificeEV(baseProduction, true);

        int data = DataModelItem.getData(getModelStack());

        DataModel model = DataModelItem.getStoredModel(getModelStack()).get();
        ModelTier tier = ModelTierRegistry.getByData(model, data);

        if(!tier.isMin() || Config.faultyData) {
            data = (int) Math.min(Integer.MAX_VALUE, (long) data + Config.sacrificerData);
        }

	    ItemStack modelStack = getModelStack();
	    DataModelItem.setData(modelStack, data);
	    itemHandler.set(0, ItemResource.of(modelStack), modelStack.getCount());
        sync();
    }

    public void sync(){
        setChanged();
        if (level != null) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public Pair<Float, Integer> getCatalystFromStack(ItemStack stack){
        if(level == null || stack.isEmpty()) return Pair.of(0f, 0);
        if(getModelStack().isEmpty()) return Pair.of(0f, 0);
	    List<CatalystRecipe> catalystRecipes = com.luna.sanguine_networks.init.ModRecipes.getRecipes(level, CatalystRecipe.Type.INSTANCE).stream().map(net.minecraft.world.item.crafting.RecipeHolder::value).toList();

        for (CatalystRecipe recipe : catalystRecipes) {
            if(recipe.getInput().test(stack)) {
                return Pair.of(recipe.getMultiplier(), recipe.getUses());
            }
        }

        return Pair.of(0f, 0);
    }

    public ItemStack getModelStack(){
	    return ItemUtil.getStack(itemHandler, 0);
    }

    public Pair<Integer, Integer> getModelFromStack(ItemStack stack){
        if (level == null || !(stack.getItem() instanceof DataModelItem)) return Pair.of(0, 0);
        DynamicHolder<DataModel> model = DataModelItem.getStoredModel(stack);
        if (!model.isBound()) return Pair.of(0, 0);
        ModelTier tier = ModelTierRegistry.getByData(model.get(), DataModelItem.getData(stack));
	    for (var holder : com.luna.sanguine_networks.init.ModRecipes.getRecipes(level, ModelRecipe.Type.INSTANCE)) {
            ModelRecipe recipe = holder.value();
            if (recipe.matchesModel(model.get())) {
                return Pair.of(recipe.getBlood(tier), recipe.getEnergy());
            }
        }
        return Pair.of(0, 0);
    }

    public ItemStack getCatalystStack(){
	    return ItemUtil.getStack(itemHandler, 1);
    }

	public ItemStacksResourceHandler getInventory() {
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
