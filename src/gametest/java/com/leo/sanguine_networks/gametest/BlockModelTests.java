package com.leo.sanguine_networks.gametest;

import com.leo.sanguine_networks.Config;
import com.leo.sanguine_networks.block.entity.VSBlockEntity;
import com.leo.sanguine_networks.init.ModBlocks;
import com.leo.sanguine_networks.recipe.ModelRecipe;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.HostileConfig;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.shadowsoffire.hostilenetworks.data.ModelTierRegistry;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("sanguine_networks")
@PrefixGameTestTemplate(false)
public class BlockModelTests {
    public static ItemStack ironModel() {
        var model = DataModelRegistry.INSTANCE.getForBlock(Blocks.IRON_ORE).iterator().next();
        var stack = new ItemStack(Hostile.Items.DATA_MODEL);
        DataModelItem.setStoredModel(stack, model);
        DataModelItem.setData(stack, model.getRequiredData(ModelTierRegistry.getMaxTier()));
        return stack;
    }

    @GameTest(template = "test_empty")
    public static void blockRecipesFollowHnnConfig(GameTestHelper h) {
        var recipe = h.getLevel().getRecipeManager().getAllRecipesFor(ModelRecipe.Type.INSTANCE).stream()
            .map(net.minecraft.world.item.crafting.RecipeHolder::value).filter(ModelRecipe::isBlockRecipe).findFirst().orElseThrow();
        if (!HostileConfig.enableBlockDataModels) {
            h.assertTrue(recipe.getModels().isEmpty(), "Disabled HNN block models cannot simulate or appear in JEI");
            h.succeed();
            return;
        }
        var stack = ironModel();
        h.assertTrue(recipe.matchesModel(DataModelItem.getStoredModel(stack).get()), "Block recipe resolves HNN block models");
        var variant = ModelRecipe.forBlock(ResourceLocation.withDefaultNamespace("deepslate_iron_ore"), new int[]{0, 10, 20, 30, 40}, 200);
        h.assertTrue(variant.matchesModel(DataModelItem.getStoredModel(stack).get()), "Block variants resolve the same model");
        var pos = h.absolutePos(new BlockPos(1, 1, 1));
        var altarPos = h.absolutePos(new BlockPos(3, 1, 1));
        h.getLevel().setBlockAndUpdate(pos, ModBlocks.VIRTUAL_SACRIFICER.get().defaultBlockState());
        h.getLevel().setBlockAndUpdate(altarPos, com.breakinblocks.neovitae.common.block.NVBlocks.ARA_VITAE.block().get().defaultBlockState());
        var machine = (VSBlockEntity) h.getLevel().getBlockEntity(pos);
        var altar = (com.breakinblocks.neovitae.common.blockentity.AraVitaeTile) h.getLevel().getBlockEntity(altarPos);
        altar.calculateStats(java.util.Map.of(), java.util.List.of());
        machine.setBloodAltar(altarPos);
        machine.getEnergyStorage().receiveEnergy(1000000, false);
        machine.getInventory().setStackInSlot(0, stack);
        machine.getInventory().setStackInSlot(1, new ItemStack(Hostile.Items.OVERWORLD_PREDICTION));
        int data = DataModelItem.getData(stack);
        for (int i = 0; i < Config.sacrificerSpeed; i++) machine.tick();
        h.assertTrue(altar.getCurrentBlood() == 60, "Block models produce the recipe amount with catalyst bonus");
        h.assertTrue(machine.getEnergyStorage().getEnergyStored() == 1000000 - 200 * Config.sacrificerSpeed, "Block recipes set power cost");
        h.assertTrue(machine.saveWithoutMetadata(h.getLevel().registryAccess()).getInt("catalystUses") == 9, "One catalyst use per block model cycle");
        h.assertTrue(DataModelItem.getData(stack) == data + Config.sacrificerData, "Block model gains configured simulation data");
        h.succeed();
    }

    @GameTest(template = "test_empty")
    public static void blockRecipeSerialization(GameTestHelper h) {
        var serializer = ModelRecipe.Serializer.INSTANCE;
        var recipe = ModelRecipe.forBlock(ResourceLocation.withDefaultNamespace("iron_ore"), new int[]{0, 10, 20, 30, 40}, 200);
        var json = serializer.codec().codec().encodeStart(com.mojang.serialization.JsonOps.INSTANCE, recipe).getOrThrow();
        h.assertTrue(json.getAsJsonObject().has("block") && !json.getAsJsonObject().has("entity"), "Block targets serialize separately from entity targets");
        var decoded = serializer.codec().codec().parse(com.mojang.serialization.JsonOps.INSTANCE, json).getOrThrow();
        h.assertTrue(recipe.getBlock().equals(decoded.getBlock()) && decoded.getEnergy() == 200, "Block JSON round-trips");
        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), h.getLevel().registryAccess());
        try {
            serializer.streamCodec().encode(buffer, recipe);
            decoded = serializer.streamCodec().decode(buffer);
            h.assertTrue(decoded.isBlockRecipe() && decoded.getBlock().equals(recipe.getBlock()) && decoded.getBlood()[4] == 40, "Block recipe packets preserve target kind and amounts");
        } finally { buffer.release(); }
        json.getAsJsonObject().addProperty("entity", "minecraft:zombie");
        h.assertTrue(serializer.codec().codec().parse(com.mojang.serialization.JsonOps.INSTANCE, json).error().isPresent(), "Ambiguous entity and block targets are rejected");
        h.succeed();
    }
}
