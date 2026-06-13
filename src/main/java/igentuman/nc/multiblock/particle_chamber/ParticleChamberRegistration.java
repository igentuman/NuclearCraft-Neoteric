package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.block.collision_chamber.CollisionChamberControllerBlock;
import igentuman.nc.block.collision_chamber.CollisionChamberPortBlock;
import igentuman.nc.block.collision_chamber.entity.CollisionChamberControllerBE;
import igentuman.nc.block.collision_chamber.entity.CollisionChamberPortBE;
import igentuman.nc.block.decay_chamber.DecayChamberControllerBlock;
import igentuman.nc.block.decay_chamber.DecayChamberPortBlock;
import igentuman.nc.block.decay_chamber.entity.DecayChamberControllerBE;
import igentuman.nc.block.decay_chamber.entity.DecayChamberPortBE;
import igentuman.nc.block.target_chamber.*;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberPortBE;
import igentuman.nc.container.CollisionChamberControllerContainer;
import igentuman.nc.container.CollisionChamberPortContainer;
import igentuman.nc.container.DecayChamberControllerContainer;
import igentuman.nc.container.DecayChamberPortContainer;
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

public class ParticleChamberRegistration {
    public static final Item.Properties TARGET_CHAMBER_ITEM_PROPERTIES = new Item.Properties();
    public static final BlockBehaviour.Properties NO_OCCLUSION_BLOCK_PROPS = BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(3f).requiresCorrectToolForDrops().noOcclusion();
    public static final Block.Properties TARGET_CHAMBER_BLOCK_PROPERTIES = BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(4f).requiresCorrectToolForDrops();
    public static final HashMap<String, RegistryObject<Block>> PARTICLE_CHAMBER_BLOCKS = new HashMap<>();
    public static final HashMap<String, RegistryObject<BlockEntityType<? extends BlockEntity>>> TARGET_CHAMBER_BE = new HashMap<>();
    public static final HashMap<String, RegistryObject<Item>> TARGET_CHAMBER_ITEMS = new HashMap<>();
    public static final TagKey<Block> TARGET_CHAMBER_CASING_BLOCKS = blockTag("target_chamber_casing");
    public static final TagKey<Block> DECAY_CHAMBER_CASING_BLOCKS = blockTag("decay_chamber_casing");
    public static final TagKey<Block> COLLISION_CHAMBER_CASING_BLOCKS = blockTag("collision_chamber_casing");
    public static final TagKey<Block> TARGET_CHAMBER_INNER_BLOCKS = blockTag("target_chamber_inner");
    public static final TagKey<Item> TARGET_CHAMBER_INNER_ITEMS = itemTag("target_chamber_inner");
    public static final TagKey<Item> TARGET_CHAMBER_CASING_ITEMS = itemTag("target_chamber_casing");
    public static final Pattern TRANSPARENT_BLOCKS_PATTERN = Pattern.compile(".*glass.*");
    public static final HashMap<String, DetectorDef> TARGET_CHAMBER_DETECTORS = new HashMap<>();

    public static final RegistryObject<MenuType<TargetChamberControllerContainer>> TARGET_CHAMBER_CONTROLLER_CONTAINER = CONTAINERS.register("target_chamber_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new TargetChamberControllerContainer(windowId, data.readBlockPos(), inv))
    );
    public static final RegistryObject<MenuType<CollisionChamberControllerContainer>> COLLISION_CHAMBER_CONTROLLER_CONTAINER = CONTAINERS.register("collision_chamber_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new CollisionChamberControllerContainer(windowId, data.readBlockPos(), inv))
    );
    public static final RegistryObject<MenuType<TargetChamberPortContainer>> TARGET_CHAMBER_PORT_CONTAINER = CONTAINERS.register("target_chamber_port",
            () -> IForgeMenuType.create((windowId, inv, data) -> new TargetChamberPortContainer(windowId, data.readBlockPos(), inv))
    );
    public static final RegistryObject<MenuType<CollisionChamberPortContainer>> COLLISION_CHAMBER_PORT_CONTAINER = CONTAINERS.register("collision_chamber_port",
            () -> IForgeMenuType.create((windowId, inv, data) -> new CollisionChamberPortContainer(windowId, data.readBlockPos(), inv))
    );
    public static final RegistryObject<MenuType<DecayChamberControllerContainer>> DECAY_CHAMBER_CONTROLLER_CONTAINER = CONTAINERS.register("decay_chamber_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new DecayChamberControllerContainer(windowId, data.readBlockPos(), inv))
    );
    public static final RegistryObject<MenuType<DecayChamberPortContainer>> DECAY_CHAMBER_PORT_CONTAINER = CONTAINERS.register("decay_chamber_port",
            () -> IForgeMenuType.create((windowId, inv, data) -> new DecayChamberPortContainer(windowId, data.readBlockPos(), inv))
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
        for (DetectorDef def : detectors()) {
            registerDetector(def.name, def);
        }
        registerSimpleBlock("target_chamber_camera");
        registerSimpleBlock("target_chamber_casing");
        registerSimpleBlock("target_chamber_casing_glass");
        registerOrientedBlock("collision_chamber_controller");
        registerOrientedBlock("decay_chamber_controller");
        registerOrientedBlock("target_chamber_controller");
        registerOrientedBlock("collision_chamber_port");
        registerOrientedBlock("decay_chamber_port");
        registerOrientedBlock("target_chamber_port");
        registerOrientedBlock("target_chamber_beam_port");

        TARGET_CHAMBER_BE.put("target_chamber_port",
                BLOCK_ENTITIES.register("target_chamber_port",
                        () -> BlockEntityType.Builder.of(TargetChamberPortBE::new, PARTICLE_CHAMBER_BLOCKS.get("target_chamber_port").get())
                                .build(null)));

        TARGET_CHAMBER_BE.put("collision_chamber_port",
                BLOCK_ENTITIES.register("collision_chamber_port",
                        () -> BlockEntityType.Builder.of(CollisionChamberPortBE::new, PARTICLE_CHAMBER_BLOCKS.get("collision_chamber_port").get())
                                .build(null)));

        TARGET_CHAMBER_BE.put("decay_chamber_port",
                BLOCK_ENTITIES.register("decay_chamber_port",
                        () -> BlockEntityType.Builder.of(DecayChamberPortBE::new, PARTICLE_CHAMBER_BLOCKS.get("decay_chamber_port").get())
                                .build(null)));

        TARGET_CHAMBER_BE.put("target_chamber_beam_port",
                BLOCK_ENTITIES.register("target_chamber_beam_port",
                        () -> BlockEntityType.Builder.of(TargetChamberBeamPortBE::new, PARTICLE_CHAMBER_BLOCKS.get("target_chamber_beam_port").get())
                                .build(null)));

        TARGET_CHAMBER_BE.put("target_chamber_controller",
                BLOCK_ENTITIES.register("target_chamber_controller",
                        () -> BlockEntityType.Builder.of(TargetChamberControllerBE::new, PARTICLE_CHAMBER_BLOCKS.get("target_chamber_controller").get())
                                .build(null)));

        TARGET_CHAMBER_BE.put("decay_chamber_controller",
                BLOCK_ENTITIES.register("decay_chamber_controller",
                        () -> BlockEntityType.Builder.of(DecayChamberControllerBE::new, PARTICLE_CHAMBER_BLOCKS.get("decay_chamber_controller").get())
                                .build(null)));

        TARGET_CHAMBER_BE.put("collision_chamber_controller",
                BLOCK_ENTITIES.register("collision_chamber_controller",
                        () -> BlockEntityType.Builder.of(CollisionChamberControllerBE::new, PARTICLE_CHAMBER_BLOCKS.get("collision_chamber_controller").get())
                                .build(null)));
    }

    private static void registerOrientedBlock(String key) {
        BlockBehaviour.Properties props = TRANSPARENT_BLOCKS_PATTERN.matcher(key).matches()
                ? NO_OCCLUSION_BLOCK_PROPS
                : TARGET_CHAMBER_BLOCK_PROPERTIES;
        switch (key) {
            case "target_chamber_controller" ->
                    PARTICLE_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new TargetChamberControllerBlock(props)));
            case "collision_chamber_controller" ->
                    PARTICLE_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new CollisionChamberControllerBlock(props)));
            case "decay_chamber_controller" ->
                    PARTICLE_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new DecayChamberControllerBlock(props)));
            case "target_chamber_port" ->
                    PARTICLE_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new TargetChamberPortBlock(props)));
            case "collision_chamber_port" ->
                    PARTICLE_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new CollisionChamberPortBlock(props)));
            case "decay_chamber_port" ->
                    PARTICLE_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new DecayChamberPortBlock(props)));
            case "target_chamber_beam_port" ->
                    PARTICLE_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new TargetChamberBeamPortBlock(props)));
            default -> throw new IllegalArgumentException("Unknown oriented particle chamber block: " + key);
        }
        TARGET_CHAMBER_ITEMS.put(key, fromMultiblock(PARTICLE_CHAMBER_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, TARGET_CHAMBER_ITEMS.get(key));
    }

    private static void registerDetector(String key, DetectorDef def) {
        BlockBehaviour.Properties props = TRANSPARENT_BLOCKS_PATTERN.matcher(key).matches()
                ? NO_OCCLUSION_BLOCK_PROPS
                : TARGET_CHAMBER_BLOCK_PROPERTIES;
        PARTICLE_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new DetectorBlock(props, def)));
        TARGET_CHAMBER_DETECTORS.put(key, def);
        TARGET_CHAMBER_ITEMS.put(key, fromMultiblock(PARTICLE_CHAMBER_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, TARGET_CHAMBER_ITEMS.get(key));
    }

    private static void registerSimpleBlock(String key) {
        BlockBehaviour.Properties props = TRANSPARENT_BLOCKS_PATTERN.matcher(key).matches()
                ? NO_OCCLUSION_BLOCK_PROPS
                : TARGET_CHAMBER_BLOCK_PROPERTIES;
        if (key.equals("target_chamber_camera")) {
            PARTICLE_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new TargetChamberCameraBlock(props)));
        } else {
            PARTICLE_CHAMBER_BLOCKS.put(key, BLOCKS.register(key, () -> new TargetChamberBlock(props)));
        }
        TARGET_CHAMBER_ITEMS.put(key, fromMultiblock(PARTICLE_CHAMBER_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, TARGET_CHAMBER_ITEMS.get(key));
    }
}
