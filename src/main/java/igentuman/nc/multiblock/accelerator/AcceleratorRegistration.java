package igentuman.nc.multiblock.accelerator;

import igentuman.nc.block.accelerator.AcceleratorBlock;
import igentuman.nc.block.accelerator.AcceleratorOrientedBlock;
import igentuman.nc.block.accelerator.LinearAcceleratorControllerBlock;
import igentuman.nc.block.entity.accelerator.AcceleratorPortBE;
import igentuman.nc.block.entity.accelerator.LinearAcceleratorControllerBE;
import igentuman.nc.container.AcceleratorPortContainer;
import igentuman.nc.container.ChamberPortContainer;
import igentuman.nc.container.ChamberTerminalContainer;
import igentuman.nc.container.LinearAcceleratorContainer;
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
import static igentuman.nc.setup.registration.Registries.CONTAINERS;
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
    public static final TagKey<Item> ACCELERATOR_CASING_ITEMS = itemTag("accelerator_casing");
    public static final Pattern TRANSPARENT_BLOCKS_PATTERN = Pattern.compile(".*glass.*");

    public static final RegistryObject<MenuType<LinearAcceleratorContainer>> LINEAR_ACCELERATOR_CONTROLLER_CONTAINER = CONTAINERS.register("linear_accelerator_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new LinearAcceleratorContainer(windowId, data.readBlockPos(), inv))
    );
    public static final RegistryObject<MenuType<AcceleratorPortContainer>> ACCELERATOR_PORT_CONTAINER = CONTAINERS.register("accelerator_port",
            () -> IForgeMenuType.create((windowId, inv, data) -> new AcceleratorPortContainer(windowId, data.readBlockPos(), inv))
    );

    public static void init() {
        registerSimpleBlock("accelerator_casing");
        registerSimpleBlock("accelerator_casing_glass");
        registerSimpleBlock("accelerator_beam");
        registerOrientedBlock("linear_accelerator_controller");
        registerOrientedBlock("synthrotron_controller");
        registerOrientedBlock("accelerator_port");
        registerOrientedBlock("accelerator_beam_port");
        registerOrientedBlock("accelerator_ion_source_port");

        ACCELERATOR_BE.put("accelerator_port",
                BLOCK_ENTITIES.register("accelerator_port",
                        () -> BlockEntityType.Builder.of(AcceleratorPortBE::new, ACCELERATOR_BLOCKS.get("accelerator_port").get())
                                .build(null)));

        ACCELERATOR_BE.put("accelerator_beam_port",
                BLOCK_ENTITIES.register("accelerator_beam_port",
                        () -> BlockEntityType.Builder.of(AcceleratorPortBE::new, ACCELERATOR_BLOCKS.get("accelerator_beam_port").get())
                                .build(null)));

        ACCELERATOR_BE.put("accelerator_ion_source_port",
                BLOCK_ENTITIES.register("accelerator_ion_source_port",
                        () -> BlockEntityType.Builder.of(AcceleratorPortBE::new, ACCELERATOR_BLOCKS.get("accelerator_ion_source_port").get())
                                .build(null)));

        ACCELERATOR_BE.put("linear_accelerator_controller",
                BLOCK_ENTITIES.register("linear_accelerator_controller",
                        () -> BlockEntityType.Builder.of(LinearAcceleratorControllerBE::new, ACCELERATOR_BLOCKS.get("linear_accelerator_controller").get())
                                .build(null)));

        ACCELERATOR_BE.put("synthrotron_controller",
                BLOCK_ENTITIES.register("synthrotron_controller",
                        () -> BlockEntityType.Builder.of(LinearAcceleratorControllerBE::new, ACCELERATOR_BLOCKS.get("synthrotron_controller").get())
                                .build(null)));
    }

    private static void registerOrientedBlock(String key) {
        BlockBehaviour.Properties props = TRANSPARENT_BLOCKS_PATTERN.matcher(key).matches()
                ? NO_OCCLUSION_BLOCK_PROPS
                : ACCELERATOR_BLOCK_PROPERTIES;
        if(key.contains("linear_accelerator_controller")) {
            ACCELERATOR_BLOCKS.put(key, BLOCKS.register(key, () -> new LinearAcceleratorControllerBlock(props)));
        } else if(key.contains("synthrotron_controller")) {
            ACCELERATOR_BLOCKS.put(key, BLOCKS.register(key, () -> new LinearAcceleratorControllerBlock(props)));
        } else {
            ACCELERATOR_BLOCKS.put(key, BLOCKS.register(key, () -> new AcceleratorOrientedBlock(props)));
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
}
