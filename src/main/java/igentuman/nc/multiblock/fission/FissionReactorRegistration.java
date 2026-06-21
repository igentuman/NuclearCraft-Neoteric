package igentuman.nc.multiblock.fission;

import com.google.gson.JsonArray;
import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.block.fission.entity.FissionPortBE;
import igentuman.nc.block.fission.entity.MSRControllerBE;
import igentuman.nc.block.fission.entity.MSRPortBE;
import igentuman.nc.block.fission.*;
import igentuman.nc.container.FissionControllerContainer;
import igentuman.nc.container.FissionPortContainer;
import igentuman.nc.container.MSRControllerContainer;
import igentuman.nc.util.JSONUtil;
import igentuman.nc.multiblock.ValidationScheduler;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.regex.Pattern;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;
import static igentuman.nc.setup.registration.Registries.*;
import static igentuman.nc.setup.registration.Tags.blockTag;
import static igentuman.nc.setup.registration.Tags.itemTag;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class FissionReactorRegistration {

    public static final Pattern TRANSPARENT_BLOCKS = Pattern.compile(".*glass|.*cell.*|photon.*|.*stabilizer.*");
    public static final Item.Properties FISSION_ITEM_PROPS = new Item.Properties();
    public static final HashMap<String, RegistryObject<Block>> FISSION_BLOCKS = new HashMap<>();
    public static final HashMap<String, RegistryObject<BlockEntityType<? extends BlockEntity>>> FISSION_BE = new HashMap<>();
    public static final HashMap<String, RegistryObject<Item>> FISSION_BLOCK_ITEMS = new HashMap<>();
    private static final List<RegistryObject<Block>> hsBlocks = new ArrayList<>();
    public static final List<String> hsSchedule = new ArrayList<>();
    public static final RegistryObject<MenuType<FissionControllerContainer>> FISSION_CONTROLLER_CONTAINER = CONTAINERS.register("fission_reactor_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FissionControllerContainer(windowId, data.readBlockPos(), inv))
            );

    public static final RegistryObject<MenuType<FissionPortContainer>> FISSION_PORT_CONTAINER = CONTAINERS.register("fission_reactor_port",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FissionPortContainer(windowId, data.readBlockPos(), inv))
            );

    public static final RegistryObject<MenuType<MSRControllerContainer>> MSR_CONTROLLER_CONTAINER = CONTAINERS.register("msr_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new MSRControllerContainer(windowId, data.readBlockPos(), inv))
            );

    public static final BlockBehaviour.Properties REACTOR_BLOCKS_PROPERTIES = BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(4f).requiresCorrectToolForDrops();

    public static final TagKey<Block> MODERATORS_BLOCKS = blockTag("moderators");
    public static final TagKey<Block> HEAT_SINK_BLOCKS = blockTag("heat_sinks");
    public static final TagKey<Block> INNER_REACTOR_BLOCKS = blockTag("reactor_inner");
    public static final TagKey<Item> MODERATORS_ITEMS = itemTag("moderators");
    public static final TagKey<Block> CASING_BLOCKS = blockTag("fission_reactor_casing");
    public static final TagKey<Item> CASING_ITEMS = itemTag("fission_reactor_casing");

    public static final List<String> reactor =  Arrays.asList(
            "casing",
            "controller",
            "irradiation_chamber",
            "pile-driver_irradiation_chamber",
            "port",
            "glass",
            "solid_fuel_cell"
    );

    public static void init() {
        blocks();
        msrBlocks();
    }

    public static void blocks()
    {
        for(String name: reactor) {
            String key = "fission_reactor_"+name;
            if(name.contains("controller")) {
                FISSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FissionControllerBlock(REACTOR_BLOCKS_PROPERTIES)));
                FISSION_BE.put(key, BLOCK_ENTITIES.register(key,
                        () -> BlockEntityType.Builder
                                .of(FissionControllerBE::new, FISSION_BLOCKS.get(key).get())
                                .build(null)));
            } else if(name.contains("port")) {
                FISSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FissionPortBlock(REACTOR_BLOCKS_PROPERTIES)));
                FISSION_BE.put(key, BLOCK_ENTITIES.register(key,
                        () -> BlockEntityType.Builder
                                .of(FissionPortBE::new, FISSION_BLOCKS.get(key).get())
                                .build(null)));
            } else if(name.contains("irradiation")) {
                FISSION_BLOCKS.put(key, BLOCKS.register(key, () -> new IrradiationChamberBlock(REACTOR_BLOCKS_PROPERTIES)));
            } else {
                BlockBehaviour.Properties props;
                if(TRANSPARENT_BLOCKS.matcher(key).matches()) {
                    props = BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(3f).requiresCorrectToolForDrops().noOcclusion();
                } else {
                    props = REACTOR_BLOCKS_PROPERTIES;
                }
                if(key.matches(".*glass|.*casing.*")) {
                    if(key.contains("glass")) {
                        FISSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FissionCasingBlock(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(3f).requiresCorrectToolForDrops().noOcclusion())));
                    } else {
                        FISSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FissionCasingBlock(props)));
                    }
                }
                if(key.matches(".*cell")) {
                    FISSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FissionFuelCellBlock(props)));
                }
            }
            FISSION_BLOCK_ITEMS.put(key, fromMultiblock(FISSION_BLOCKS.get(key)));
            ALL_NC_ITEMS.put(key, FISSION_BLOCK_ITEMS.get(key));
        }

        for(String name: heatsinks.keySet()) {
            FISSION_BLOCKS.put(name+"_heat_sink", BLOCKS.register(name+"_heat_sink", () -> new HeatSinkBlock(REACTOR_BLOCKS_PROPERTIES, heatsinks.get(name))));
            FISSION_BLOCK_ITEMS.put(name+"_heat_sink", fromMultiblock(FISSION_BLOCKS.get(name+"_heat_sink")));
            ALL_NC_ITEMS.put(name+"_heat_sink", FISSION_BLOCK_ITEMS.get(name+"_heat_sink"));
            if(!name.matches("empty|active")) {
                hsBlocks.add(FISSION_BLOCKS.get(name + "_heat_sink"));
            }
        }
    }

    public static void msrBlocks() {
        String key = "msr_controller";
        
        // Register MSR Controller
        FISSION_BLOCKS.put(key, BLOCKS.register(key, () -> new MSRControllerBlock(REACTOR_BLOCKS_PROPERTIES)));
        FISSION_BE.put(key, BLOCK_ENTITIES.register(key,
                () -> BlockEntityType.Builder
                        .of(MSRControllerBE::new, FISSION_BLOCKS.get(key).get())
                        .build(null)));

        FISSION_BLOCK_ITEMS.put(key, fromMultiblock(FISSION_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, FISSION_BLOCK_ITEMS.get(key));

        FISSION_BLOCKS.put("msr_fuel_cell", BLOCKS.register("msr_fuel_cell", () -> new FissionFuelCellBlock(REACTOR_BLOCKS_PROPERTIES)));
        FISSION_BLOCK_ITEMS.put("msr_fuel_cell", fromMultiblock(FISSION_BLOCKS.get("msr_fuel_cell")));
        ALL_NC_ITEMS.put("msr_fuel_cell", FISSION_BLOCK_ITEMS.get("msr_fuel_cell"));

        FISSION_BLOCKS.put("msr_port", BLOCKS.register("msr_port", () -> new MSRPortBlock(REACTOR_BLOCKS_PROPERTIES)));
        FISSION_BE.put("msr_port", BLOCK_ENTITIES.register("msr_port",
                () -> BlockEntityType.Builder
                        .of(MSRPortBE::new, FISSION_BLOCKS.get("msr_port").get())
                        .build(null)));
        FISSION_BLOCK_ITEMS.put("msr_port", fromMultiblock(FISSION_BLOCKS.get("msr_port")));
        ALL_NC_ITEMS.put("msr_port", FISSION_BLOCK_ITEMS.get("msr_port"));
    }

    public static final HashSet<Block> blocks = moderators();

    public static HashSet<Block> moderators() {
        return getBlocksByTagKey(MODERATORS_BLOCKS.location().toString());
    }

    public static final HashMap<String, HeatSinkDef> heatsinks = heatsinks();

    public static HashMap<String, HeatSinkDef> heatsinks() {
        HashMap<String, HeatSinkDef> tmp = new HashMap<>();
        ValidationScheduler scheduler = new ValidationScheduler();
        List<JsonArray> data = JSONUtil.loadAllJsonFromConfig("heat_sinks");
        Pattern activeCheck = Pattern.compile("^(?!.*active_).+_heat_sink$");
        if(data == null) {
            return tmp;
        }
        for (JsonArray array : data) {
            for (int i = 0; i < array.size(); i++) {
                HeatSinkDef heatSink = HeatSinkDef.of(array.get(i).getAsJsonObject());
                if (heatSink != null) {
                    tmp.put(heatSink.name, heatSink);
                    String name = heatSink.name;
                    if (!name.contains(":")) {
                        name = MODID + ":" + name;
                    }
                    for (String rule: heatSink.rules) {
                        String[] conditionParts = rule.split("=|-|>|<|\\^");
                        for (String block: conditionParts[0].split("\\|")) {
                            String blockName = block;
                            if (!block.contains(":")) {
                                blockName = MODID + ":" + block;
                            }
                            scheduler.graphAddEdge(name + "_heat_sink", blockName);
                            if (activeCheck.matcher(block).matches()) {
                                if (block.contains(":")) {
                                    String[] blockParts = block.split(":");
                                    blockName = blockParts[0] + ":active_" + blockParts[1];
                                } else {
                                    blockName = MODID + ":active_" + block;
                                }
                                scheduler.graphAddEdge(name + "_heat_sink", blockName);
                            }
                        }
                    }
                }
            }
        }
        hsSchedule.addAll(scheduler.getSchedule());
        return tmp;
    }

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
