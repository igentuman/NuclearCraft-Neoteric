package igentuman.nc.multiblock.turbine;

import igentuman.nc.block.turbine.TurbineBlock;
import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.block.turbine.TurbineControllerBlock;
import igentuman.nc.container.FissionControllerContainer;
import igentuman.nc.container.FissionPortContainer;
import igentuman.nc.setup.registration.CreativeTabs;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.Registration.BLOCKS;
import static igentuman.nc.setup.Registration.BLOCK_ENTITIES;
import static igentuman.nc.setup.registration.NCItems.ITEMS;

public class TurbineRegistration {
    public static final Item.Properties TURBINE_ITEM_PROPS = new Item.Properties().tab(CreativeTabs.TURBINE);

    public static final BlockBehaviour.Properties TURBINE_BLOCKS_PROPERTIES = BlockBehaviour.Properties.of(Material.METAL).strength(4f).requiresCorrectToolForDrops();
    public static HashMap<String, RegistryObject<Block>> TURBINE_BLOCKS = new HashMap<>();
    public static HashMap<String, RegistryObject<BlockEntityType<? extends BlockEntity>>> TURBINE_BE = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> TURBINE_BLOCK_ITEMS = new HashMap<>();
    public static TagKey<Block> CASING_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MODID, "turbine_casing"));
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static TagKey<Block> INNER_TURBINE_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MODID, "turbine_inner"));

    public static final List<String> turbine =  Arrays.asList(
            "casing",
            "bearing",
            "controller",
            "rotor_shaft",
            "rotor_stator",
            "port",
            "glass",
            "extreme_rotor_blade",
            "steel_rotor_blade"
    );

    public static final HashMap<String, Object> coils =  initCoils();

    private static HashMap<String, Object> initCoils() {
        HashMap<String, Object> tmp = new HashMap<>();
        tmp.put("copper", 1);
        tmp.put("magnesium", 2);
        tmp.put("silver", 3);
        tmp.put("gold", 4);
        tmp.put("beryllium", 5);
        tmp.put("aluminium", 6);
        return tmp;
    }


    public static final RegistryObject<MenuType<FissionControllerContainer>> TURBINE_CONTROLLER_CONTAINER = CONTAINERS.register("turbine_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FissionControllerContainer(windowId, data.readBlockPos(), inv))
    );
    public static final RegistryObject<MenuType<FissionPortContainer>> TURBINE_PORT_CONTAINER = CONTAINERS.register("turbine_port",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FissionPortContainer(windowId, data.readBlockPos(), inv))
    );

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        CONTAINERS.register(bus);
        blocks();
    }

    public static void blocks()
    {
        for (String block : turbine) {
            if(block.equals("controller")) {
                TURBINE_BLOCKS.put("turbine_" + block, BLOCKS.register("turbine_" + block, () -> new TurbineControllerBlock(TURBINE_BLOCKS_PROPERTIES)));
            } else {
                TURBINE_BLOCKS.put("turbine_" + block, BLOCKS.register("turbine_" + block, () -> new TurbineBlock(TURBINE_BLOCKS_PROPERTIES)));
            }
            TURBINE_BE.put("turbine_"+block, BLOCK_ENTITIES.register("turbine_"+block, () -> BlockEntityType.Builder.of(TurbineControllerBE::new, TURBINE_BLOCKS.get("turbine_"+block).get()).build(null)));
            TURBINE_BLOCK_ITEMS.put("turbine_"+block, ITEMS.register("turbine_"+block, () -> new BlockItem(TURBINE_BLOCKS.get("turbine_"+block).get(), TURBINE_ITEM_PROPS)));
        }

        for(String block: coils.keySet()) {
            String key = "turbine_" + block + "_coil";
            TURBINE_BLOCKS.put(key, BLOCKS.register(key, () -> new TurbineBlock(TURBINE_BLOCKS_PROPERTIES)));
            TURBINE_BE.put(key, BLOCK_ENTITIES.register(key, () -> BlockEntityType.Builder.of(TurbineControllerBE::new, TURBINE_BLOCKS.get(key).get()).build(null)));
            TURBINE_BLOCK_ITEMS.put(key, ITEMS.register(key, () -> new BlockItem(TURBINE_BLOCKS.get(key).get(), TURBINE_ITEM_PROPS)));
        }
    }
}
