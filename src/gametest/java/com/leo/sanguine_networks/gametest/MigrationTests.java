package com.leo.sanguine_networks.gametest;

import com.breakinblocks.neovitae.api.altar.rune.RuneInstance;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.leo.sanguine_networks.Config;
import com.leo.sanguine_networks.block.entity.VSBlockEntity;
import com.leo.sanguine_networks.init.ModBlocks;
import com.leo.sanguine_networks.recipe.ModelRecipe;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.shadowsoffire.hostilenetworks.data.ModelTierRegistry;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;

@GameTestHolder("sanguine_networks")
@PrefixGameTestTemplate(false)
public class MigrationTests {
    private record Fixture(VSBlockEntity machine, AraVitaeTile altar) {}

    private static Fixture fixture(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos machinePos = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos altarPos = helper.absolutePos(new BlockPos(3, 1, 1));
        level.setBlockAndUpdate(machinePos, ModBlocks.VIRTUAL_SACRIFICER.get().defaultBlockState());
        level.setBlockAndUpdate(altarPos, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("neovitae:ara_vitae")).defaultBlockState());
        var machine = (VSBlockEntity) level.getBlockEntity(machinePos);
        var altar = (AraVitaeTile) level.getBlockEntity(altarPos);
        helper.assertTrue(altar != null, "NeoVitae altar must register");
        machine.setBloodAltar(altarPos);
        var model = DataModelRegistry.INSTANCE.getForEntity(EntityType.ZOMBIE).iterator().next();
        var stack = new ItemStack(Hostile.Items.DATA_MODEL);
        DataModelItem.setStoredModel(stack, model);
        DataModelItem.setData(stack, model.getRequiredData(ModelTierRegistry.getMaxTier()));
        machine.getInventory().setStackInSlot(0, stack);
        var energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, machinePos, Direction.UP);
        helper.assertTrue(energy != null, "Energy capability must be exposed");
        energy.receiveEnergy(energy.getMaxEnergyStored(), false);
        helper.assertTrue(level.getCapability(Capabilities.ItemHandler.BLOCK, machinePos, Direction.DOWN) == machine.getInventory(), "Item capability must expose inventory");
        return new Fixture(machine, altar);
    }

    @GameTest(template = "test_empty")
    public static void sacrificeAndPersistence(GameTestHelper helper) {
        var f = fixture(helper);
        var rune = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("neovitae:rune_sacrifice"));
        f.altar.calculateStats(Map.of(), List.of(new RuneInstance(f.altar.getBlockPos().below(), rune, null)));
        helper.assertTrue(f.altar.getSacrificeBonus() > 0, "Rune data map must load");
        f.machine.getInventory().setStackInSlot(1, new ItemStack(Hostile.Items.OVERWORLD_PREDICTION));
        int originalData = DataModelItem.getData(f.machine.getModelStack());
        int originalEnergy = f.machine.getEnergyStorage().getEnergyStored();
        var production = f.machine.getModelFromStack(f.machine.getModelStack());
        int expected = (int) ((double) (1 + f.altar.getSacrificeBonus()) * (int) (production.first * 1.5f));
        for (int i = 0; i < Config.sacrificerSpeed; i++) f.machine.tick();
        helper.assertTrue(f.altar.getCurrentBlood() == expected, "EV must apply catalyst and rune bonuses exactly once");
        helper.assertTrue(f.machine.getEnergyStorage().getEnergyStored() == originalEnergy - production.second * Config.sacrificerSpeed, "Operation must consume configured energy");
        helper.assertTrue(DataModelItem.getData(f.machine.getModelStack()) == originalData + Config.sacrificerData, "Model components must gain data");
        var saved = f.machine.saveWithoutMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(saved.getInt("catalystUses") == 9, "First catalyst operation must consume one use");
        var restored = new VSBlockEntity(f.machine.getBlockPos(), f.machine.getBlockState());
        restored.setLevel(helper.getLevel());
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(ItemStack.matches(restored.getModelStack(), f.machine.getModelStack()), "Model components must survive save/load");
        helper.assertTrue(restored.getEnergyStorage().getEnergyStored() == f.machine.getEnergyStorage().getEnergyStored(), "Energy must survive save/load");
        for (int i = 0; i < Config.sacrificerSpeed; i++) restored.tick();
        helper.assertTrue(f.altar.getCurrentBlood() == 2 * expected, "Saved altar link and catalyst must survive save/load");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void fullAltarAndInvalidModel(GameTestHelper helper) {
        var f = fixture(helper);
        f.altar.addSacrificeEV(f.altar.getCapacity(), true);
        int originalEnergy = f.machine.getEnergyStorage().getEnergyStored();
        int originalData = DataModelItem.getData(f.machine.getModelStack());
        for (int i = 0; i < Config.sacrificerSpeed; i++) f.machine.tick();
        helper.assertTrue(f.machine.getEnergyStorage().getEnergyStored() == originalEnergy, "Full altar must pause energy consumption");
        helper.assertTrue(DataModelItem.getData(f.machine.getModelStack()) == originalData, "Full altar must pause model training");
        f.machine.getInventory().setStackInSlot(0, new ItemStack(Hostile.Items.DATA_MODEL));
        f.machine.getInventory().setStackInSlot(1, new ItemStack(Items.DIRT));
        f.machine.tick();
        helper.assertTrue(f.machine.getCatalystStack().is(Items.DIRT), "Invalid catalysts must not be consumed");
        helper.assertTrue(f.machine.getModelFromStack(f.machine.getModelStack()).first == 0, "Unbound models must be safe");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void wrenchLinksAndRejectsMissingAltar(GameTestHelper helper) {
        var f = fixture(helper);
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
        var wrench = new ItemStack(com.leo.sanguine_networks.init.ModItems.WRENCH.get());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, wrench);
        var item = wrench.getItem();
        var altarHit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(f.altar.getBlockPos()), Direction.UP, f.altar.getBlockPos(), false);
        item.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, altarHit));
        var data = wrench.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag();
        helper.assertTrue(net.minecraft.nbt.NbtUtils.readBlockPos(data, "altar").orElseThrow().equals(f.altar.getBlockPos()), "Wrench must store altar position in components");
        helper.assertTrue(data.getString("dimension").equals(helper.getLevel().dimension().location().toString()), "Wrench must remember dimension");
        var fresh = new VSBlockEntity(f.machine.getBlockPos(), f.machine.getBlockState());
        helper.getLevel().setBlockEntity(fresh);
        var machineHit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(fresh.getBlockPos()), Direction.UP, fresh.getBlockPos(), false);
        var context = new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, machineHit);
        item.useOn(context);
        helper.assertTrue(fresh.saveWithoutMetadata(helper.getLevel().registryAccess()).contains("altarPos"), "Wrench must link the machine");
        helper.getLevel().removeBlock(f.altar.getBlockPos(), false);
        item.useOn(context);
        fresh.tick();
        helper.assertTrue(fresh.getEnergyStorage().getEnergyStored() == 0, "Removed altar must be handled safely");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void recipeCodecs(GameTestHelper helper) {
        var recipe = helper.getLevel().getRecipeManager().getAllRecipesFor(ModelRecipe.Type.INSTANCE).getFirst().value();
        var serializer = ModelRecipe.Serializer.INSTANCE;
        var json = serializer.codec().codec().encodeStart(com.mojang.serialization.JsonOps.INSTANCE, recipe).getOrThrow();
        var decoded = serializer.codec().codec().parse(com.mojang.serialization.JsonOps.INSTANCE, json).getOrThrow();
        helper.assertTrue(decoded.getEntity().equals(recipe.getEntity()) && java.util.Arrays.equals(decoded.getBlood(), recipe.getBlood()), "Recipe JSON must round-trip");
        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            serializer.streamCodec().encode(buffer, recipe);
            decoded = serializer.streamCodec().decode(buffer);
            helper.assertTrue(decoded.getEnergy() == recipe.getEnergy() && java.util.Arrays.equals(decoded.getBlood(), recipe.getBlood()), "Recipe network data must round-trip");
        } finally {
            buffer.release();
        }
        var invalid = com.google.gson.JsonParser.parseString("{\"entity\":\"minecraft:zombie\",\"blood\":[1],\"energy\":1}");
        helper.assertTrue(serializer.codec().codec().parse(com.mojang.serialization.JsonOps.INSTANCE, invalid).error().isPresent(), "Malformed tier arrays must be rejected");
        helper.succeed();
    }
}
