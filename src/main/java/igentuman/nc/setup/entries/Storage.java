package igentuman.nc.setup.entries;

import igentuman.nc.block.storage.AbstractStorageBlock;
import igentuman.nc.block.storage.BarrelBlock;
import igentuman.nc.block.storage.BatteryBlock;
import igentuman.nc.block.storage.ContainerBlock;
import igentuman.nc.block_entity.storage.AbstractStorageBE;
import igentuman.nc.block_entity.storage.BarrelBE;
import igentuman.nc.block_entity.storage.BatteryBE;
import igentuman.nc.block_entity.storage.ContainerBE;
import igentuman.nc.container.StorageContainerItemMenu;
import igentuman.nc.container.StorageContainerMenu;
import igentuman.nc.content.storage.StorageDefs;
import igentuman.nc.handler.storage.UuidBackedItemHandler;
import igentuman.nc.item.BarrelBlockItem;
import igentuman.nc.item.BatteryBlockItem;
import igentuman.nc.item.ContainerBlockItem;
import igentuman.nc.registration.ModEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static igentuman.nc.setup.ModEntries.ENTRIES;
import static igentuman.nc.setup.Registers.*;

/** Registers the storage block families (barrels, containers, batteries), their menu, and capabilities. */
public class Storage {

    public static final List<ModEntry> STORAGE_ENTRIES = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<MenuType<?>, MenuType<StorageContainerMenu>> STORAGE_MENU =
            (DeferredHolder<MenuType<?>, MenuType<StorageContainerMenu>>) (DeferredHolder<?, ?>)
                    CONTAINERS.register("storage_container", () -> IMenuTypeExtension.create(
                            (IContainerFactory<StorageContainerMenu>) StorageContainerMenu::new));

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<MenuType<?>, MenuType<StorageContainerItemMenu>> STORAGE_ITEM_MENU =
            (DeferredHolder<MenuType<?>, MenuType<StorageContainerItemMenu>>) (DeferredHolder<?, ?>)
                    CONTAINERS.register("storage_container_item", () -> IMenuTypeExtension.create(
                            (IContainerFactory<StorageContainerItemMenu>) StorageContainerItemMenu::new));

    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(2f)
                .requiresCorrectToolForDrops()
                .noOcclusion();
    }

    @FunctionalInterface
    public interface StorageBEFactory {
        AbstractStorageBE create(BlockEntityType<?> type, BlockPos pos, BlockState state, String name);
    }

    public static void storage() {
        for (String name : StorageDefs.BARRELS.keySet()) {
            register(name, () -> new BarrelBlock(props(), name), BarrelBE::new, BarrelBlockItem::new);
        }
        for (String name : StorageDefs.CONTAINERS.keySet()) {
            register(name, () -> new ContainerBlock(props(), name), ContainerBE::new, ContainerBlockItem::new);
        }
        for (String name : StorageDefs.BATTERIES.keySet()) {
            register(name, () -> new BatteryBlock(props(), name), BatteryBE::new, BatteryBlockItem::new);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void register(String name, Supplier<Block> blockSupplier, StorageBEFactory beFactory,
                                 BiFunction<Block, Item.Properties, ? extends BlockItem> itemFactory) {
        DeferredBlock<Block> block = BLOCKS.register(name, blockSupplier);
        DeferredItem<Item> item = ITEMS.register(name, () -> itemFactory.apply(block.get(), new Item.Properties()));

        final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>>[] beHolder = new DeferredHolder[1];
        DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<?>> beReg = BLOCK_ENTITIES.register(name,
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> beFactory.create(beHolder[0].get(), pos, state, name),
                        block.get()).build(null));
        beHolder[0] = (DeferredHolder<BlockEntityType<?>, BlockEntityType<?>>) (DeferredHolder) beReg;

        ModEntry entry = new ModEntry(name, block, item, null, beHolder[0], false, null, null, null,
                null, null, null, null, 0, null, null, Set.of(), Set.of(), false);
        ENTRIES.put(name, entry);
        STORAGE_ENTRIES.add(entry);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (ModEntry entry : STORAGE_ENTRIES) {
            BlockEntityType<?> type = entry.blockEntity().get();
            Block block = entry.block().get();
            if (block instanceof BarrelBlock) {
                event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type,
                        (be, side) -> be instanceof BarrelBE barrel ? barrel.getFluidHandler(side) : null);
            } else if (block instanceof ContainerBlock) {
                event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type,
                        (be, side) -> be instanceof ContainerBE container ? container.getItemHandler(side) : null);
                int size = StorageDefs.containerSize(entry.name());
                event.registerItem(Capabilities.ItemHandler.ITEM,
                        (stack, ctx) -> new UuidBackedItemHandler(size, () -> ContainerBlockItem.readUuid(stack)),
                        entry.item().get());
            } else if (block instanceof BatteryBlock) {
                event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type,
                        (be, side) -> be instanceof BatteryBE battery ? battery.getEnergyHandler(side) : null);
            }
        }
    }
}
