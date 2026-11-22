package igentuman.nc.gametest;

import igentuman.nc.api.pipe.ConnectorMode;
import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import igentuman.nc.block_entity.storage.BarrelBE;
import igentuman.nc.block_entity.storage.BatteryBE;
import igentuman.nc.block_entity.storage.ContainerBE;
import igentuman.nc.handler.storage.ContainerInventoryStore;
import igentuman.nc.setup.level.ModBiomes;
import igentuman.nc.util.persistence.CurrentSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;

/** Runs core smoke tests on a dedicated GameTest server. */
@Mod(NuclearCraftSmokeTests.MOD_ID)
public final class NuclearCraftSmokeTests {
    public static final String MOD_ID = "nc_gametest";

    private record SmokeTest(String name, int timeout, Identifier structure, Consumer<GameTestHelper> run) {}

    private static final Identifier WORKSPACE = Identifier.fromNamespaceAndPath(MOD_ID, "workspace");
    private static final List<SmokeTest> TESTS = List.of(
            new SmokeTest("rtg_charges_battery", 40, WORKSPACE, NuclearCraftSmokeTests::rtgChargesBattery),
            new SmokeTest("hopper_manufactory", 400, WORKSPACE, NuclearCraftSmokeTests::hopperManufactory),
            new SmokeTest("linked_container_persistence", 40, WORKSPACE, NuclearCraftSmokeTests::linkedContainerPersistence),
            new SmokeTest("item_pipe_reconnect", 80, WORKSPACE, NuclearCraftSmokeTests::itemPipeReconnect),
            new SmokeTest("fluid_pipe_transaction", 40, WORKSPACE, NuclearCraftSmokeTests::fluidPipeTransaction),
            new SmokeTest("wasteland_biome", 20, Identifier.withDefaultNamespace("empty"),
                    NuclearCraftSmokeTests::wastelandBiome)
    );

    public NuclearCraftSmokeTests(IEventBus modBus) {
        DeferredRegister<Consumer<GameTestHelper>> functions = DeferredRegister.create(Registries.TEST_FUNCTION, MOD_ID);
        for (SmokeTest test : TESTS) {
            functions.register(test.name(), test::run);
        }
        functions.register(modBus);
        modBus.addListener(NuclearCraftSmokeTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment =
                event.registerEnvironment(Identifier.fromNamespaceAndPath(MOD_ID, "smoke"));
        for (SmokeTest test : TESTS) {
            Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, test.name());
            event.registerTest(id, new FunctionGameTestInstance(
                    ResourceKey.create(Registries.TEST_FUNCTION, id),
                    new TestData<>(environment, test.structure(), test.timeout(), 0, true)));
        }
    }

    private static void rtgChargesBattery(GameTestHelper helper) {
        BlockPos batteryPos = new BlockPos(1, 0, 1);
        helper.setBlock(batteryPos, block(helper, "basic_lithium_ion_battery"));
        BatteryBE battery = helper.getBlockEntity(batteryPos, BatteryBE.class);
        var level = helper.getLevel();
        var energy = level.getCapability(Capabilities.Energy.BLOCK, battery.getBlockPos(), Direction.WEST);
        if (energy == null) {
            throw helper.assertionException(batteryPos, "Battery has no energy capability on its west side");
        }
        helper.assertValueEqual(energy.getAmountAsLong(), 0L, "initial battery charge");
        helper.setBlock(batteryPos.west(), block(helper, "californium_rtg"));

        helper.succeedWhen(() -> {
            long charge = energy.getAmountAsLong();
            helper.assertTrue(charge > 0, "RTG did not charge the adjacent battery");
            var registries = level.registryAccess();
            var saved = battery.saveWithFullMetadata(registries);
            var loaded = BlockEntity.loadStatic(battery.getBlockPos(), battery.getBlockState(), saved, registries);
            if (!(loaded instanceof BatteryBE restored)) {
                throw helper.assertionException(batteryPos, "Battery did not deserialize as a battery");
            }
            helper.assertValueEqual(restored.energyStorage.getAmountAsLong(), charge, "restored battery charge");
        });
    }

    private static void hopperManufactory(GameTestHelper helper) {
        BlockPos chestPos = new BlockPos(1, 0, 1);
        BlockPos machinePos = chestPos.above(2);
        BlockPos inputPos = machinePos.above();
        var downHopper = Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN);
        Item ironDust = BuiltInRegistries.ITEM.getOptional(rl("iron_dust"))
                .orElseThrow(() -> helper.assertionException("Missing nuclearcraft:iron_dust"));

        helper.setBlock(chestPos, Blocks.CHEST);
        helper.setBlock(chestPos.above(), downHopper);
        helper.setBlock(machinePos, block(helper, "manufactory"));
        helper.setBlock(machinePos.west(), block(helper, "californium_rtg"));
        helper.setBlock(inputPos, downHopper);
        var input = helper.getBlockEntity(inputPos, HopperBlockEntity.class);
        input.setItem(0, new ItemStack(Items.IRON_INGOT));

        helper.succeedWhen(() -> {
            helper.assertContainerContainsSingle(chestPos, ironDust);
            helper.assertContainerEmpty(inputPos);
        });
    }

    private static void itemPipeReconnect(GameTestHelper helper) {
        BlockPos pullPos = new BlockPos(1, 0, 1);
        BlockPos pipePos = pullPos.above();
        BlockPos pushPos = pipePos.above();
        Block pipe = block(helper, "pipe");
        Block connector = block(helper, "pipe_connector");
        helper.setBlock(pullPos.east(), Blocks.CHEST);
        helper.setBlock(pushPos.east(), Blocks.CHEST);
        helper.setBlock(pullPos, connector);
        helper.setBlock(pushPos, connector);
        helper.setBlock(pipePos, pipe);

        var source = helper.getBlockEntity(pullPos.east(), ChestBlockEntity.class);
        var destination = helper.getBlockEntity(pushPos.east(), ChestBlockEntity.class);
        ItemStack namedIron = new ItemStack(Items.IRON_INGOT, 12);
        namedIron.set(DataComponents.CUSTOM_NAME, Component.literal("Pipe iron"));
        source.setItem(0, namedIron.copy());
        destination.setItem(0, namedIron.copyWithCount(64));
        for (int slot = 1; slot < destination.getContainerSize(); slot++) {
            destination.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }

        var pull = helper.getBlockEntity(pullPos, PipeConnectorBE.class);
        var push = helper.getBlockEntity(pushPos, PipeConnectorBE.class);
        pull.setCapabilityEnabled(PipeConnectorBE.CAP_ITEM, true);
        push.setCapabilityEnabled(PipeConnectorBE.CAP_ITEM, true);
        pull.setMode(ConnectorMode.PULL);
        push.setMode(ConnectorMode.PUSH);

        helper.startSequence()
                .thenExecuteFor(5, () -> {
                    helper.assertValueEqual(source.getItem(0).getCount(), 12, "source count with full destination");
                    helper.assertValueEqual(destination.getItem(0).getCount(), 64, "full destination count");
                })
                .thenExecute(() -> destination.setItem(0, namedIron.copyWithCount(60)))
                .thenWaitUntil(() -> helper.assertValueEqual(destination.getItem(0).getCount(), 64, "partially accepted transfer"))
                .thenExecute(() -> {
                    helper.assertValueEqual(source.getItem(0).getCount(), 8, "source count after partial transfer");
                    helper.setBlock(pipePos, Blocks.AIR);
                    destination.setItem(0, ItemStack.EMPTY);
                })
                .thenExecuteFor(5, () -> {
                    helper.assertValueEqual(source.getItem(0).getCount(), 8, "source count with disconnected pipe");
                    helper.assertTrue(destination.getItem(0).isEmpty(), "Disconnected pipe delivered items");
                })
                .thenExecute(() -> helper.setBlock(pipePos, pipe))
                .thenWaitUntil(() -> helper.assertValueEqual(destination.getItem(0).getCount(), 8, "delivery after reconnection"))
                .thenExecute(() -> {
                    helper.assertTrue(source.isEmpty(), "Reconnected pipe left items in the source");
                    helper.assertValueEqual(ItemResource.of(destination.getItem(0)), ItemResource.of(namedIron),
                            "delivered item components");
                })
                .thenSucceed();
    }

    private static void fluidPipeTransaction(GameTestHelper helper) {
        BlockPos inletPos = new BlockPos(1, 0, 1);
        BlockPos outletPos = inletPos.above(2);
        BlockPos barrelPos = outletPos.east();
        Block connector = block(helper, "pipe_connector");
        helper.setBlock(barrelPos, block(helper, "basic_barrel"));
        helper.setBlock(inletPos, connector);
        helper.setBlock(outletPos, connector);
        helper.setBlock(inletPos.above(), block(helper, "pipe"));
        var inlet = helper.getBlockEntity(inletPos, PipeConnectorBE.class);
        var outlet = helper.getBlockEntity(outletPos, PipeConnectorBE.class);
        inlet.setCapabilityEnabled(PipeConnectorBE.CAP_FLUID, true);
        outlet.setCapabilityEnabled(PipeConnectorBE.CAP_FLUID, true);
        outlet.setMode(ConnectorMode.PUSH);

        var fluid = helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, inlet.getBlockPos(), Direction.EAST);
        if (fluid == null) {
            throw helper.assertionException(inletPos, "Pipe inlet has no fluid capability");
        }
        var barrel = helper.getBlockEntity(barrelPos, BarrelBE.class);
        var water = FluidResource.of(Fluids.WATER);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertValueEqual(fluid.size(), 1, "connected barrel tanks"))
                .thenExecute(() -> {
                    try (var transaction = Transaction.openRoot()) {
                        helper.assertValueEqual(fluid.insert(water, 1000, transaction), 1000, "water accepted by pipe");
                        helper.assertValueEqual(barrel.fluidTank.getAmountAsLong(0), 1000L, "barrel amount during transaction");
                    }
                    helper.assertValueEqual(barrel.fluidTank.getAmountAsLong(0), 0L, "barrel amount after rollback");
                    helper.assertTrue(barrel.fluidTank.getResource(0).isEmpty(), "Barrel kept fluid after rollback");
                    try (var transaction = Transaction.openRoot()) {
                        helper.assertValueEqual(fluid.insert(water, 1000, transaction), 1000, "water accepted on retry");
                        helper.assertValueEqual(fluid.extract(water, 1000, transaction), 0, "extraction through a push outlet");
                        transaction.commit();
                    }
                    helper.assertValueEqual(barrel.fluidTank.getAmountAsLong(0), 1000L, "barrel amount after commit");
                    helper.assertValueEqual(barrel.fluidTank.getResource(0), water, "barrel fluid after commit");
                })
                .thenSucceed();
    }

    private static void wastelandBiome(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        var wasteland = biomes.get(ModBiomes.WASTELAND)
                .orElseThrow(() -> helper.assertionException("Missing nuclearcraft:wasteland biome"));
        helper.assertTrue(wasteland.is(ModBiomes.WASTELAND_TAG), "Wasteland biome is missing its biome tag");
        helper.succeed();
    }

    private static void linkedContainerPersistence(GameTestHelper helper) {
        BlockPos firstPos = new BlockPos(1, 0, 1);
        BlockPos secondPos = firstPos.east();
        Block container = block(helper, "basic_storage_container");
        helper.setBlock(firstPos, container);
        helper.setBlock(secondPos, container);
        var first = helper.getBlockEntity(firstPos, ContainerBE.class);
        var second = helper.getBlockEntity(secondPos, ContainerBE.class);
        var uuid = first.assignUuidIfAbsent();
        second.setUuid(uuid);

        ItemStack namedStack = new ItemStack(Items.IRON_INGOT, 1025);
        namedStack.set(DataComponents.CUSTOM_NAME, Component.literal("Linked iron"));
        first.getInventory().setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 4096));
        first.getInventory().setStackInSlot(13, namedStack);
        first.getInventory().setStackInSlot(26, new ItemStack(Items.GOLD_INGOT, 65));

        var level = helper.getLevel();
        var inventory = level.getCapability(Capabilities.Item.BLOCK, second.getBlockPos(), Direction.NORTH);
        if (inventory == null) {
            throw helper.assertionException(secondPos, "Linked Container has no item capability");
        }
        var namedIron = ItemResource.of(namedStack);
        helper.assertValueEqual(inventory.getResource(13), namedIron, "linked item components");
        try (var transaction = Transaction.openRoot()) {
            helper.assertValueEqual(inventory.extract(13, namedIron, 25, transaction), 25, "linked extraction");
            helper.assertValueEqual(first.getInventory().getAmountAsLong(13), 1000L, "shared amount during extraction");
        }
        helper.assertValueEqual(first.getInventory().getAmountAsLong(13), 1025L, "shared amount after rollback");

        var registries = level.registryAccess();
        var saved = second.saveWithFullMetadata(registries);
        var loaded = BlockEntity.loadStatic(second.getBlockPos(), second.getBlockState(), saved, registries);
        if (!(loaded instanceof ContainerBE restored)) {
            throw helper.assertionException(secondPos, "Container did not deserialize as a Container");
        }
        helper.assertValueEqual(restored.getUuid(), uuid, "restored Container UUID");

        try {
            Path directory = Files.createTempDirectory("ncn-linked-container-");
            try {
                var type = ContainerInventoryStore.TYPE;
                var liveStore = ContainerInventoryStore.get(level.getServer());
                var snapshot = ContainerInventoryStore.load(liveStore.save(new CompoundTag(), registries), registries);
                try (var disk = new SavedDataStorage(level, directory, DataFixers.getDataFixer(), registries)) {
                    disk.set(type, snapshot);
                }
                for (int cycle = 1; cycle <= 2; cycle++) {
                    try (var disk = new SavedDataStorage(level, directory, DataFixers.getDataFixer(), registries)) {
                        var store = CurrentSavedData.get(disk, directory, type);
                        var items = store.get(restored.getUuid());
                        helper.assertTrue(items != null, "Missing Container inventory after reopen " + cycle);
                        helper.assertValueEqual(items.getAmountAsLong(0), 4096L, "restored cobblestone count");
                        helper.assertValueEqual(items.getAmountAsLong(13), 1025L, "restored named iron count");
                        helper.assertValueEqual(items.getResource(13), namedIron, "restored named iron components");
                        helper.assertValueEqual(items.getAmountAsLong(26), 65L, "restored gold count");
                        helper.assertTrue(items.getResource(1).isEmpty(), "Empty Container slot changed");
                        store.setDirty();
                    }
                }
            } finally {
                FileUtils.deleteDirectory(directory.toFile());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not reopen Container saved data", exception);
        }
        helper.succeed();
    }

    private static Block block(GameTestHelper helper, String name) {
        Identifier id = rl(name);
        return BuiltInRegistries.BLOCK.getOptional(id)
                .orElseThrow(() -> helper.assertionException("Missing block " + id));
    }
}
