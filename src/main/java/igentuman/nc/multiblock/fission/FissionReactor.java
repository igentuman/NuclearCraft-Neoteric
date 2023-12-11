package igentuman.nc.multiblock.fission;

import igentuman.nc.block.entity.fission.*;
import igentuman.nc.block.fission.*;
import igentuman.nc.container.FissionControllerContainer;
import igentuman.nc.container.FissionPortContainer;
import igentuman.nc.setup.registration.CreativeTabs;
import igentuman.nc.setup.registration.NCBlocks;
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.Registration.BLOCKS;
import static igentuman.nc.setup.Registration.ITEMS;
import static igentuman.nc.multiblock.fission.FissionBlocks.REACTOR_BLOCKS_PROPERTIES;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;

public class FissionReactor {
    public static final Item.Properties FISSION_ITEM_PROPS = new Item.Properties();
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static HashMap<String, RegistryObject<Block>> FISSION_BLOCKS = new HashMap<>();
    public static HashMap<String, RegistryObject<BlockEntityType<? extends BlockEntity>>> FISSION_BE = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> FISSION_BLOCK_ITEMS = new HashMap<>();


    public static final RegistryObject<MenuType<FissionControllerContainer>> FISSION_CONTROLLER_CONTAINER = CONTAINERS.register("fission_reactor_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FissionControllerContainer(windowId, data.readBlockPos(), inv))
            );
    public static final RegistryObject<MenuType<FissionPortContainer>> FISSION_PORT_CONTAINER = CONTAINERS.register("fission_reactor_port",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FissionPortContainer(windowId, data.readBlockPos(), inv))
            );



    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCK_ENTITIES.register(bus);
        CONTAINERS.register(bus);
        blocks();
    }
    public static List<Block> moderators = new ArrayList<>();
    public static List<Block> moderators() {
        if(moderators.isEmpty()) {
            moderators.add(NCBlocks.NC_BLOCKS.get("graphite").get());
            moderators.add(NCBlocks.NC_BLOCKS.get("beryllium").get());
        }
        return moderators;
    }

    public static void blocks()
    {
        for(String name: FissionBlocks.reactor) {
            String key = "fission_reactor_"+name;
            if(name.contains("controller")) {
                FISSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FissionControllerBlock(REACTOR_BLOCKS_PROPERTIES)));
                FISSION_BE.put(key, BLOCK_ENTITIES.register(key,
                        () -> BlockEntityType.Builder
                                .of(FissionControllerBE::new, FISSION_BLOCKS.get(key).get())
                                .build(null)));
            } else if(name.contains("port")) {
                FISSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FissionPort(REACTOR_BLOCKS_PROPERTIES)));
                FISSION_BE.put(key, BLOCK_ENTITIES.register(key,
                        () -> BlockEntityType.Builder
                                .of(FissionPortBE::new, FISSION_BLOCKS.get(key).get())
                                .build(null)));
            } else if(name.contains("irradiation")) {
                FISSION_BLOCKS.put(key, BLOCKS.register(key, () -> new IrradiationChamberBlock(REACTOR_BLOCKS_PROPERTIES)));
                FISSION_BE.put(key, BLOCK_ENTITIES.register(key,
                        () -> BlockEntityType.Builder
                                .of(FissionIrradiationChamberBE::new, FISSION_BLOCKS.get(key).get())
                                .build(null)));
            } else {
                BlockBehaviour.Properties props;
                if(key.matches(".*glass|.*cell.*")) {
                    props = BlockBehaviour.Properties.of().sound(SoundType.GLASS).strength(1f).requiresCorrectToolForDrops().noOcclusion();
                } else {
                    props = REACTOR_BLOCKS_PROPERTIES;
                }
                if(name.contains("slope")) {
                    FISSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FissionCasingSlopeBlock(props)));
                } else {
                    FISSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FissionBlock(props)));
                }
            }
            FISSION_BLOCK_ITEMS.put(key, fromMultiblock(FISSION_BLOCKS.get(key)));
            ALL_NC_ITEMS.put(key, FISSION_BLOCK_ITEMS.get(key));
        }

        for(String name: FissionBlocks.heatsinks.keySet()) {
            FISSION_BLOCKS.put(name+"_heat_sink", BLOCKS.register(name+"_heat_sink", () -> new HeatSinkBlock(REACTOR_BLOCKS_PROPERTIES, FissionBlocks.heatsinks.get(name))));
            FISSION_BLOCK_ITEMS.put(name+"_heat_sink", fromMultiblock(FISSION_BLOCKS.get(name+"_heat_sink")));
            hsBlocks.add(FISSION_BLOCKS.get(name+"_heat_sink"));
            ALL_NC_ITEMS.put(name+"_heat_sink", FISSION_BLOCK_ITEMS.get(name+"_heat_sink"));
        }


        FISSION_BE.put("fission_heat_sink", BLOCK_ENTITIES.register("fission_heat_sink",
                () -> BlockEntityType.Builder
                        .of(FissionHeatSinkBE::new, getHSBlocks())
                        .build(null)));

        FISSION_BE.put("fission_moderator", BLOCK_ENTITIES.register("fission_moderator",
                () -> BlockEntityType.Builder
                        .of(FissionModeratorBE::new,
                                moderators().toArray(new Block[0]))
                        .build(null)));

        FISSION_BE.put("fission_casing", BLOCK_ENTITIES.register("fission_casing",
                () -> BlockEntityType.Builder
                        .of(FissionCasingBE::new,
                                FISSION_BLOCKS.get("fission_reactor_casing").get(),
                                FISSION_BLOCKS.get("fission_reactor_glass").get())
                        .build(null)));

        FISSION_BE.put("fission_reactor_fuel_cell", BLOCK_ENTITIES.register("fission_reactor_fuel_cell",
                () -> BlockEntityType.Builder
                        .of(FissionFuelCellBE::new,
                                FISSION_BLOCKS.get("fission_reactor_solid_fuel_cell").get())
                        .build(null)));

    }

    private static List<RegistryObject<Block>> hsBlocks = new ArrayList<>();


    public static Block[] getHSBlocks() {
        Block[] blocks = new Block[hsBlocks.size()];
        int i = 0;
        for (RegistryObject<Block> b: hsBlocks) {
            blocks[i] = b.get();
            i++;
        }
        return blocks;
    }

    public static <B extends Block> RegistryObject<Item> fromMultiblock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), FISSION_ITEM_PROPS));
    }

}
