package igentuman.nc.setup.registration;

import igentuman.nc.block.crafter.EngineersCrafterBlock;
import igentuman.nc.block.crafter.entity.EngineersCrafterBE;
import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.item.CraftingPatternItem;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.setup.registration.CreativeTabs.NC_ITEMS_TAB;
import static igentuman.nc.setup.registration.NCBlocks.fromBlock;
import static igentuman.nc.setup.registration.Registries.*;

public class NCCrafter {

    public static final RegistryObject<Item> CRAFTING_PATTERN = ITEMS.register("crafting_pattern", () -> new CraftingPatternItem(new Item.Properties().tab(NC_ITEMS_TAB)));
    public static final RegistryObject<Block> ENGINEERS_CRAFTING_TABLE_BLOCK = BLOCKS.register("engineers_crafting_table", EngineersCrafterBlock::new);
    public static final RegistryObject<Item> ENGINEERS_CRAFTING_TABLE_ITEM = fromBlock(ENGINEERS_CRAFTING_TABLE_BLOCK);
    public static final RegistryObject<BlockEntityType<EngineersCrafterBE>> ENGINEERS_CRAFTING_TABLE_BE = BLOCK_ENTITIES.register("engineers_crafting_table",
            () -> BlockEntityType.Builder.of(EngineersCrafterBE::new, ENGINEERS_CRAFTING_TABLE_BLOCK.get()).build(null));
    public static final RegistryObject<MenuType<EngineersCrafterContainer>> ENGINEERS_CRAFTING_TABLE_CONTAINER = CONTAINERS.register("engineers_crafting_table",
            () -> IForgeMenuType.create((windowId, inv, data) -> new EngineersCrafterContainer(windowId, data.readBlockPos(), inv)));
    public static final RegistryObject<MenuType<EngineersEncoderContainer>> ENGINEERS_ENCODER_CONTAINER = CONTAINERS.register("engineers_encoder",
            () -> IForgeMenuType.create((windowId, inv, data) -> new EngineersEncoderContainer(windowId, data.readBlockPos(), inv)));

    public static void init() {
    }
}
