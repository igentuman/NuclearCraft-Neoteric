package igentuman.nc.setup.registration;

import igentuman.nc.block.BatteryBlock;
import igentuman.nc.block.RTGBlock;
import igentuman.nc.block.SolarPanelBlock;
import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.item.BatteryBlockItem;
import igentuman.nc.content.energy.BatteryBlocks;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.content.energy.SolarPanels;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.function.Supplier;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.Registration.BLOCKS;
import static igentuman.nc.setup.Registration.ITEMS;

public class NCEnergyBlocks {
    public static HashMap<String, RegistryObject<Block>> ENERGY_BLOCKS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> BLOCK_ITEMS = new HashMap<>();
    public static final Item.Properties ENERGY_ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.NC_ITEMS);
    public static final Block.Properties ENERGY_BLOCK_PROPERTIES = Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(2f).requiresCorrectToolForDrops();
    private static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MODID);
    public static HashMap<String, RegistryObject<TileEntityType<? extends NCEnergy>>> ENERGY_BE = new HashMap<>();

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCK_ENTITIES.register(bus);
        registerBlocks();
        registerBlockEntities();
    }

    private static void registerBlockEntities() {
        for(String name: SolarPanels.registered().keySet()) {
            String key = "solar_panel/"+name;
            ENERGY_BE.put(key, BLOCK_ENTITIES.register(key,
                    () -> TileEntityType.Builder
                            .of(SolarPanels.all().get(name).getBlockEntity(name), ENERGY_BLOCKS.get(key).get())
                            .build(null)));
        }
        for(String name: BatteryBlocks.registered().keySet()) {
            ENERGY_BE.put(name, BLOCK_ENTITIES.register(name,
                    () -> TileEntityType.Builder
                            .of(BatteryBlocks.all().get(name).getBlockEntity(name), ENERGY_BLOCKS.get(name).get())
                            .build(null)));

        }
        for(String name: RTGs.registered().keySet()) {
            ENERGY_BE.put(name, BLOCK_ENTITIES.register(name,
                    () -> TileEntityType.Builder
                            .of(RTGs.all().get(name).getBlockEntity(name), ENERGY_BLOCKS.get(name).get())
                            .build(null)));

        }
    }

    private static void registerBlocks() {
        for(String name: SolarPanels.registered().keySet()) {
            String key = "solar_panel/"+name;
            ENERGY_BLOCKS.put(key, BLOCKS.register(key.replace("/","_"), () -> new SolarPanelBlock(ENERGY_BLOCK_PROPERTIES)));
            BLOCK_ITEMS.put(key, fromBlock(ENERGY_BLOCKS.get(key)));
        }

        for(String name: BatteryBlocks.registered().keySet()) {
            ENERGY_BLOCKS.put(name, BLOCKS.register(name, () -> new BatteryBlock(ENERGY_BLOCK_PROPERTIES)));
            BLOCK_ITEMS.put(name, fromBatteryBlock(ENERGY_BLOCKS.get(name)));
        }

        for(String name: RTGs.registered().keySet()) {
            ENERGY_BLOCKS.put(name, BLOCKS.register(name, () -> new RTGBlock(ENERGY_BLOCK_PROPERTIES)));
            BLOCK_ITEMS.put(name, fromBlock(ENERGY_BLOCKS.get(name)));
        }
    }

    public static <B extends Block> RegistryObject<Item> fromBatteryBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BatteryBlockItem(block.get(), ENERGY_ITEM_PROPERTIES));
    }

    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), ENERGY_ITEM_PROPERTIES));
    }

}
