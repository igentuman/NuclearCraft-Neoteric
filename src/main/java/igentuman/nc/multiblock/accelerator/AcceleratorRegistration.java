package igentuman.nc.multiblock.accelerator;

import com.google.gson.JsonArray;
import igentuman.nc.block.accelerator.*;
import igentuman.nc.block.accelerator.entity.*;
import igentuman.nc.container.AcceleratorIonSourcePortContainer;
import igentuman.nc.container.AcceleratorPortContainer;
import igentuman.nc.container.LinearAcceleratorContainer;
import igentuman.nc.container.ThoroidalAcceleratorContainer;
import igentuman.nc.util.JSONUtil;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static igentuman.nc.setup.registration.NCBlocks.fromMultiblock;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;
import static igentuman.nc.setup.registration.Registries.*;
import static igentuman.nc.setup.registration.Tags.blockTag;
import static igentuman.nc.setup.registration.Tags.itemTag;

public class AcceleratorRegistration {
    public static final Item.Properties ACCELERATOR_ITEM_PROPERTIES = new Item.Properties();
    public static final BlockBehaviour.Properties NO_OCCLUSION_BLOCK_PROPS = BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(3f).requiresCorrectToolForDrops().noOcclusion();
    public static final Block.Properties ACCELERATOR_BLOCK_PROPERTIES =  BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(4f).requiresCorrectToolForDrops();;
    public static final HashMap<String, RegistryObject<Block>> ACCELERATOR_BLOCKS = new HashMap<>();
    public static final HashMap<String, RegistryObject<BlockEntityType<? extends BlockEntity>>> ACCELERATOR_BE = new HashMap<>();
    public static final HashMap<String, RegistryObject<Item>> ACCELERATOR_ITEMS = new HashMap<>();
    public static final TagKey<Block> ACCELERATOR_CASING_BLOCKS = blockTag("accelerator_casing");
    public static final TagKey<Block> ACCELERATOR_INNER_BLOCKS = blockTag("accelerator_inner");
    public static final TagKey<Item> ACCELERATOR_INNER_ITEMS = itemTag("accelerator_inner");
    public static final TagKey<Item> ACCELERATOR_CASING_ITEMS = itemTag("accelerator_casing");
    public static final Pattern TRANSPARENT_BLOCKS_PATTERN = Pattern.compile(".*glass.*");

    public static final RegistryObject<MenuType<LinearAcceleratorContainer>> LINEAR_ACCELERATOR_CONTROLLER_CONTAINER = CONTAINERS.register("linear_accelerator_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new LinearAcceleratorContainer(windowId, data.readBlockPos(), inv))
    );
    public static final RegistryObject<MenuType<ThoroidalAcceleratorContainer>> THOROIDAL_ACCELERATOR_CONTROLLER_CONTAINER = CONTAINERS.register("thoroidal_accelerator_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new ThoroidalAcceleratorContainer(windowId, data.readBlockPos(), inv))
    );
    public static final RegistryObject<MenuType<AcceleratorPortContainer>> ACCELERATOR_PORT_CONTAINER = CONTAINERS.register("accelerator_port",
            () -> IForgeMenuType.create((windowId, inv, data) -> new AcceleratorPortContainer(windowId, data.readBlockPos(), inv))
    );
    public static final RegistryObject<MenuType<AcceleratorIonSourcePortContainer>> ACCELERATOR_ION_SOURCE_PORT_CONTAINER = CONTAINERS.register("accelerator_ion_source_port",
            () -> IForgeMenuType.create((windowId, inv, data) -> new AcceleratorIonSourcePortContainer(windowId, data.readBlockPos(), inv))
    );

    public static final HashMap<String,CoolerDef> COOLERS = coolers();

    private static final List<RegistryObject<Block>> COOLER_BLOCKS = new ArrayList<>();

    public static void init() {
        registerSimpleBlock("accelerator_casing");
        registerSimpleBlock("electromagnet_yoke");
        registerSimpleBlock("accelerator_casing_glass");
        registerSimpleBlock("particle_beam");
        registerOrientedBlock("linear_accelerator_controller");
        registerOrientedBlock("thoroidal_accelerator_controller");
        registerOrientedBlock("accelerator_port");
        registerOrientedBlock("accelerator_beam_port");
        registerOrientedBlock("accelerator_ion_source_port");

        ACCELERATOR_BE.put("accelerator_port",
                BLOCK_ENTITIES.register("accelerator_port",
                        () -> BlockEntityType.Builder.of(AcceleratorPortBE::new, ACCELERATOR_BLOCKS.get("accelerator_port").get())
                                .build(null)));

        ACCELERATOR_BE.put("accelerator_beam_port",
                BLOCK_ENTITIES.register("accelerator_beam_port",
                        () -> BlockEntityType.Builder.of(AcceleratorBeamPortBE::new, ACCELERATOR_BLOCKS.get("accelerator_beam_port").get())
                                .build(null)));

        ACCELERATOR_BE.put("accelerator_ion_source_port",
                BLOCK_ENTITIES.register("accelerator_ion_source_port",
                        () -> BlockEntityType.Builder.of(AcceleratorIonSourcePortBE::new, ACCELERATOR_BLOCKS.get("accelerator_ion_source_port").get())
                                .build(null)));

        ACCELERATOR_BE.put("linear_accelerator_controller",
                BLOCK_ENTITIES.register("linear_accelerator_controller",
                        () -> BlockEntityType.Builder.of(LinearAcceleratorControllerBE::new, ACCELERATOR_BLOCKS.get("linear_accelerator_controller").get())
                                .build(null)));

        ACCELERATOR_BE.put("thoroidal_accelerator_controller",
                BLOCK_ENTITIES.register("thoroidal_accelerator_controller",
                        () -> BlockEntityType.Builder.of(ThoroidalAcceleratorControllerBE::new, ACCELERATOR_BLOCKS.get("thoroidal_accelerator_controller").get())
                                .build(null)));

        for(String name: COOLERS.keySet()) {
            ACCELERATOR_BLOCKS.put(name+"_cooler", BLOCKS.register(name+"_cooler", () -> new CoolerBlock(ACCELERATOR_BLOCK_PROPERTIES, COOLERS.get(name))));
            ACCELERATOR_ITEMS.put(name+"_cooler", fromMultiblock(ACCELERATOR_BLOCKS.get(name+"_cooler")));
            ALL_NC_ITEMS.put(name+"_cooler", ACCELERATOR_ITEMS.get(name+"_cooler"));
            if(!name.contains("empty")) {
                COOLER_BLOCKS.add(ACCELERATOR_BLOCKS.get(name + "_cooler"));
            }
        }
    }

    private static void registerOrientedBlock(String key) {
        BlockBehaviour.Properties props = TRANSPARENT_BLOCKS_PATTERN.matcher(key).matches()
                ? NO_OCCLUSION_BLOCK_PROPS
                : ACCELERATOR_BLOCK_PROPERTIES;
        switch (key) {
            case "linear_accelerator_controller" ->
                    ACCELERATOR_BLOCKS.put(key, BLOCKS.register(key, () -> new LinearAcceleratorControllerBlock(props)));
            case "thoroidal_accelerator_controller" ->
                    ACCELERATOR_BLOCKS.put(key, BLOCKS.register(key, () -> new ThoroidalAcceleratorControllerBlock(props)));
            case "accelerator_ion_source_port" ->
                    ACCELERATOR_BLOCKS.put(key, BLOCKS.register(key, () -> new AcceleratorIonSourcePortBlock(props)));
            case "accelerator_port" ->
                    ACCELERATOR_BLOCKS.put(key, BLOCKS.register(key, () -> new AcceleratorPortBlock(props)));
            case "accelerator_beam_port" ->
                    ACCELERATOR_BLOCKS.put(key, BLOCKS.register(key, () -> new AcceleratorBeamPortBlock(props)));
            default -> ACCELERATOR_BLOCKS.put(key, BLOCKS.register(key, () -> new AcceleratorOrientedBlock(props)));
        }
        ACCELERATOR_ITEMS.put(key, fromMultiblock(ACCELERATOR_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, ACCELERATOR_ITEMS.get(key));
    }

    private static void registerSimpleBlock(String key) {
        BlockBehaviour.Properties props = TRANSPARENT_BLOCKS_PATTERN.matcher(key).matches()
                ? NO_OCCLUSION_BLOCK_PROPS
                : ACCELERATOR_BLOCK_PROPERTIES;
        ACCELERATOR_BLOCKS.put(key, BLOCKS.register(key, () -> new AcceleratorBlock(props)));
        ACCELERATOR_ITEMS.put(key, fromMultiblock(ACCELERATOR_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, ACCELERATOR_ITEMS.get(key));
    }

    public static HashMap<String, CoolerDef> coolers() {
        HashMap<String, CoolerDef> tmp = new HashMap<>();
        List<JsonArray> data = JSONUtil.loadAllJsonFromConfig("accelerator_coolers");
        if(data == null) {
            return tmp;
        }
        for (JsonArray array : data) {
            for (int i = 0; i < array.size(); i++) {
                CoolerDef heatSink = CoolerDef.of(array.get(i).getAsJsonObject());
                if (heatSink != null) {
                    tmp.put(heatSink.name, heatSink);
                }
            }
        }
        return tmp;
    }

    public static Block[] getCoolerBlocks() {
        Block[] blocks = new Block[COOLER_BLOCKS.size()];
        int i = 0;
        for (RegistryObject<Block> b: COOLER_BLOCKS) {
            blocks[i] = b.get();
            i++;
        }
        return blocks;
    }
}
