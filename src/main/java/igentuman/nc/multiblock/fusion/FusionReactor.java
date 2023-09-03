package igentuman.nc.multiblock.fusion;

import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.block.entity.fission.*;
import igentuman.nc.block.fission.*;
import igentuman.nc.container.FissionControllerContainer;
import igentuman.nc.container.FissionPortContainer;
import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.setup.registration.CreativeTabs;
import igentuman.nc.setup.registration.NCBlocks;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.fission.FissionBlocks.REACTOR_BLOCKS_PROPERTIES;
import static igentuman.nc.setup.Registration.BLOCKS;
import static igentuman.nc.setup.Registration.ITEMS;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;

public class FusionReactor {
    public static final Item.Properties MULTIBLOCK_ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.FISSION_REACTOR);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static HashMap<String, RegistryObject<Block>> MULTI_BLOCKS = new HashMap<>();
    public static HashMap<String, RegistryObject<BlockEntityType<? extends BlockEntity>>> MULTIBLOCK_BE = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> MULTIBLOCK_ITEMS = new HashMap<>();


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
    }


    public static <B extends Block> RegistryObject<Item> fromMultiblock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), MULTIBLOCK_ITEM_PROPERTIES));
    }

}
