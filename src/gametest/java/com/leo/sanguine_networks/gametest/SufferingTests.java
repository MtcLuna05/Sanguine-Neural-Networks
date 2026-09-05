package com.leo.sanguine_networks.gametest;

import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.leo.sanguine_networks.Config;
import com.leo.sanguine_networks.block.SufferingBlock;
import com.leo.sanguine_networks.block.SufferingIOPortBlock;
import com.leo.sanguine_networks.block.SufferingIOPortBlock.Mode;
import com.leo.sanguine_networks.block.entity.SufferingBlockEntity;
import com.leo.sanguine_networks.block.entity.SufferingIOPortBlockEntity;
import com.leo.sanguine_networks.init.ModBlocks;
import com.leo.sanguine_networks.init.ModItems;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.shadowsoffire.hostilenetworks.data.ModelTierRegistry;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("sanguine_networks")
@PrefixGameTestTemplate(false)
public class SufferingTests {
    private record Fixture(SufferingBlockEntity machine, SufferingIOPortBlockEntity energy, SufferingIOPortBlockEntity models,
                           SufferingIOPortBlockEntity catalysts, SufferingIOPortBlockEntity output, BlockPos min) {}
    private static ItemStack model(boolean selfAware) {
        var model = DataModelRegistry.INSTANCE.getForEntity(EntityType.ZOMBIE).iterator().next();
        var stack = new ItemStack(Hostile.Items.DATA_MODEL);
        DataModelItem.setStoredModel(stack, model);
        DataModelItem.setData(stack, selfAware ? model.getRequiredData(ModelTierRegistry.getMaxTier()) : 0);
        return stack;
    }
    private static Fixture fixture(GameTestHelper h) {
        var level = h.getLevel();
        BlockPos min = h.absolutePos(new BlockPos(1, 1, 1));
        for (int x = 0; x < 7; x++) for (int y = 0; y < 7; y++) for (int z = 0; z < 7; z++)
            level.setBlockAndUpdate(min.offset(x, y, z), y == 0 ? NVBlocks.RUNE_BLANK.block().get().defaultBlockState()
                : x == 0 || x == 6 || y == 6 || z == 0 || z == 6 ? NVBlocks.BLOOD_STAINED_GLASS.block().get().defaultBlockState()
                : Blocks.AIR.defaultBlockState());
        var controller = min.offset(3, 1, 0);
        level.setBlockAndUpdate(controller, ModBlocks.SUFFERING_INCORPORATED.get().defaultBlockState().setValue(SufferingBlock.FACING, Direction.NORTH));
        SufferingIOPortBlockEntity[] ports = new SufferingIOPortBlockEntity[4];
        for (int i = 0; i < 4; i++) {
            var pos = min.offset(0, 1, i + 1);
            level.setBlockAndUpdate(pos, ModBlocks.SUFFERING_IO_PORT.get().defaultBlockState().setValue(SufferingIOPortBlock.MODE, Mode.values()[i]));
            ports[i] = (SufferingIOPortBlockEntity) level.getBlockEntity(pos);
        }
        var machine = (SufferingBlockEntity) level.getBlockEntity(controller);
        machine.refreshStructure();
        h.assertTrue(machine.getLayout() != null, "Rune/glass shell with custom ports must form");
        for (var port : ports) h.assertTrue(port.resolveOwner() == machine, "Ports must bind to the controller");
        var energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, ports[0].getBlockPos(), Direction.UP);
        h.assertTrue(energy != null && energy.receiveEnergy(1000000, false) == 1000000, "Energy port must accept power");
        return new Fixture(machine, ports[0], ports[1], ports[2], ports[3], min);
    }
    private static int fluid(Fixture f) { return f.machine.getFluidHandler().getFluidInTank(0).getAmount(); }

    @GameTest(template = "test_suffering")
    public static void multiblockConfigToggle(GameTestHelper h) {
        boolean old = Config.sufferingEnabled;
        int oldSpeed = Config.sufferingSpeed;
        Config.sufferingSpeed = 1;
        try {
            var f = fixture(h);
            f.machine.getInventory().setStackInSlot(0, model(true));
            f.machine.tick();
            int blood = fluid(f), energy = f.machine.getEnergyStorage().getEnergyStored();
            var cachedOutput = f.output.getFluidHandler();
            var cachedEnergy = f.energy.getEnergyHandler();
            Config.sufferingEnabled = false;
            h.assertTrue(cachedOutput.drain(100, IFluidHandler.FluidAction.EXECUTE).isEmpty()
                && cachedEnergy.receiveEnergy(100, false) == 0, "Cached ports immediately respect disabled multiblock");
            f.machine.tick();
            h.assertTrue(f.machine.getLayout() == null && f.output.resolveOwner() == null, "Disabling clears formation and port ownership");
            h.assertTrue(fluid(f) == blood && f.machine.getEnergyStorage().getEnergyStored() == energy
                && !f.machine.getInventory().getStackInSlot(0).isEmpty(), "Disabling preserves stored contents and stops processing");
            f.machine.refreshStructure();
            h.assertTrue(f.machine.getLayout() == null && f.machine.getData().get(0) == -1, "Disabled structure cannot reform and reports disabled state");
            Config.sufferingEnabled = true;
            f.machine.tick();
            h.assertTrue(f.machine.getLayout() != null && f.output.resolveOwner() == f.machine
                && fluid(f) == blood + 375, "Re-enabling reforms and resumes processing");
            h.succeed();
        } finally { Config.sufferingEnabled = old; Config.sufferingSpeed = oldSpeed; }
    }

    @GameTest(template = "test_suffering")
    public static void rejectsExtraModels(GameTestHelper h) {
        var f = fixture(h);
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(
            net.minecraft.resources.ResourceLocation.parse("extrahnn:extra_data_model"));
        if (item.isEmpty()) { h.succeed(); return; }
        var stack = new ItemStack(item.get());
        // Even forged regular-model components must not bypass combined-model rejection.
        var normal = model(true);
        DataModelItem.setStoredModel(stack, DataModelItem.getStoredModel(normal).get());
        DataModelItem.setData(stack, DataModelItem.getData(normal));
        h.assertTrue(!f.machine.getInventory().isItemValid(0, stack), "Controller inventory rejects Extra HNN models");
        h.assertTrue(!f.models.getItemHandler().insertItem(0, stack, false).isEmpty(), "Model input port rejects Extra HNN models");
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
        var menu = f.machine.createMenu(0, player.getInventory(), player);
        h.assertTrue(!menu.slots.get(0).mayPlace(stack), "GUI rejects Extra HNN models");
        f.machine.getInventory().setStackInSlot(0, stack);
        int energy = f.machine.getEnergyStorage().getEnergyStored();
        f.machine.tick();
        h.assertTrue(f.machine.getStatus(0) == SufferingBlockEntity.NO_RECIPE
            && f.machine.getEnergyStorage().getEnergyStored() == energy && fluid(f) == 0,
            "Forced combined model cannot run in multiblock");
        h.succeed();
    }

    @GameTest(template = "test_suffering")
    public static void hnnRedstoneAndAnimationActivation(GameTestHelper h) {
        int oldSpeed = Config.sufferingSpeed;
        Config.sufferingSpeed = 5;
        try {
            var f = fixture(h);
            f.machine.getInventory().setStackInSlot(0, model(true));
            f.machine.tick();
            h.assertTrue(f.machine.getActiveSlotsMask() == 1, "Running model immediately activates HNN hologram");
            var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
            player.setPos(f.machine.getBlockPos().getX() + 0.5, f.machine.getBlockPos().getY(), f.machine.getBlockPos().getZ() - 1);
            var menu = (com.leo.sanguine_networks.block.menu.SufferingMenu) f.machine.createMenu(0, player.getInventory(), player);
            h.assertTrue(menu.slots.get(25).x == 132 && menu.slots.get(25).y == 20
                && menu.slots.get(29).x == 35 && menu.slots.get(29).y == 140, "Input and player slots match HNN texture");
            h.assertTrue(!menu.clickMenuButton(player, 1999) && !menu.clickMenuButton(player, 2003), "Reject invalid button IDs");
            h.assertTrue(menu.clickMenuButton(player, 2002), "HNN redstone button changes server mode");
            int energy = f.machine.getEnergyStorage().getEnergyStored();
            f.machine.tick();
            h.assertTrue(f.machine.getActiveSlotsMask() == 0 && f.machine.getProgress(0) == 1
                && f.machine.getEnergyStorage().getEnergyStored() == energy, "Redstone pause stops hologram and preserves cycle");
            var saved = f.machine.saveWithoutMetadata(h.getLevel().registryAccess());
            h.assertTrue(saved.getInt("redstoneState") == 2, "Redstone mode persists");
            f.machine.loadWithComponents(saved, h.getLevel().registryAccess());
            f.machine.refreshStructure();
            h.getLevel().setBlockAndUpdate(f.machine.getBlockPos().relative(Direction.NORTH), Blocks.REDSTONE_BLOCK.defaultBlockState());
            f.machine.tick();
            h.assertTrue(f.machine.getActiveSlotsMask() == 1 && f.machine.getProgress(0) == 2, "Powered controller resumes cycle and hologram");
            menu.clickMenuButton(player, 2001);
            f.machine.tick();
            h.assertTrue(f.machine.getActiveSlotsMask() == 0 && f.machine.getProgress(0) == 2, "Off-when-powered pauses");
            menu.clickMenuButton(player, 2000);
            f.machine.tick();
            h.assertTrue(f.machine.getActiveSlotsMask() == 1 && f.machine.getProgress(0) == 3, "Ignored redstone resumes");
            h.succeed();
        } finally { Config.sufferingSpeed = oldSpeed; }
    }

    @GameTest(template = "test_suffering")
    public static void additiveBoostsAndPerModelUses(GameTestHelper h) {
        int oldSpeed = Config.sufferingSpeed;
        Config.sufferingSpeed = 1;
        try {
            var f = fixture(h);
            var models = h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, f.models.getBlockPos(), Direction.UP);
            var catalysts = h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, f.catalysts.getBlockPos(), Direction.UP);
            h.assertTrue(models.getSlots() == 25 && catalysts.getSlots() == 4, "Ports expose exactly their own slots");
            for (int i = 0; i < 25; i++) h.assertTrue(models.insertItem(i, model(true), false).isEmpty(), "Model insertion must succeed");
            for (int i = 0; i < 4; i++) catalysts.insertItem(i, new ItemStack(Items.NETHER_STAR), false);
            h.assertTrue(models.extractItem(0, 1, false).isEmpty(), "Model ports are insert-only");
            h.assertTrue(!catalysts.isItemValid(0, new ItemStack(Items.DIRT)), "Reject invalid catalyst");
            f.machine.tick();
            h.assertTrue(fluid(f) == 25 * 375 * 20, "Four 5x catalysts produce 20x, for each of 25 models");
            h.assertTrue(f.machine.getEnergyStorage().getEnergyStored() == 1000000 - 25 * 625, "Four catalyst slots cost 2.5x energy");
            for (int i = 0; i < 4; i++) h.assertTrue(f.machine.getCatalystUses(i) == 75, "Every slot spends 25 uses for 25 completed models");
            h.assertTrue(f.machine.getCatalystMultiplier() == 20, "Boost is additive");
            for (int i = 0; i <= 4; i++) h.assertTrue(SufferingBlockEntity.energyCost(250, i) == new int[]{250,250,375,500,625}[i], "Energy scales linearly");
            var saved = f.machine.saveWithoutMetadata(h.getLevel().registryAccess());
            var restored = new SufferingBlockEntity(f.machine.getBlockPos(), f.machine.getBlockState());
            restored.setLevel(h.getLevel());
            restored.loadWithComponents(saved, h.getLevel().registryAccess());
            h.assertTrue(restored.getFluidHandler().getFluidInTank(0).getAmount() == fluid(f), "Fluid persists");
            h.assertTrue(restored.getCatalystUses(3) == 75 && restored.getCatalystMultiplier() == 20, "Catalyst charges and boosts persist");
            h.assertTrue(ItemStack.matches(restored.getInventory().getStackInSlot(24), f.machine.getInventory().getStackInSlot(24)), "Model components persist");
            var menu = f.machine.createMenu(0, net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel()).getInventory(),
                net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel()));
            h.assertTrue(menu.slots.size() == 65 && menu.slots.get(29).y == 140, "Menu exposes all slots with HNN inventory spacing");
            h.succeed();
        } finally { Config.sufferingSpeed = oldSpeed; }
    }

    @GameTest(template = "test_suffering")
    public static void decorativeRunesAndInvalidShell(GameTestHelper h) {
        int oldSpeed = Config.sufferingSpeed;
        Config.sufferingSpeed = 1;
        try {
            var f = fixture(h);
            f.machine.getInventory().setStackInSlot(0, model(true));
            f.machine.tick();
            h.assertTrue(fluid(f) == 375, "Unboosted self-aware output");
            h.getLevel().setBlockAndUpdate(f.min, NVBlocks.RUNE_SACRIFICE.block().get().defaultBlockState());
            f.machine.refreshStructure();
            h.assertTrue(f.machine.getLayout() != null, "Sacrifice runes may replace blank runes");
            f.machine.tick();
            h.assertTrue(fluid(f) == 750, "Shell sacrifice runes have no effect");
            var cachedOutput = f.output.getFluidHandler();
            h.assertTrue(cachedOutput.drain(50, IFluidHandler.FluidAction.SIMULATE).getAmount() == 50 && fluid(f) == 750, "Fluid simulation must not drain");
            h.assertTrue(cachedOutput.drain(50, IFluidHandler.FluidAction.EXECUTE).getAmount() == 50 && fluid(f) == 700, "Output port allows extraction");
            h.assertTrue(cachedOutput.fill(new net.neoforged.neoforge.fluids.FluidStack(com.breakinblocks.neovitae.common.fluid.NVFluids.ESSENTIA_VITAE_SOURCE.get(), 50), IFluidHandler.FluidAction.EXECUTE) == 0, "Output rejects filling");
            var cachedEnergy = f.energy.getEnergyHandler();
            h.getLevel().setBlockAndUpdate(f.energy.getBlockPos(), f.energy.getBlockState().setValue(SufferingIOPortBlock.MODE, Mode.MODELS));
            h.assertTrue(cachedEnergy.receiveEnergy(50, false) == 0, "Cached capability respects new mode");
            h.getLevel().setBlockAndUpdate(f.min.offset(6, 3, 3), Blocks.GLASS.defaultBlockState());
            f.machine.refreshStructure();
            int energy = f.machine.getEnergyStorage().getEnergyStored();
            f.machine.tick();
            h.assertTrue(f.machine.getLayout() == null && fluid(f) == 700 && f.machine.getEnergyStorage().getEnergyStored() == energy, "Wrong glass invalidates and pauses machine");
            h.assertTrue(cachedOutput.drain(50, IFluidHandler.FluidAction.EXECUTE).isEmpty(), "Cached ports stop after shell invalidation");
            h.succeed();
        } finally { Config.sufferingSpeed = oldSpeed; }
    }

    @GameTest(template = "test_suffering")
    public static void exhaustionSelfAwareAndInfiniteUses(GameTestHelper h) {
        int oldSpeed = Config.sufferingSpeed;
        Config.sufferingSpeed = 1;
        try {
            var f = fixture(h);
            f.machine.getInventory().setStackInSlot(0, model(false));
            f.machine.getInventory().setStackInSlot(25, new ItemStack(Hostile.Items.OVERWORLD_PREDICTION));
            f.machine.tick();
            h.assertTrue(fluid(f) == 0 && f.machine.getInventory().getStackInSlot(25).getCount() == 1, "Non-self-aware model must not run or load catalyst");
            for (int i = 0; i < 25; i++) f.machine.getInventory().setStackInSlot(i, model(true));
            f.machine.tick();
            h.assertTrue(fluid(f) == 10 * 562 + 15 * 375, "Ten-use catalyst boosts exactly ten model completions");
            h.assertTrue(f.machine.getCatalystUses(0) == 0, "Exhausted catalyst has no remaining charges");
            f.machine.getInventory().setStackInSlot(25, new ItemStack(Items.BARRIER));
            int before = fluid(f);
            f.machine.tick();
            h.assertTrue(fluid(f) - before == 25 * 375 * 5 && f.machine.getCatalystUses(0) == -1, "Infinite catalyst boosts every model without decrement");
            h.succeed();
        } finally { Config.sufferingSpeed = oldSpeed; }
    }

    @GameTest(template = "test_suffering")
    public static void pausedCyclesAndProgressPersistence(GameTestHelper h) {
        int oldSpeed = Config.sufferingSpeed;
        Config.sufferingSpeed = 3;
        try {
            var f = fixture(h);
            f.machine.getInventory().setStackInSlot(0, model(true));
            f.machine.getInventory().setStackInSlot(25, new ItemStack(Items.NETHER_STAR));
            f.machine.tick();
            f.machine.tick();
            h.assertTrue(fluid(f) == 0 && f.machine.getCatalystUses(0) == 100, "No catalyst use before completion");
            var saved = f.machine.saveWithoutMetadata(h.getLevel().registryAccess());
            saved.putInt("energy", 0);
            f.machine.loadWithComponents(saved, h.getLevel().registryAccess());
            f.machine.refreshStructure();
            f.machine.tick();
            h.assertTrue(f.machine.getProgress(0) == 2 && f.machine.getCatalystUses(0) == 100 && fluid(f) == 0, "Unpowered machine preserves progress and charges");
            f.energy.getEnergyHandler().receiveEnergy(10000, false);
            f.machine.tick();
            h.assertTrue(fluid(f) == 1875 && f.machine.getCatalystUses(0) == 99, "Restored cycle completes exactly once");
            var fullTank = new net.neoforged.neoforge.fluids.capability.templates.FluidTank(Config.sufferingTank);
            fullTank.fill(new net.neoforged.neoforge.fluids.FluidStack(com.breakinblocks.neovitae.common.fluid.NVFluids.ESSENTIA_VITAE_SOURCE.get(), Config.sufferingTank),
                IFluidHandler.FluidAction.EXECUTE);
            saved = f.machine.saveWithoutMetadata(h.getLevel().registryAccess());
            saved.put("tank", fullTank.writeToNBT(h.getLevel().registryAccess(), new net.minecraft.nbt.CompoundTag()));
            f.machine.loadWithComponents(saved, h.getLevel().registryAccess());
            f.machine.refreshStructure();
            int energy = f.machine.getEnergyStorage().getEnergyStored();
            f.machine.tick();
            h.assertTrue(f.machine.getEnergyStorage().getEnergyStored() == energy && f.machine.getCatalystUses(0) == 99
                && f.machine.getStatus(0) == SufferingBlockEntity.TANK_FULL, "Full tank pauses energy and catalyst consumption");
            h.succeed();
        } finally { Config.sufferingSpeed = oldSpeed; }
    }

    @GameTest(template = "test_suffering")
    public static void linkedOutputPortAndFullAltar(GameTestHelper h) {
        int oldSpeed = Config.sufferingSpeed;
        Config.sufferingSpeed = 1;
        try {
            var f = fixture(h);
            var pos = f.min.offset(8, 1, 0);
            h.getLevel().setBlockAndUpdate(pos, net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse("neovitae:ara_vitae")).defaultBlockState());
            var altar = (AraVitaeTile) h.getLevel().getBlockEntity(pos);
            altar.calculateStats(java.util.Map.of(), java.util.List.of());
            var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
            var wrench = new ItemStack(ModItems.WRENCH.get());
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, wrench);
            for (BlockPos target : new BlockPos[]{pos, f.energy.getBlockPos(), f.output.getBlockPos()}) {
                var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(target), Direction.UP, target, false);
                wrench.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hit));
            }
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            h.assertTrue(f.energy.getAltarPos() == null && pos.equals(f.output.getAltarPos()), "Wrench links only output ports");
            f.machine.getInventory().setStackInSlot(0, model(true));
            f.machine.tick();
            f.output.tick();
            h.assertTrue(altar.getCurrentBlood() == 375 && fluid(f) == 0, "Linked port transfers EV directly to altar: altar=" + altar.getCurrentBlood() + ", tank=" + fluid(f) + ", bonus=" + altar.getSacrificeBonus() + ", owner=" + (f.output.resolveOwner() != null) + ", capacity=" + altar.getCapacity());
            altar.addSacrificeEV(altar.getCapacity(), true);
            f.machine.tick();
            f.output.tick();
            h.assertTrue(fluid(f) == 375, "Full altar preserves buffered output");
            altar.drainMainTank(100);
            f.output.tick();
            h.assertTrue(fluid(f) == 275 && altar.getCurrentBlood() == altar.getCapacity(), "Partial transfer never loses overflow");
            altar.drainMainTank(altar.getCapacity());
            altar.calculateStats(java.util.Map.of(), java.util.List.of(new com.breakinblocks.neovitae.api.altar.rune.RuneInstance(
                pos.below(), NVBlocks.RUNE_SACRIFICE.block().get(), null)));
            int expected = (int) ((double) (1f + altar.getSacrificeBonus()) * fluid(f));
            f.output.tick();
            h.assertTrue(fluid(f) == 0 && altar.getCurrentBlood() == expected, "Receiving altar runes apply exactly once");
            var saved = f.output.saveWithoutMetadata(h.getLevel().registryAccess());
            var restored = new SufferingIOPortBlockEntity(f.output.getBlockPos(), f.output.getBlockState());
            restored.loadWithComponents(saved, h.getLevel().registryAccess());
            h.assertTrue(pos.equals(restored.getAltarPos()) && f.machine.getBlockPos().equals(restored.getOwnerPos()), "Port links persist");
            h.getLevel().removeBlock(f.machine.getBlockPos(), false);
            h.assertTrue(f.output.resolveOwner() == null, "Breaking controller disconnects port");
            h.succeed();
        } finally { Config.sufferingSpeed = oldSpeed; }
    }
}
