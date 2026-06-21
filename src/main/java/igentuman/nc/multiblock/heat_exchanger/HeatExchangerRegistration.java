package igentuman.nc.multiblock.heat_exchanger;

import igentuman.nc.block.heat_exchanger.HeatExchangerCasingBlock;
import igentuman.nc.block.heat_exchanger.HeatExchangerControllerBlock;
import igentuman.nc.block.heat_exchanger.HeatExchangerPortBlock;
import igentuman.nc.block.heat_exchanger.RadiatorBlock;
import igentuman.nc.block.heat_exchanger.entity.HeatExchangerControllerBE;
import igentuman.nc.block.heat_exchanger.entity.HeatExchangerColdCoolantPortBE;
import igentuman.nc.block.heat_exchanger.entity.HeatExchangerHotCoolantPortBE;
import igentuman.nc.container.HeatExchangerControllerContainer;
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

import java.util.HashMap;
import java.util.function.Supplier;

import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;
import static igentuman.nc.setup.registration.Registries.*;
import static igentuman.nc.setup.registration.Tags.blockTag;

public class HeatExchangerRegistration {

    public static final Item.Properties HX_ITEM_PROPS = new Item.Properties();
    public static final BlockBehaviour.Properties HX_BLOCKS_PROPERTIES = BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(4f).requiresCorrectToolForDrops();
    public static final HashMap<String, RegistryObject<Block>> HX_BLOCKS = new HashMap<>();
    public static final HashMap<String, RegistryObject<BlockEntityType<? extends BlockEntity>>> HX_BE = new HashMap<>();
    public static final HashMap<String, RegistryObject<BlockItem>> HX_BLOCK_ITEMS = new HashMap<>();
    public static final TagKey<Block> CASING_BLOCKS = blockTag("heat_exchanger_casing");
    public static final TagKey<Block> INNER_BLOCKS = blockTag("heat_exchanger_inner");

    public static final RegistryObject<MenuType<HeatExchangerControllerContainer>> HX_CONTROLLER_CONTAINER = CONTAINERS.register("heat_exchanger_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new HeatExchangerControllerContainer(windowId, data.readBlockPos(), inv))
    );

    public static void init() {
        blocks();
    }

    public static RegistryObject<Block> addBlock(String name, Supplier<? extends Block> block) {
        HX_BLOCKS.put(name, BLOCKS.register(name, block));
        HX_BLOCK_ITEMS.put(name, ITEMS.register(name, () -> new BlockItem(HX_BLOCKS.get(name).get(), HX_ITEM_PROPS)));
        return HX_BLOCKS.get(name);
    }

    public static void blocks() {
        RegistryObject<Block> controller = addBlock("heat_exchanger_controller", () -> new HeatExchangerControllerBlock(HX_BLOCKS_PROPERTIES));
        HX_BE.put("heat_exchanger_controller",
                BLOCK_ENTITIES.register("heat_exchanger_controller",
                        () -> BlockEntityType.Builder.of(HeatExchangerControllerBE::new, controller.get())
                                .build(null)));

        RegistryObject<Block> coldCoolantPort = addBlock("heat_exchanger_cold_coolant_port", () -> new HeatExchangerPortBlock(HX_BLOCKS_PROPERTIES));
        HX_BE.put("heat_exchanger_cold_coolant_port",
                BLOCK_ENTITIES.register("heat_exchanger_cold_coolant_port",
                        () -> BlockEntityType.Builder.of(HeatExchangerColdCoolantPortBE::new, coldCoolantPort.get())
                                .build(null)));

        RegistryObject<Block> hotCoolantPort = addBlock("heat_exchanger_hot_coolant_port", () -> new HeatExchangerPortBlock(HX_BLOCKS_PROPERTIES));
        HX_BE.put("heat_exchanger_hot_coolant_port",
                BLOCK_ENTITIES.register("heat_exchanger_hot_coolant_port",
                        () -> BlockEntityType.Builder.of(HeatExchangerHotCoolantPortBE::new, hotCoolantPort.get())
                                .build(null)));

        addBlock("heat_exchanger_casing", () -> new HeatExchangerCasingBlock(HX_BLOCKS_PROPERTIES));
        addBlock("heat_exchanger_radiator", () -> new RadiatorBlock(HX_BLOCKS_PROPERTIES));
        addBlock("heat_exchanger", () -> new Block(HX_BLOCKS_PROPERTIES));
    }
}
