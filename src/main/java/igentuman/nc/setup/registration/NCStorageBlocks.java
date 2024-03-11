package igentuman.nc.setup.registration;

import igentuman.nc.block.BarrelBlock;
import igentuman.nc.block.ContainerBlock;
import igentuman.nc.container.StorageContainerContainer;
import igentuman.nc.item.BarrelBlockItem;
import igentuman.nc.item.ContainerBlockItem;
import igentuman.nc.content.storage.BarrelBlocks;
import igentuman.nc.content.storage.ContainerBlocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.Registration.BLOCKS;
import static igentuman.nc.setup.Registration.ITEMS;

public class NCStorageBlocks {
    public static HashMap<String, RegistryObject<Block>> STORAGE_BLOCK = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> BLOCK_ITEMS = new HashMap<>();
    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.NC_ITEMS);
    public static final Block.Properties BLOCK_PROPERTIES = Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(2f).requiresCorrectToolForDrops().noOcclusion();
    private static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MODID);
    public static HashMap<String, RegistryObject<TileEntityType<? extends TileEntity>>> STORAGE_BE = new HashMap<>();
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);
    public static final RegistryObject<ContainerType<StorageContainerContainer>> STORAGE_CONTAINER = CONTAINERS.register("storage_container",
            () -> IForgeContainerType.create((windowId, inv, data) -> new StorageContainerContainer(windowId, data.readBlockPos(), inv)));
    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCK_ENTITIES.register(bus);
        CONTAINERS.register(bus);
        registerBlocks();
        registerBlockEntities();
        registerContainers();
    }

    private static void registerContainers() {

    }

    private static void registerBlockEntities() {
        for(String name: BarrelBlocks.registered().keySet()) {
/*            STORAGE_BE.put(name, BLOCK_ENTITIES.register(name,
                    () -> TileEntityType.Builder
                            .of(BarrelBlocks.all().get(name).getBlockEntity(), STORAGE_BLOCK.get(name).get())
                            .build(null)));*/

        }
        for(String name: ContainerBlocks.registered().keySet()) {
/*            STORAGE_BE.put(name, BLOCK_ENTITIES.register(name,
                    () -> TileEntityType.Builder
                            .of(ContainerBlocks.all().get(name).getBlockEntity(), STORAGE_BLOCK.get(name).get())
                            .build(null)));*/

        }
    }

    private static void registerBlocks() {
        for(String name: BarrelBlocks.registered().keySet()) {
            STORAGE_BLOCK.put(name, BLOCKS.register(name, () -> new BarrelBlock(BLOCK_PROPERTIES)));
            BLOCK_ITEMS.put(name, fromBarrelBlock(STORAGE_BLOCK.get(name)));
        }
        for(String name: ContainerBlocks.registered().keySet()) {
            STORAGE_BLOCK.put(name, BLOCKS.register(name, () -> new ContainerBlock(BLOCK_PROPERTIES)));
            BLOCK_ITEMS.put(name, fromContainerBlock(STORAGE_BLOCK.get(name)));
        }
    }

    public static <B extends Block> RegistryObject<Item> fromBarrelBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BarrelBlockItem(block.get(), ITEM_PROPERTIES));
    }

    public static <B extends Block> RegistryObject<Item> fromContainerBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new ContainerBlockItem(block.get(), ITEM_PROPERTIES));
    }

    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), ITEM_PROPERTIES));
    }

}
