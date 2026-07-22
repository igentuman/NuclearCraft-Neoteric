package igentuman.nc.setup.entries;

import igentuman.nc.block.crafter.EngineersCrafterBlock;
import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.item.CraftingPatternItem;
import igentuman.nc.registration.ModEntry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static igentuman.nc.setup.ModEntries.ENTRIES;
import static igentuman.nc.setup.Registers.*;

/** Registers the Engineer's Crafting Table (block + BE + terminal/encoder menus) and the Crafting Pattern item. */
public class Crafter {

    public static final List<ModEntry> CRAFTER_ENTRIES = new ArrayList<>();

    public static final DeferredItem<Item> CRAFTING_PATTERN =
            ITEMS.register("crafting_pattern", () -> new CraftingPatternItem(new Item.Properties()));

    public static final DeferredBlock<Block> ENGINEERS_CRAFTING_TABLE_BLOCK =
            BLOCKS.register("engineers_crafting_table", () -> new EngineersCrafterBlock(props()));

    public static final DeferredItem<Item> ENGINEERS_CRAFTING_TABLE_ITEM =
            ITEMS.register("engineers_crafting_table",
                    () -> new BlockItem(ENGINEERS_CRAFTING_TABLE_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EngineersCrafterBE>> ENGINEERS_CRAFTING_TABLE_BE =
            BLOCK_ENTITIES.register("engineers_crafting_table",
                    () -> BlockEntityType.Builder.of(EngineersCrafterBE::new, ENGINEERS_CRAFTING_TABLE_BLOCK.get()).build(null));

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<MenuType<?>, MenuType<EngineersCrafterContainer>> ENGINEERS_CRAFTING_TABLE_MENU =
            (DeferredHolder<MenuType<?>, MenuType<EngineersCrafterContainer>>) (DeferredHolder<?, ?>)
                    CONTAINERS.register("engineers_crafting_table", () -> IMenuTypeExtension.create(
                            (IContainerFactory<EngineersCrafterContainer>)
                                    (id, inv, buf) -> new EngineersCrafterContainer(id, buf.readBlockPos(), inv)));

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<MenuType<?>, MenuType<EngineersEncoderContainer>> ENGINEERS_ENCODER_MENU =
            (DeferredHolder<MenuType<?>, MenuType<EngineersEncoderContainer>>) (DeferredHolder<?, ?>)
                    CONTAINERS.register("engineers_encoder", () -> IMenuTypeExtension.create(
                            (IContainerFactory<EngineersEncoderContainer>)
                                    (id, inv, buf) -> new EngineersEncoderContainer(id, buf.readBlockPos(), inv)));

    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(2.5f)
                .requiresCorrectToolForDrops();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void crafter() {
        ModEntry pattern = new ModEntry("crafting_pattern", null, CRAFTING_PATTERN, null, null, false, null, null,
                null, null, null, null, null, 0, null, null, Set.of(), Set.of());
        ENTRIES.put("crafting_pattern", pattern);
        CRAFTER_ENTRIES.add(pattern);

        DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> beHolder =
                (DeferredHolder<BlockEntityType<?>, BlockEntityType<?>>) (DeferredHolder) ENGINEERS_CRAFTING_TABLE_BE;
        ModEntry table = new ModEntry("engineers_crafting_table", ENGINEERS_CRAFTING_TABLE_BLOCK,
                ENGINEERS_CRAFTING_TABLE_ITEM, null, beHolder, false, null, null,
                null, null, null, null, null, 0, null, null, Set.of(), Set.of());
        ENTRIES.put("engineers_crafting_table", table);
        CRAFTER_ENTRIES.add(table);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        BlockEntityType<EngineersCrafterBE> type = ENGINEERS_CRAFTING_TABLE_BE.get();
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type,
                (be, side) -> be instanceof EngineersCrafterBE crafter ? crafter.getItemHandler(side) : null);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type,
                (be, side) -> be instanceof EngineersCrafterBE crafter ? crafter.getEnergyHandler(side) : null);
    }
}
