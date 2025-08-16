package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberPortBE;
import igentuman.nc.block.target_chamber.*;
import igentuman.nc.container.TargetChamberControllerContainer;
import igentuman.nc.container.TargetChamberPortContainer;
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

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static igentuman.nc.setup.registration.NCBlocks.fromMultiblock;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;
import static igentuman.nc.setup.registration.Registries.*;
import static igentuman.nc.setup.registration.Tags.blockTag;
import static igentuman.nc.setup.registration.Tags.itemTag;

public class TargetChamberRegistration {
    public static final Item.Properties TARGET_CHAMBER_ITEM_PROPERTIES = new Item.Properties();
    public static final BlockBehaviour.Properties NO_OCCLUSION_BLOCK_PROPS = BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(3f).requiresCorrectToolForDrops().noOcclusion();
    public static final Block.Properties TARGET_CHAMBER_BLOCK_PROPERTIES =  BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(4f).requiresCorrectToolForDrops();;
    public static final HashMap<String, RegistryObject<Block>> TARGET_CHAMBER_BLOCKS = new HashMap<>();
    public static final HashMap<String, RegistryObject<BlockEntityType<? extends BlockEntity>>> TARGET_CHAMBER_BE = new HashMap<>();
    public static final HashMap<String, RegistryObject<Item>> TARGET_CHAMBER_ITEMS = new HashMap<>();
    public static final TagKey<Block> TARGET_CHAMBER_CASING_BLOCKS = blockTag("target_chamber_casing");
    public static final TagKey<Block> TARGET_CHAMBER_INNER_BLOCKS = blockTag("target_chamber_inner");
    public static final TagKey<Item> TARGET_CHAMBER_INNER_ITEMS = itemTag("target_chamber_inner");
    public static final TagKey<Item> TARGET_CHAMBER_CASING_ITEMS = itemTag("target_chamber_casing");
    public static final Pattern TRANSPARENT_BLOCKS_PATTERN = Pattern.compile(".*glass.*");
    public static final HashMap<String, DetectorDef> TARGET_CHAMBER_DETECTORS = new HashMap<>();

    public static final RegistryObject<MenuType<TargetChamberControllerContainer>> TARGET_CHAMBER_CONTROLLER_CONTAINER = CONTAINERS.register("target_chamber_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new TargetChamberControllerContainer(windowId, data.readBlockPos(), inv))
    );
    public static final RegistryObject<MenuType<TargetChamberPortContainer>> TARGET_CHAMBER_PORT_CONTAINER = CONTAINERS.register("target_chamber_port",
            () -> IForgeMenuType.create((windowId, inv, data) -> new TargetChamberPortContainer(windowId, data.readBlockPos(), inv))
    );

    public static List<DetectorDef> detectors() {
        return List.of(
                DetectorDef.make("bubble_chamber", 0.075D, 200, 2),
                DetectorDef.make("silicon_tracker", 0.15D, 2000, 1),
                DetectorDef.make("wire_chamber", 0.1D, 1000, 2),
                DetectorDef.make("em_calorimeter", 0.05D, 200, 3),
                DetectorDef.make("hadron_calorimeter", 0.025D, 100, 4)
        );
    }

    public static void init() {
        for(DetectorDef def : detectors()) {
            registerDetector(def.name, def);
        }
        registerSimpleBlock("target_chamber_camera");
        registerSimpleBlock("target_chamber_casing");
        registerSimpleBlock("target_chamber_casing_glass");
        registerOrientedBlock("target_chamber_controller");
        registerOrientedBlock("target_chamber_port");
        registerOrientedBlock("target_chamber_beam_port");

        TARGET_CHAMBER_BE.put("target_chamber_port",
                BLOCK_ENTITIES.register("target_chamber_port",
                        () -> BlockEntityType.Builder.of(TargetChamberPortBE::new, TARGET_CHAMBER_BLOCKS.get("target_chamber_port").get())
                                .build(null)));

        TARGET_CHAMBER_BE.put("target_chamber_beam_port",
                BLOCK_ENTITIES.register("target_chamber_beam_port",
                        () -> BlockEntityType.Builder.of(TargetChamberBeamPortBE::new, TARGET_CHAMBER_BLOCKS.get("target_chamber_beam_port").get())
                                .build(null)));

        TARGET_CHAMBER_BE.put("target_chamber_controller",
                BLOCK_ENTITIES.register("target_chamber_controller",
                        () -> BlockEntityType.Builder.of(TargetChamberControllerBE::new, TARGET_CHAMBER_BLOCKS.get("target_chamber_controller").get())
                                .build(null)));
    }

    private static void registerOrientedBlock(String key) {
        BlockBehaviour.Properties props = TRANSPARENT_BLOCKS_PATTERN.matcher(key).matches()
                ? NO_OCCLUSION_BLOCK_PROPS
                : TARGET_CHAMBER_BLOCK_PROPERTIES;
        if(key.equals("target_chamber_controller")) {
            TARGET_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new TargetChamberControllerBlock(props)));
        }
        if(key.equals("target_chamber_port")) {
            TARGET_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new TargetChamberPortBlock(props)));
        }
        if(key.equals("target_chamber_beam_port")) {
            TARGET_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new TargetChamberBeamPortBlock(props)));
        }
        TARGET_CHAMBER_ITEMS.put(key, fromMultiblock(TARGET_CHAMBER_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, TARGET_CHAMBER_ITEMS.get(key));
    }

    private static void registerDetector(String key, DetectorDef def) {
        BlockBehaviour.Properties props = TRANSPARENT_BLOCKS_PATTERN.matcher(key).matches()
                ? NO_OCCLUSION_BLOCK_PROPS
                : TARGET_CHAMBER_BLOCK_PROPERTIES;
        TARGET_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new DetectorBlock(props, def)));
        TARGET_CHAMBER_DETECTORS.put(key, def);
        TARGET_CHAMBER_ITEMS.put(key, fromMultiblock(TARGET_CHAMBER_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, TARGET_CHAMBER_ITEMS.get(key));
    }

    private static void registerSimpleBlock(String key) {
        BlockBehaviour.Properties props = TRANSPARENT_BLOCKS_PATTERN.matcher(key).matches()
                ? NO_OCCLUSION_BLOCK_PROPS
                : TARGET_CHAMBER_BLOCK_PROPERTIES;
        if(key.equals("target_chamber_camera")) {
            TARGET_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new TargetChamberCameraBlock(props)));
        } else {
            TARGET_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new TargetChamberBlock(props)));
        }
        TARGET_CHAMBER_ITEMS.put(key, fromMultiblock(TARGET_CHAMBER_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, TARGET_CHAMBER_ITEMS.get(key));
    }
}
