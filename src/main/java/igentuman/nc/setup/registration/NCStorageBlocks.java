package igentuman.nc.setup.registration;

import igentuman.nc.block.BarrelBlock;
import igentuman.nc.block.ContainerBlock;
import igentuman.nc.container.StorageContainerContainer;
import igentuman.nc.item.BarrelBlockItem;
import igentuman.nc.item.ContainerBlockItem;
import igentuman.nc.content.storage.BarrelBlocks;
import igentuman.nc.content.storage.ContainerBlocks;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.Registration.BLOCKS;
import static igentuman.nc.setup.Registration.ITEMS;

public class NCStorageBlocks {
    public static HashMap<String, RegistryObject<Block>> STORAGE_BLOCK = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> BLOCK_ITEMS = new HashMap<>();
    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties();
    public static final BlockBehaviour.Properties BLOCK_PROPERTIES = BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(2f).requiresCorrectToolForDrops().noOcclusion();
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static HashMap<String, RegistryObject<BlockEntityType<? extends BlockEntity>>> STORAGE_BE = new HashMap<>();
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final RegistryObject<MenuType<StorageContainerContainer<?>>> STORAGE_CONTAINER = CONTAINERS.register("storage_container",
            () -> IForgeMenuType.create((windowId, inv, data) -> new StorageContainerContainer<>(windowId, data.readBlockPos(), inv)));
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
            STORAGE_BE.put(name, BLOCK_ENTITIES.register(name,
                    () -> BlockEntityType.Builder
                            .of(BarrelBlocks.all().get(name).getBlockEntity(), STORAGE_BLOCK.get(name).get())
                            .build(null)));

        }
        for(String name: ContainerBlocks.registered().keySet()) {
            STORAGE_BE.put(name, BLOCK_ENTITIES.register(name,
                    () -> BlockEntityType.Builder
                            .of(ContainerBlocks.all().get(name).getBlockEntity(), STORAGE_BLOCK.get(name).get())
                            .build(null)));

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
