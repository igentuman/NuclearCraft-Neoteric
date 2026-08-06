package igentuman.nc.setup.registration;

import igentuman.nc.block.fission.FissionDesignerBlock;
import igentuman.nc.block.fission.entity.FissionDesignerBE;
import igentuman.nc.container.FissionDesignerContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;
import static igentuman.nc.setup.registration.Registries.*;

public class FissionDesignerRegistration {

    public static final BlockBehaviour.Properties DESIGNER_PROPERTIES = BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(4f).requiresCorrectToolForDrops();
    public static final Item.Properties DESIGNER_ITEM_PROPS = new Item.Properties();

    public static final RegistryObject<Block> FISSION_REACTOR_DESIGNER = BLOCKS.register("fission_reactor_designer", () -> new FissionDesignerBlock(DESIGNER_PROPERTIES));
    public static final RegistryObject<BlockEntityType<FissionDesignerBE>> FISSION_REACTOR_DESIGNER_BE = BLOCK_ENTITIES.register("fission_reactor_designer",
            () -> BlockEntityType.Builder.of(FissionDesignerBE::new, FISSION_REACTOR_DESIGNER.get()).build(null));
    public static final RegistryObject<Item> FISSION_REACTOR_DESIGNER_ITEM = ITEMS.register("fission_reactor_designer", () -> new BlockItem(FISSION_REACTOR_DESIGNER.get(), DESIGNER_ITEM_PROPS));
    public static final RegistryObject<MenuType<FissionDesignerContainer>> FISSION_DESIGNER_CONTAINER = CONTAINERS.register("fission_reactor_designer",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FissionDesignerContainer(windowId, data.readBlockPos(), inv)));

    public static void init() {
        ALL_NC_ITEMS.put("fission_reactor_designer", FISSION_REACTOR_DESIGNER_ITEM);
    }
}
