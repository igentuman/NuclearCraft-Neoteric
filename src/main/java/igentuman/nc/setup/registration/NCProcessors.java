package igentuman.nc.setup.registration;

import igentuman.nc.block.ProcessorBlock;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.item.ProcessorBlockItem;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.Registration.BLOCKS;
import static igentuman.nc.setup.Registration.ITEMS;

public class NCProcessors {
    public static HashMap<String, RegistryObject<Block>> PROCESSORS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> PROCESSOR_BLOCKS_ITEMS = new HashMap<>();
    public static final Item.Properties PROCESSOR_ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.NC_BLOCKS);
    public static final Block.Properties PROCESSOR_BLOCK_PROPERTIES = Block.Properties.of(Material.HEAVY_METAL).sound(SoundType.METAL).strength(2f).requiresCorrectToolForDrops();
    private static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MODID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);
    public static HashMap<String, RegistryObject<ContainerType<? extends NCProcessorContainer<?>>>> PROCESSORS_CONTAINERS = new HashMap<>();
    public static HashMap<String, RegistryObject<TileEntityType<? extends NCProcessorBE<?>>>> PROCESSORS_BE = new HashMap<>();

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCK_ENTITIES.register(bus);
        CONTAINERS.register(bus);
        registerBlocks();
        registerBlockEntities();
        registerContainers();
    }

    @SuppressWarnings("unchecked")
    private static void registerContainers() {
        for(String name: Processors.registered().keySet()) {
            PROCESSORS_CONTAINERS.put(name, CONTAINERS.register(name,
                    () -> IForgeContainerType.create((windowId, inv, data) -> {
                        NCProcessorContainer<?> o = null;
                        try {
                            o = (NCProcessorContainer<?>) Processors.registered().get(name).getContainerConstructor()
                                    .newInstance(windowId, data.readBlockPos(), inv, inv.player, name);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ignore) {
                        }
                        return o;
                    })));
        }
    }


    @SuppressWarnings("unchecked")
    private static void registerBlockEntities() {
        for(String name: Processors.registered().keySet()) {
            PROCESSORS_BE.put(name, BLOCK_ENTITIES.register(name,
                    () -> TileEntityType.Builder
                            .of(Processors.all().get(name).getBlockEntity(name), PROCESSORS.get(name).get())
                            .build(null)));
        }
    }

    private static void registerBlocks() {
        for(String name: Processors.registered().keySet()) {
            PROCESSORS.put(name, BLOCKS.register(name, () -> new ProcessorBlock(PROCESSOR_BLOCK_PROPERTIES)));
            PROCESSOR_BLOCKS_ITEMS.put(name, fromBlock(PROCESSORS.get(name)));
        }
    }

    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new ProcessorBlockItem(block.get(), PROCESSOR_ITEM_PROPERTIES));
    }
}
