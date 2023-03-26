package igentuman.nc.setup.registration;

import igentuman.nc.block.NCBaseBlock;
import igentuman.nc.block.NCProcessorBlock;
import igentuman.nc.block.PortalBlock;
import igentuman.nc.block.entity.ManufactoryBE;
import igentuman.nc.block.entity.NCProcessor;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.setup.ModSetup;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.materials.Blocks;
import igentuman.nc.setup.registration.materials.Ores;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static igentuman.nc.NuclearCraft.MODID;

public class NCProcessors {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static HashMap<String, RegistryObject<Block>> PROCESSORS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> PROCESSOR_BLOCKS_ITEMS = new HashMap<>();
    public static final Item.Properties PROCESSOR_ITEM_PROPERTIES = new Item.Properties().tab(ModSetup.ITEM_GROUP);
    public static final BlockBehaviour.Properties PROCESSOR_BLOCK_PROPERTIES = BlockBehaviour.Properties.of(Material.METAL).sound(SoundType.METAL).strength(2f).requiresCorrectToolForDrops();
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);


    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        CONTAINERS.register(bus);
        registerBlocks();
        registerBlockEntities();
        registerContainers();
    }

    private static void registerContainers() {
        for(String name: Processors.registered().keySet()) {
            PROCESSORS_CONTAINERS.put(name, CONTAINERS.register(name,
                    () -> IForgeMenuType.create((windowId, inv, data) -> new NCProcessorContainer(windowId, data.readBlockPos(), inv, inv.player, name))));
        }
    }

    public static HashMap<String, RegistryObject<MenuType<? extends NCProcessorContainer>>> PROCESSORS_CONTAINERS = new HashMap<>();

    public static HashMap<String, RegistryObject<BlockEntityType<? extends NCProcessor>>> PROCESSORS_BE = new HashMap<>();


    private static void registerBlockEntities() {
        for(String name: Processors.registered().keySet()) {
            PROCESSORS_BE.put(name, BLOCK_ENTITIES.register(name,
                    () -> BlockEntityType.Builder
                            .of(Processors.all().get(name).getBlockEntity(), PROCESSORS.get(name).get())
                            .build(null)));
        }
    }

    private static void registerBlocks() {
        for(String name: Processors.registered().keySet()) {
            PROCESSORS.put(name, BLOCKS.register(name, () -> new NCProcessorBlock(PROCESSOR_BLOCK_PROPERTIES)));
            PROCESSOR_BLOCKS_ITEMS.put(name, fromBlock(PROCESSORS.get(name)));
        }
    }

    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), PROCESSOR_ITEM_PROPERTIES));
    }
}
