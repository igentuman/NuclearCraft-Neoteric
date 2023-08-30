package igentuman.nc.setup.registration;

import igentuman.nc.block.BarrelBlock;
import igentuman.nc.item.BarrelBlockItem;
import igentuman.nc.setup.storage.BarrelBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCBlocks.NC_BLOCKS;

public class NCStorageBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static HashMap<String, RegistryObject<Block>> STORAGE_BLOCK = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> BLOCK_ITEMS = new HashMap<>();
    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.NC_ITEMS);
    public static final BlockBehaviour.Properties BLOCK_PROPERTIES = BlockBehaviour.Properties.of(Material.METAL).sound(SoundType.METAL).strength(2f).requiresCorrectToolForDrops();
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static HashMap<String, RegistryObject<BlockEntityType<? extends BlockEntity>>> STORAGE_BE = new HashMap<>();

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        registerBlocks();
        registerBlockEntities();
    }

    private static void registerBlockEntities() {
        for(String name: BarrelBlocks.registered().keySet()) {
            STORAGE_BE.put(name, BLOCK_ENTITIES.register(name,
                    () -> BlockEntityType.Builder
                            .of(BarrelBlocks.all().get(name).getBlockEntity(), STORAGE_BLOCK.get(name).get())
                            .build(null)));

        }
    }

    private static void registerBlocks() {
        for(String name: BarrelBlocks.registered().keySet()) {
            STORAGE_BLOCK.put(name, BLOCKS.register(name, () -> new BarrelBlock(BLOCK_PROPERTIES)));
            BLOCK_ITEMS.put(name, fromBarrelBlock(STORAGE_BLOCK.get(name)));
        }
    }

    public static <B extends Block> RegistryObject<Item> fromBarrelBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BarrelBlockItem(block.get(), ITEM_PROPERTIES));
    }

    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), ITEM_PROPERTIES));
    }

}
