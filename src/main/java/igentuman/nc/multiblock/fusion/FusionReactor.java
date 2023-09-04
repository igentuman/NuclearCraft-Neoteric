package igentuman.nc.multiblock.fusion;

import igentuman.nc.block.fusion.FusionBlock;
import igentuman.nc.block.fusion.FusionCoreBlock;
import igentuman.nc.container.FissionControllerContainer;
import igentuman.nc.setup.registration.CreativeTabs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import igentuman.nc.block.entity.fusion.*;
import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.fission.FissionBlocks.REACTOR_BLOCKS_PROPERTIES;
import static igentuman.nc.setup.Registration.BLOCKS;
import static igentuman.nc.setup.Registration.ITEMS;

public class FusionReactor {
    public static final Item.Properties FUSION_ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.FISSION_REACTOR);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static HashMap<String, RegistryObject<Block>> FUSION_BLOCKS = new HashMap<>();
    public static HashMap<String, RegistryObject<BlockEntityType<? extends BlockEntity>>> FUSION_BE = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> FUSION_ITEMS = new HashMap<>();


    public static final RegistryObject<MenuType<FissionControllerContainer>> FISSION_CORE_CONTAINER = CONTAINERS.register("fission_reactor_controller",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FissionControllerContainer(windowId, data.readBlockPos(), inv))
            );


    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCK_ENTITIES.register(bus);
        CONTAINERS.register(bus);

        FUSION_BLOCKS.put("fusion_connector", BLOCKS.register("fusion_connector", () -> new FusionBlock(REACTOR_BLOCKS_PROPERTIES)));
        FUSION_BE.put("fusion_connector", BLOCK_ENTITIES.register("fusion_connector",
                () -> BlockEntityType.Builder
                        .of(FusionConnectorBE::new, FUSION_BLOCKS.get("fusion_connector").get())
                        .build(null)));

        FUSION_BLOCKS.put("fusion_casing", BLOCKS.register("fusion_casing", () -> new FusionBlock(REACTOR_BLOCKS_PROPERTIES)));
        FUSION_BE.put("fusion_casing", BLOCK_ENTITIES.register("fusion_casing",
                () -> BlockEntityType.Builder
                        .of(FusionCasingBE::new, FUSION_BLOCKS.get("fusion_casing").get())
                        .build(null)));

        FUSION_BLOCKS.put("fusion_casing_glass", BLOCKS.register("fusion_casing_glass", () -> new FusionBlock(REACTOR_BLOCKS_PROPERTIES)));
        FUSION_BE.put("fusion_casing_glass", BLOCK_ENTITIES.register("fusion_casing_glass",
                () -> BlockEntityType.Builder
                        .of(FusionCasingBE::new, FUSION_BLOCKS.get("fusion_casing_glass").get())
                        .build(null)));

        FUSION_BLOCKS.put("fusion_core", BLOCKS.register("fusion_core", () -> new FusionCoreBlock(REACTOR_BLOCKS_PROPERTIES)));
        FUSION_BE.put("fusion_core", BLOCK_ENTITIES.register("fusion_core",
                () -> BlockEntityType.Builder
                        .of(FusionCoreBE::new, FUSION_BLOCKS.get("fusion_core").get())
                        .build(null)));
    }


    public static <B extends Block> RegistryObject<Item> fromMultiblock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), FUSION_ITEM_PROPERTIES));
    }

}
