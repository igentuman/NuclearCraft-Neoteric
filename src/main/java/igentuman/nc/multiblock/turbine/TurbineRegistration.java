package igentuman.nc.multiblock.turbine;

import igentuman.nc.block.entity.turbine.*;
import igentuman.nc.block.turbine.TurbineBladeBlock;
import igentuman.nc.block.turbine.TurbineBlock;
import igentuman.nc.block.turbine.TurbineControllerBlock;
import igentuman.nc.block.turbine.TurbinePortBlock;
import igentuman.nc.container.FissionControllerContainer;
import igentuman.nc.container.FissionPortContainer;
import igentuman.nc.container.TurbineControllerContainer;
import igentuman.nc.container.TurbinePortContainer;
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
import org.checkerframework.checker.units.qual.C;

import java.util.*;
import java.util.function.Supplier;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.Registration.BLOCKS;
import static igentuman.nc.setup.Registration.BLOCK_ENTITIES;
import static igentuman.nc.setup.registration.NCItems.ITEMS;

public class TurbineRegistration {
    public static final Item.Properties TURBINE_ITEM_PROPS = new Item.Properties().tab(CreativeTabs.TURBINE);

    public static final BlockBehaviour.Properties TURBINE_BLOCKS_PROPERTIES = BlockBehaviour.Properties.of(Material.METAL).strength(4f).requiresCorrectToolForDrops();
    public static final BlockBehaviour.Properties GLASS_BLOCK_PROPERTIES = BlockBehaviour.Properties.of(Material.METAL).strength(3f).requiresCorrectToolForDrops().noOcclusion();
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
            "basic_rotor_blade",
            "sic_sic_cmc_rotor_blade",
            "steel_rotor_blade"
    );

    public static final HashMap<String, CoilDef> coils =  coils();
    private static HashMap<String, Double> efficiency;


    public static HashMap<String, CoilDef> coils() {
        HashMap<String, CoilDef> tmp = new HashMap<>();
        tmp.put("copper", new CoilDef("copper", 110, "turbine_gold_coil"));
        tmp.put("magnesium", new CoilDef("magnesium", 86, "turbine_bearing"));
        tmp.put("silver", new CoilDef("silver", 112, "turbine_magnesium_coil", "turbine_gold_coil"));
        tmp.put("gold", new CoilDef("gold", 104, "turbine_beryllium_coil"));
        tmp.put("beryllium", new CoilDef("beryllium", 90, "turbine_magnesium_coil"));
        tmp.put("aluminium", new CoilDef("aluminium", 98, "turbine_gold_coil|turbine_magnesium_coil|turbine_beryllium_coil|turbine_gold_coil|turbine_copper_coil"));
        return tmp;
    }


    public static final RegistryObject<MenuType<TurbineControllerContainer>> TURBINE_CONTROLLER_CONTAINER = CONTAINERS.register("turbine_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new TurbineControllerContainer(windowId, data.readBlockPos(), inv))
    );
    public static final RegistryObject<MenuType<TurbinePortContainer>> TURBINE_PORT_CONTAINER = CONTAINERS.register("turbine_port",
            () -> IForgeMenuType.create((windowId, inv, data) -> new TurbinePortContainer(windowId, data.readBlockPos(), inv))
    );

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        CONTAINERS.register(bus);
        blocks();
    }

    public static RegistryObject<Block> addBlock(String name, Supplier<? extends Block> block) {
        TURBINE_BLOCKS.put(name, BLOCKS.register(name, block));
        TURBINE_BLOCK_ITEMS.put(name, ITEMS.register(name, () -> new BlockItem(TURBINE_BLOCKS.get(name).get(), TURBINE_ITEM_PROPS)));
        return TURBINE_BLOCKS.get(name);
    }

    public static void blocks()
    {
        for (String block : turbine) {
            String key = "turbine_" + block;

            if(block.contains("controller")) {
                RegistryObject<Block> toAdd = addBlock(key, () -> new TurbineControllerBlock(TURBINE_BLOCKS_PROPERTIES));
                TURBINE_BE.put(key,
                        BLOCK_ENTITIES.register(key,
                                () -> BlockEntityType.Builder.of(TurbineControllerBE::new, toAdd.get())
                                        .build(null)));
                continue;
            }

            if(block.contains("port")) {
                RegistryObject<Block> toAdd = addBlock(key, () -> new TurbinePortBlock(TURBINE_BLOCKS_PROPERTIES));
                TURBINE_BE.put(key,
                        BLOCK_ENTITIES.register(key,
                                () -> BlockEntityType.Builder.of(TurbinePortBE::new, toAdd.get())
                                        .build(null)));
                continue;
            }

            if(block.matches("casing|glass")) {
                if(block.matches("glass")) {
                    addBlock(key, () -> new TurbineBlock(GLASS_BLOCK_PROPERTIES));
                    continue;
                }
                addBlock(key, () -> new TurbineBlock(TURBINE_BLOCKS_PROPERTIES));
                continue;
            }
            if(block.contains("blade")) {
               addBlock(key, () -> new TurbineBladeBlock(TURBINE_BLOCKS_PROPERTIES));
                continue;
            }
            RegistryObject<Block> toAdd = addBlock(key, () -> new TurbineBlock(TURBINE_BLOCKS_PROPERTIES));
            TURBINE_BE.put(key, BLOCK_ENTITIES.register(key, () -> BlockEntityType.Builder.of(TurbineCasingBE::new, toAdd.get()).build(null)));
        }
        TURBINE_BE.put("turbine_blade", BLOCK_ENTITIES.register("turbine_blade", () -> BlockEntityType.Builder.of(TurbineBladeBE::new, getBladeBlocks()).build(null)));
        TURBINE_BE.put("turbine_casing",
                BLOCK_ENTITIES.register("turbine_casing",
                        () -> BlockEntityType.Builder.of(TurbineCasingBE::new,
                                TURBINE_BLOCKS.get("turbine_casing").get(),
                                TURBINE_BLOCKS.get("turbine_glass").get()).build(null)));


        for(String block: coils.keySet()) {
            String key = "turbine_" + block + "_coil";
            TURBINE_BLOCKS.put(key, BLOCKS.register(key, () -> new TurbineBlock(TURBINE_BLOCKS_PROPERTIES)));
            TURBINE_BLOCK_ITEMS.put(key, ITEMS.register(key, () -> new BlockItem(TURBINE_BLOCKS.get(key).get(), TURBINE_ITEM_PROPS)));
        }
        TURBINE_BE.put("turbine_coil", BLOCK_ENTITIES.register("turbine_coil", () -> BlockEntityType.Builder.of(TurbineCoilBE::new, getCoilBlocks()).build(null)));
    }

    public static Block[] getCoilBlocks() {
        Block[] blocks = new Block[coils.size()];
        int i = 0;
        for(String block: coils.keySet()) {
            String key = "turbine_" + block + "_coil";
            blocks[i] = TURBINE_BLOCKS.get(key).get();
            i++;
        }
        return blocks;
    }

    public static Block[] getBladeBlocks() {
        Block[] blocks = new Block[4];
        int i = 0;
        for (String name: TURBINE_BLOCKS.keySet()) {
            if(name.contains("blade")) {
                blocks[i] = TURBINE_BLOCKS.get(name).get();
                i++;
            }
        }
        return blocks;
    }

    public static List<String> initialPlacementRules(String name) {
        return List.of(coils().get(name).rules);
    }


    public static HashMap<String, Double> initialEfficiency()
    {
        if(efficiency == null) {
            efficiency = new HashMap<>();
            for(String name: coils().keySet()) {
                if(name.contains("empty")) continue;
                efficiency.put(name, coils().get(name).efficiency);
            }
        }

        return efficiency;
    }
}
