package com.leo.sanguine_networks.gametest;

import wayoftime.bloodmagic.common.tile.TileAltar;
import com.leo.sanguine_networks.Config;
import com.leo.sanguine_networks.block.entity.VSBlockEntity;
import com.leo.sanguine_networks.init.ModBlocks;
import com.leo.sanguine_networks.recipe.ModelRecipe;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import net.lmor.extrahnn.ExtraHostile;
import net.lmor.extrahnn.item.ExtraDataModelItem;
import net.lmor.extrahnn.data.ExtraModelTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@GameTestHolder("sanguine_networks")
@PrefixGameTestTemplate(false)
public class ExtraHnnTests {
    private record Fixture(VSBlockEntity machine, TileAltar altar) {}
    private static Fixture fixture(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(1, 1, 1));
        var altarPos = h.absolutePos(new BlockPos(3, 1, 1));
        h.getLevel().setBlockAndUpdate(pos, ModBlocks.VIRTUAL_SACRIFICER.get().defaultBlockState());
        h.getLevel().setBlockAndUpdate(altarPos, net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(new ResourceLocation("bloodmagic:altar")).defaultBlockState());
        var machine = (VSBlockEntity) h.getLevel().getBlockEntity(pos);
        var altar = (TileAltar) h.getLevel().getBlockEntity(altarPos);
        machine.setBloodAltar(altarPos);
        machine.getEnergyStorage().receiveEnergy(1000000, false);
        return new Fixture(machine, altar);
    }
    private static ItemStack combined(EntityType<?>... types) {
        var stack = new ItemStack(ExtraHostile.Items.EXTRA_DATA_MODEL.get());
        ExtraDataModelItem.setStoredModels(stack, Arrays.stream(types)
            .<dev.shadowsoffire.hostilenetworks.data.DataModel>map(type -> DataModelRegistry.INSTANCE.getForEntity(type)).toList());
        return stack;
    }
    private static ModelRecipe recipe(GameTestHelper h, String name) {
        return h.getLevel().getRecipeManager().getAllRecipesFor(ModelRecipe.Type.INSTANCE).stream()
            .filter(r -> r.getEntity().equals(new ResourceLocation(name))).findFirst().orElseThrow();
    }

    @GameTest(template = "test_empty")
    public static void combinedModelConfigToggle(GameTestHelper h) {
        boolean old = Config.extraHnnModelsEnabled;
        try {
            var f = fixture(h);
            var stack = combined(EntityType.BLAZE, EntityType.BLAZE, EntityType.BLAZE, EntityType.BLAZE);
            f.machine.getInventory().setStackInSlot(0, stack);
            f.machine.getInventory().setStackInSlot(1, new ItemStack(Items.NETHER_STAR));
            Config.extraHnnModelsEnabled = false;
            h.assertTrue(!VSBlockEntity.acceptsModel(stack) && !f.machine.getInventory().isItemValid(0, stack), "Disabled combined models cannot be inserted");
            h.assertTrue(VSBlockEntity.acceptsModel(new ItemStack(Hostile.Items.DATA_MODEL.get())), "Regular models remain accepted");
            int energy = f.machine.getEnergyStorage().getEnergyStored();
            f.machine.tick();
            h.assertTrue(f.machine.getEnergyStorage().getEnergyStored() == energy && f.altar.getCurrentBlood() == 0
                && f.machine.getCatalystStack().getCount() == 1, "Existing combined models stop without spending energy or catalyst");
            Config.extraHnnModelsEnabled = true;
            h.assertTrue(VSBlockEntity.acceptsModel(stack) && f.machine.getModelFromStack(stack).second == 40000, "Re-enabling restores combined model support");
            h.succeed();
        } finally { Config.extraHnnModelsEnabled = old; }
    }

    @GameTest(template = "test_empty")
    public static void fourModelsOneCycleAndPersistence(GameTestHelper h) {
        int oldSpeed = Config.sacrificerSpeed;
        Config.sacrificerSpeed = 2;
        try {
            var f = fixture(h);
            var stack = combined(EntityType.BLAZE, EntityType.BLAZE, EntityType.BLAZE, EntityType.BLAZE);
            var input = f.machine.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).orElseThrow(IllegalStateException::new);
            h.assertTrue(input.insertItem(0, stack, false).isEmpty(), "Combined models enter the Virtual Sacrificer through automation");
            f.machine.getInventory().setStackInSlot(1, new ItemStack(Hostile.Items.OVERWORLD_PREDICTION.get()));
            var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
            var menu = f.machine.createMenu(0, player.getInventory(), player);
            h.assertTrue(menu.slots.get(36).mayPlace(stack), "GUI accepts combined models");
            int originalData = ExtraDataModelItem.getData(f.machine.getModelStack());
            int expectedBlood = (int) (recipe(h, "blaze").getExtraBlood(0) * 4 * 1.5f);
            f.machine.tick();
            h.assertTrue(f.machine.getEnergyStorage().getEnergyStored() == 960000, "Four blazes cost 40,000 FE per tick");
            h.assertTrue(f.altar.getCurrentBlood() == 0 && f.machine.saveWithoutMetadata().getInt("catalystUses") == 10,
                "No EV or catalyst use before the single combined cycle completes");
            f.machine.tick();
            h.assertTrue(f.machine.getEnergyStorage().getEnergyStored() == 920000, "Every cycle tick uses combined power cost");
            h.assertTrue(f.altar.getCurrentBlood() == expectedBlood, "One output sums all four tier-scaled inner models before catalyst bonus");
            h.assertTrue(f.machine.saveWithoutMetadata().getInt("catalystUses") == 9, "Four inner models consume exactly one catalyst use");
            h.assertTrue(ExtraDataModelItem.getData(f.machine.getModelStack()) == originalData + Config.sacrificerData, "Combined model gains data once");
            var saved = f.machine.saveWithoutMetadata();
            var restored = new VSBlockEntity(f.machine.getBlockPos(), f.machine.getBlockState());
            restored.setLevel(h.getLevel());
            restored.load(saved);
            h.assertTrue(ExtraDataModelItem.getStoredModels(restored.getModelStack()).size() == 4, "All inner models survive persistence");
            h.assertTrue(restored.getModelFromStack(restored.getModelStack()).second == 40000, "Restored combined model remains usable");
            f.altar.fillMainTank(f.altar.getCapacity());
            f.machine.tick();
            h.assertTrue(f.machine.getEnergyStorage().getEnergyStored() == 920000
                && f.machine.saveWithoutMetadata().getInt("catalystUses") == 9, "Full altar pauses combined production");
            h.succeed();
        } finally { Config.sacrificerSpeed = oldSpeed; }
    }

    @GameTest(template = "test_empty")
    public static void mixedModelsAndEveryExtraTier(GameTestHelper h) {
        var f = fixture(h);
        var stack = combined(EntityType.BLAZE, EntityType.ZOMBIE, EntityType.COW, EntityType.ENDERMAN);
        var recipes = List.of(recipe(h, "blaze"), recipe(h, "zombie"), recipe(h, "cow"), recipe(h, "enderman"));
        int expectedEnergy = 10 * (1000 + 250 + 100 + 1000);
        int[] blazeOutputs = {1500, 3000, 6000, 12000, 24000};
        for (int i = 0; i < blazeOutputs.length; i++)
            h.assertTrue(recipe(h, "blaze").getExtraBlood(i) == blazeOutputs[i], "Blaze tier " + i + " doubles the previous increase");
        var saturating = new ModelRecipe(new ResourceLocation("blaze"), new int[]{0, 1, 2, 100, Integer.MAX_VALUE}, 1, new ResourceLocation("sanguine_networks:test_overflow"));
        h.assertTrue(saturating.getExtraBlood(4) == Integer.MAX_VALUE, "Blood overflow saturates safely");
        for (var tier : ExtraModelTier.values()) {
            ExtraDataModelItem.setData(stack, tier.data().requiredData());
            var stats = f.machine.getModelFromStack(stack);
            int expected = recipes.stream().mapToInt(r -> r.getExtraBlood(tier.ordinal())).sum();
            h.assertTrue(stats.first == expected && stats.second == expectedEnergy,
                "Mixed model blood follows Extra HNN tier " + tier + " with tier-independent summed recipe power");
        }
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void invalidCombinedModelsAreInert(GameTestHelper h) {
        var f = fixture(h);
        var incomplete = combined(EntityType.BLAZE, EntityType.BLAZE, EntityType.BLAZE);
        f.machine.getInventory().setStackInSlot(0, incomplete);
        f.machine.getInventory().setStackInSlot(1, new ItemStack(Items.NETHER_STAR));
        f.machine.tick();
        h.assertTrue(f.machine.getEnergyStorage().getEnergyStored() == 1000000 && f.altar.getCurrentBlood() == 0
            && f.machine.getCatalystStack().getCount() == 1, "Incomplete combined model does not consume energy or catalyst");
        f.machine.getInventory().setStackInSlot(0, new ItemStack(ExtraHostile.Items.EXTRA_DATA_MODEL.get()));
        f.machine.tick();
        h.assertTrue(f.machine.getEnergyStorage().getEnergyStored() == 1000000, "Unbound combined model is safe");
        h.succeed();
    }
}
