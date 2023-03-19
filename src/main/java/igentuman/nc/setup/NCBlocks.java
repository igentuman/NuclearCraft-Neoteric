package igentuman.nc.setup;

import igentuman.nc.world.ore.Ores;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import igentuman.nc.block.*;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;

public class NCBlocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final BlockBehaviour.Properties ORE_BLOCK_PROPERTIES = BlockBehaviour.Properties.of(Material.STONE).strength(2f).requiresCorrectToolForDrops();
    public static HashMap<String, RegistryObject<Block>> ORE_BLOCKS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> ORE_BLOCK_ITEMS = new HashMap<>();
    public static final Item.Properties ORE_ITEM_PROPERTIES = new Item.Properties().tab(ModSetup.ITEM_GROUP);

    public static final RegistryObject<Block> PORTAL_BLOCK = BLOCKS.register("portal", PortalBlock::new);
    public static final RegistryObject<Item> PORTAL_ITEM = fromBlock(PORTAL_BLOCK);
    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        registerOres();
    }

    private static void registerOres() {
        for(String name: Ores.registered().keySet()) {
            ORE_BLOCKS.put(name, BLOCKS.register(name+"_ore", () -> new Block(ORE_BLOCK_PROPERTIES)));
            ORE_BLOCK_ITEMS.put(name, fromBlock(ORE_BLOCKS.get(name)));
        }
    }

    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), ORE_ITEM_PROPERTIES));
    }

}
