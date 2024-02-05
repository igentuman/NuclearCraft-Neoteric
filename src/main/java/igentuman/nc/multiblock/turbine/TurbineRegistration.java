package igentuman.nc.multiblock.turbine;

import igentuman.nc.block.entity.turbine.*;
import igentuman.nc.block.turbine.*;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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
    public static HashMap<String, RegistryObject<BlockEntityType<? extends TurbineBE>>> TURBINE_BE = new HashMap<>();
    public static HashMap<String, RegistryObject<BlockItem>> TURBINE_BLOCK_ITEMS = new HashMap<>();
    public static TagKey<Block> CASING_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MODID, "turbine_casing"));
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);

    public static TagKey<Block> INNER_TURBINE_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MODID, "turbine_inner"));

    public static final HashMap<String, BladeDef> blades = blades();

    public static HashMap<String, BladeDef> blades() {
        if(blades != null) return blades;
        HashMap<String, BladeDef> tmp = new HashMap<>();
        tmp.put("basic_rotor_blade", new BladeDef("basic", 95, 120));
        tmp.put("steel_rotor_blade", new BladeDef("steel", 100, 140));
        tmp.put("extreme_rotor_blade", new BladeDef("extreme", 110, 160));
        tmp.put("sic_sic_cmc_rotor_blade", new BladeDef("sic_sic_cmc", 125, 180));
        return tmp;
    }

    public static final HashMap<String, CoilDef> coils = coils();
    private static HashMap<String, Double> efficiency;


    public static HashMap<String, CoilDef> coils() {
        if(coils != null) return coils;
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
        RegistryObject<Block> controller = addBlock("turbine_controller", () -> new TurbineControllerBlock(TURBINE_BLOCKS_PROPERTIES));
        TURBINE_BE.put("turbine_controller",
                BLOCK_ENTITIES.register("turbine_controller",
                        () -> BlockEntityType.Builder.of(TurbineControllerBE::new, controller.get())
                                .build(null)));

        RegistryObject<Block> port = addBlock("turbine_port", () -> new TurbinePortBlock(TURBINE_BLOCKS_PROPERTIES));
        TURBINE_BE.put("turbine_port",
                BLOCK_ENTITIES.register("turbine_port",
                        () -> BlockEntityType.Builder.of(TurbinePortBE::new, port.get())
                                .build(null)));

        RegistryObject<Block> rotor = addBlock("turbine_rotor_shaft", () -> new TurbineRotorBlock(GLASS_BLOCK_PROPERTIES));
        TURBINE_BE.put("turbine_rotor_shaft",
                BLOCK_ENTITIES.register("turbine_rotor_shaft",
                        () -> BlockEntityType.Builder.of(TurbineRotorBE::new, rotor.get())
                                .build(null)));

        RegistryObject<Block> bearing = addBlock("turbine_bearing", () -> new TurbineBearingBlock(TURBINE_BLOCKS_PROPERTIES));
        TURBINE_BE.put("turbine_bearing",
                BLOCK_ENTITIES.register("turbine_bearing",
                        () -> BlockEntityType.Builder.of(TurbineBearingBE::new, bearing.get())
                                .build(null)));

        addBlock("turbine_glass", () -> new TurbineBlock(GLASS_BLOCK_PROPERTIES));
        addBlock("turbine_casing", () -> new TurbineBlock(TURBINE_BLOCKS_PROPERTIES));

        TURBINE_BE.put("turbine_casing",
                BLOCK_ENTITIES.register("turbine_casing",
                        () -> BlockEntityType.Builder.of(TurbineCasingBE::new,
                                TURBINE_BLOCKS.get("turbine_casing").get(),
                                TURBINE_BLOCKS.get("turbine_glass").get()).build(null)));

        for (String block : blades().keySet()) {
            String key = "turbine_" + block;
            addBlock(key, () -> new TurbineBladeBlock(TURBINE_BLOCKS_PROPERTIES));
        }

        TURBINE_BE.put("turbine_blade", BLOCK_ENTITIES.register("turbine_blade", () -> BlockEntityType.Builder.of(TurbineBladeBE::new, getBladeBlocks()).build(null)));

        for(String block: coils().keySet()) {
            String key = "turbine_" + block + "_coil";
            addBlock(key, () -> new TurbineCoilBlock(TURBINE_BLOCKS_PROPERTIES));
        }
        TURBINE_BE.put("turbine_coil", BLOCK_ENTITIES.register("turbine_coil", () -> BlockEntityType.Builder.of(TurbineCoilBE::new, getCoilBlocks()).build(null)));
    }

    public static Block[] getCoilBlocks() {
        Block[] blocks = new Block[coils().size()];
        int i = 0;
        for(String block: coils().keySet()) {
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
