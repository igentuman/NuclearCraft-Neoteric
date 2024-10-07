package igentuman.nc.multiblock.fusion;

import igentuman.nc.block.fusion.*;
import igentuman.nc.container.FissionControllerContainer;
import igentuman.nc.container.FusionCoreContainer;
import igentuman.nc.item.FusionCoreItem;
import igentuman.nc.setup.registration.CreativeTabs;
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
import igentuman.nc.block.entity.fusion.*;
import java.util.HashMap;

import static igentuman.nc.multiblock.fission.FissionBlocks.REACTOR_BLOCKS_PROPERTIES;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;
import static igentuman.nc.setup.registration.Registries.*;
import static igentuman.nc.setup.registration.Tags.blockTag;
import static igentuman.nc.setup.registration.Tags.itemTag;

public class FusionReactor {

    public static final Item.Properties FUSION_ITEM_PROPERTIES = new Item.Properties();
    public static HashMap<String, RegistryObject<Block>> FUSION_BLOCKS = new HashMap<>();
    public static HashMap<String, RegistryObject<BlockEntityType<? extends BlockEntity>>> FUSION_BE = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> FUSION_ITEMS = new HashMap<>();
    public static TagKey<Block> CASING_BLOCKS = blockTag("fusion_reactor_casing");
    public static TagKey<Item> CASING_ITEMS = itemTag("fusion_reactor_casing");

    public static final RegistryObject<Block> FUSION_CORE_PROXY =
            BLOCKS.register("fusion_reactor_core_proxy",
                    () -> new FusionCoreProxy(REACTOR_BLOCKS_PROPERTIES));
    public static final RegistryObject<BlockEntityType<? extends BlockEntity>> FUSION_CORE_PROXY_BE =
            BLOCK_ENTITIES.register("fusion_reactor_core_proxy",
                    () -> BlockEntityType.Builder
                            .of(FusionCoreProxyBE::new, FUSION_CORE_PROXY.get())
                            .build(null));

    public static final RegistryObject<MenuType<FusionCoreContainer>> FUSION_CORE_CONTAINER =
            CONTAINERS.register("fusion_reactor_core",
                () -> IForgeMenuType.create((windowId, inv, data) -> new FusionCoreContainer(windowId, data.readBlockPos(), inv))
            );

    public static void init() {
        String key;
        key = "fusion_reactor_connector";
        FUSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FusionConnectorBlock(REACTOR_BLOCKS_PROPERTIES)));
        FUSION_ITEMS.put(key, fromMultiblock(FUSION_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, FUSION_ITEMS.get(key));

        key = "fusion_reactor_casing";
        FUSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FusionCasingBlock(REACTOR_BLOCKS_PROPERTIES)));

        FUSION_ITEMS.put(key, fromMultiblock(FUSION_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, FUSION_ITEMS.get(key));

        key = "fusion_reactor_casing_glass";
        FUSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FusionCasingBlock(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(1f).requiresCorrectToolForDrops().noOcclusion())));
        FUSION_ITEMS.put(key, fromMultiblock(FUSION_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, FUSION_ITEMS.get(key));

        key = "fusion_core";
        FUSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FusionCoreBlock(REACTOR_BLOCKS_PROPERTIES)));
        FUSION_BE.put(key, BLOCK_ENTITIES.register(key,
                () -> BlockEntityType.Builder
                        .of(FusionCoreBE::new, FUSION_BLOCKS.get("fusion_core").get())
                        .build(null)));
        FUSION_ITEMS.put(key,
                ITEMS.register(FUSION_BLOCKS.get(key).getId().getPath(),
                        () -> new FusionCoreItem(FUSION_BLOCKS.get("fusion_core").get(), FUSION_ITEM_PROPERTIES)));
        ALL_NC_ITEMS.put(key, FUSION_ITEMS.get(key));
    }

    public static <B extends Block> RegistryObject<Item> fromMultiblock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), FUSION_ITEM_PROPERTIES));
    }
}
