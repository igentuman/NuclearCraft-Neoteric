package igentuman.nc.multiblock.fusion;

import igentuman.nc.block.fusion.FusionBlock;
import igentuman.nc.block.fusion.FusionCoreBlock;
import igentuman.nc.block.fusion.FusionCoreProxy;
import igentuman.nc.container.FusionCoreContainer;
import igentuman.nc.item.FusionCoreItem;
import igentuman.nc.setup.registration.CreativeTabs;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import igentuman.nc.block.entity.fusion.*;
import java.util.HashMap;
import java.util.function.Supplier;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.fission.FissionBlocks.REACTOR_BLOCKS_PROPERTIES;
import static igentuman.nc.setup.Registration.BLOCKS;
import static igentuman.nc.setup.Registration.ITEMS;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;

public class  FusionReactor {
    public static final Item.Properties FUSION_ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.FUSION_REACTOR);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);
    private static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MODID);
    public static HashMap<String, RegistryObject<Block>> FUSION_BLOCKS = new HashMap<>();
    public static HashMap<String, RegistryObject<TileEntityType<? extends TileEntity>>> FUSION_BE = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> FUSION_ITEMS = new HashMap<>();
    public static final RegistryObject<Block> FUSION_CORE_PROXY =
            BLOCKS.register("fusion_reactor_core_proxy",
                    () -> new FusionCoreProxy(REACTOR_BLOCKS_PROPERTIES));
    public static final RegistryObject<TileEntityType<? extends TileEntity>> FUSION_CORE_PROXY_BE =

            BLOCK_ENTITIES.register("fusion_reactor_core_proxy",
                    () -> TileEntityType.Builder
                            .of(FusionCoreProxyBE::new, FUSION_CORE_PROXY.get())
                            .build(null));


    public static final RegistryObject<ContainerType<FusionCoreContainer>> FUSION_CORE_CONTAINER =
            CONTAINERS.register("fusion_reactor_core",
                () -> IForgeContainerType.create((windowId, inv, data) -> new FusionCoreContainer(windowId, data.readBlockPos(), inv))
            );

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCK_ENTITIES.register(bus);
        CONTAINERS.register(bus);
        String key;
        key = "fusion_reactor_connector";
        FUSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FusionBlock(REACTOR_BLOCKS_PROPERTIES)));
        FUSION_BE.put(key, BLOCK_ENTITIES.register(key,
                () -> TileEntityType.Builder
                        .of(FusionConnectorBE::new, FUSION_BLOCKS.get("fusion_reactor_connector").get())
                        .build(null)));
        FUSION_ITEMS.put(key, fromMultiblock(FUSION_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, FUSION_ITEMS.get(key));

        key = "fusion_reactor_casing";
        FUSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FusionBlock(REACTOR_BLOCKS_PROPERTIES)));
        FUSION_BE.put(key, BLOCK_ENTITIES.register(key,
                () -> TileEntityType.Builder
                        .of(FusionCasingBE::new, FUSION_BLOCKS.get("fusion_reactor_casing").get(), FUSION_BLOCKS.get("fusion_reactor_casing_glass").get())
                        .build(null)));
        FUSION_BE.put("fusion_reactor_casing_glass", FUSION_BE.get(key));
        FUSION_ITEMS.put(key, fromMultiblock(FUSION_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, FUSION_ITEMS.get(key));

        key = "fusion_reactor_casing_glass";
        FUSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FusionBlock(Block.Properties.of(Material.METAL).strength(1f).requiresCorrectToolForDrops().noOcclusion())));
        FUSION_ITEMS.put(key, fromMultiblock(FUSION_BLOCKS.get(key)));
        ALL_NC_ITEMS.put(key, FUSION_ITEMS.get(key));

        key = "fusion_core";
        FUSION_BLOCKS.put(key, BLOCKS.register(key, () -> new FusionCoreBlock(REACTOR_BLOCKS_PROPERTIES)));
        FUSION_BE.put(key, BLOCK_ENTITIES.register(key,
                () -> TileEntityType.Builder
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
