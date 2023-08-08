package igentuman.nc.setup.registration;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.setup.ModSetup;
import igentuman.nc.content.materials.Blocks;
import igentuman.nc.content.materials.Ores;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import igentuman.nc.block.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;

public class NCBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final BlockBehaviour.Properties ORE_BLOCK_PROPERTIES = BlockBehaviour.Properties.of(Material.STONE).strength(2f).requiresCorrectToolForDrops();
    public static final BlockBehaviour.Properties NC_BLOCKS_PROPERTIES = BlockBehaviour.Properties.of(Material.METAL).strength(2f).requiresCorrectToolForDrops();
    public static final BlockBehaviour.Properties ORE_DEEPSLATE_BLOCK_PROPERTIES = BlockBehaviour.Properties.of(Material.STONE).strength(4f).requiresCorrectToolForDrops();
    public static HashMap<String, RegistryObject<Block>> ORE_BLOCKS = new HashMap<>();
    public static HashMap<String, RegistryObject<Block>> NC_BLOCKS = new HashMap<>();
    public static HashMap<String, RegistryObject<Block>> MULTI_BLOCKS = new HashMap<>();
    public static HashMap<String, RegistryObject<Block>> NC_MATERIAL_BLOCKS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> ORE_BLOCK_ITEMS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> NC_BLOCKS_ITEMS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> MULTIBLOCK_ITEMS = new HashMap<>();
    public static final Item.Properties ORE_ITEM_PROPERTIES = new Item.Properties().tab(ModSetup.ITEM_GROUP);
    public static final Item.Properties MULTIBLOCK_ITEM_PROPERTIES = new Item.Properties().tab(ModSetup.ITEM_GROUP);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static HashMap<String, RegistryObject<BlockEntityType<? extends BlockEntity>>> MULTIBLOCK_BE = new HashMap<>();

    public static final RegistryObject<Block> PORTAL_BLOCK = BLOCKS.register("portal", PortalBlock::new);

    public static final RegistryObject<Block> MUSHROOM_BLOCK = BLOCKS.register("glowing_mushroom", () -> new Block(BlockBehaviour.Properties.of(Material.PLANT).noCollission().lightLevel($ -> 10)));
    public static final RegistryObject<Item> MUSHROOM_ITEM = fromBlock(MUSHROOM_BLOCK);
    public static final RegistryObject<Item> PORTAL_ITEM = fromBlock(PORTAL_BLOCK);

    public static HashMap<String, TagKey<Block>> ORE_TAGS = new HashMap<>();
    public static HashMap<String, TagKey<Item>> ORE_ITEM_TAGS = new HashMap<>();
    public static HashMap<String, TagKey<Item>> BLOCK_ITEM_TAGS = new HashMap<>();
    public static HashMap<String, TagKey<Block>> BLOCK_TAGS = new HashMap<>();

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        registerOres();
        registerBlocks();
        multiblocks();
    }

    private static void multiblocks() {



    }


    private static void registerOres() {
        for(String name: Ores.registered().keySet()) {
            ORE_TAGS.put(name, TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("forge", "ores/"+name)));
            ORE_ITEM_TAGS.put(name, TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "ores/"+name)));
            if(Materials.ores().get(name).normal_ore) {
                ORE_BLOCKS.put(name, BLOCKS.register(name + "_ore", () -> new Block(ORE_BLOCK_PROPERTIES)));
                ORE_BLOCK_ITEMS.put(name, fromBlock(ORE_BLOCKS.get(name)));
                ALL_NC_ITEMS.put("ore_"+name, ORE_BLOCK_ITEMS.get(name));
            }
            if(Materials.ores().get(name).deepslate_ore) {
                ORE_BLOCKS.put(name+"_deepslate", BLOCKS.register(name + "_deepslate_ore", () -> new Block(ORE_DEEPSLATE_BLOCK_PROPERTIES)));
                ORE_BLOCK_ITEMS.put(name+"_deepslate", fromBlock(ORE_BLOCKS.get(name+"_deepslate")));
                ALL_NC_ITEMS.put("ore_"+name+"_deepslate", ORE_BLOCK_ITEMS.get(name+"_deepslate"));
            }
            if(Materials.ores().get(name).nether_ore) {
                ORE_BLOCKS.put(name+"_nether", BLOCKS.register(name + "_nether_ore", () -> new Block(ORE_BLOCK_PROPERTIES)));
                ORE_BLOCK_ITEMS.put(name+"_nether", fromBlock(ORE_BLOCKS.get(name+"_nether")));
                ALL_NC_ITEMS.put("ore_"+name+"_nether", ORE_BLOCK_ITEMS.get(name+"_nether"));
            }
            if(Materials.ores().get(name).end_ore) {
                ORE_BLOCKS.put(name+"_end", BLOCKS.register(name + "_end_ore", () -> new Block(ORE_BLOCK_PROPERTIES)));
                ORE_BLOCK_ITEMS.put(name+"_end", fromBlock(ORE_BLOCKS.get(name+"_end")));
                ALL_NC_ITEMS.put("ore_"+name+"_end", ORE_BLOCK_ITEMS.get(name+"_end"));
            }
        }
    }

    private static void registerBlocks() {
        for(String name: Blocks.registered().keySet()) {
            BLOCK_TAGS.put(name, TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("forge","storage_blocks/"+name)));
            BLOCK_ITEM_TAGS.put(name, TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "storage_blocks/"+name)));
            NC_BLOCKS.put(name, BLOCKS.register(name + "_block", () -> new Block(NC_BLOCKS_PROPERTIES)));
            NC_BLOCKS_ITEMS.put(name, fromBlock(NC_BLOCKS.get(name)));
            ALL_NC_ITEMS.put(name+"_block", NC_BLOCKS_ITEMS.get(name));
        }

        ALL_NC_ITEMS.put("glowing_mushroom", NC_BLOCKS_ITEMS.get("glowing_mushroom"));
    }

    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), ORE_ITEM_PROPERTIES));
    }

    public static <B extends Block> RegistryObject<Item> fromMultiblock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), MULTIBLOCK_ITEM_PROPERTIES));
    }


    public static final class BlockEntry<T extends Block> implements Supplier<T>, ItemLike
    {
        public static final Collection<BlockEntry<?>> ALL_ENTRIES = new ArrayList<>();

        private final RegistryObject<T> regObject;
        private final Supplier<BlockBehaviour.Properties> properties;

        public static BlockEntry<NCBaseBlock> simple(String name, Supplier<BlockBehaviour.Properties> properties, Consumer<NCBaseBlock> extra)
        {
            return new BlockEntry<>(name, properties, p -> Util.make(new NCBaseBlock(p), extra));
        }

        public static BlockEntry<NCBaseBlock> simple(String name, Supplier<BlockBehaviour.Properties> properties)
        {
            return simple(name, properties, $ -> {
            });
        }

        public static BlockEntry<FenceBlock> fence(String name, Supplier<BlockBehaviour.Properties> props)
        {
            return new BlockEntry<>(name, props, FenceBlock::new);
        }


        public BlockEntry(String name, Supplier<BlockBehaviour.Properties> properties, Function<BlockBehaviour.Properties, T> make)
        {
            this.properties = properties;
            this.regObject = BLOCKS.register(name, () -> make.apply(properties.get()));
            ALL_ENTRIES.add(this);
        }

        public BlockEntry(T existing)
        {
            this.properties = () -> BlockBehaviour.Properties.copy(existing);
            this.regObject = RegistryObject.create(Registry.BLOCK.getKey(existing), ForgeRegistries.BLOCKS);
        }

        @SuppressWarnings("unchecked")
        public BlockEntry(BlockEntry<? extends T> toCopy)
        {
            this.properties = toCopy.properties;
            this.regObject = (RegistryObject<T>)toCopy.regObject;
        }

        @Override
        public T get()
        {
            return regObject.get();
        }

        public BlockState defaultBlockState()
        {
            return get().defaultBlockState();
        }

        public ResourceLocation getId()
        {
            return regObject.getId();
        }

        public BlockBehaviour.Properties getProperties()
        {
            return properties.get();
        }

        @Nonnull
        @Override
        public Item asItem()
        {
            return get().asItem();
        }
    }

}
