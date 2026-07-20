package igentuman.nc.setup.entries;

import igentuman.nc.block.energy.EnergyBlock;
import igentuman.nc.block_entity.energy.AbstractEnergyBE;
import igentuman.nc.block_entity.energy.DecayGeneratorBE;
import igentuman.nc.block_entity.energy.RtgBE;
import igentuman.nc.block_entity.energy.SolarPanelBE;
import igentuman.nc.content.energy.EnergyGenDefs;
import igentuman.nc.registration.ModEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static igentuman.nc.setup.ModEntries.ENTRIES;
import static igentuman.nc.setup.Registers.*;

/** Registers the non-GUI energy generator blocks (RTGs, solar panels, decay generator) and their capabilities. */
public class Energy {

    public static final List<ModEntry> ENERGY_ENTRIES = new ArrayList<>();

    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(2f)
                .requiresCorrectToolForDrops();
    }

    @FunctionalInterface
    public interface EnergyBEFactory {
        AbstractEnergyBE create(BlockEntityType<?> type, BlockPos pos, BlockState state, String name);
    }

    public static void energy() {
        for (String name : EnergyGenDefs.RTGS.keySet()) {
            register(name, RtgBE::new);
        }
        for (String name : EnergyGenDefs.SOLAR_PANELS.keySet()) {
            register(name, SolarPanelBE::new);
        }
        register("decay_generator", DecayGeneratorBE::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void register(String name, EnergyBEFactory beFactory) {
        DeferredBlock<Block> block = BLOCKS.register(name, () -> new EnergyBlock(props(), name));
        DeferredItem<Item> item = ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));

        final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>>[] beHolder = new DeferredHolder[1];
        DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<?>> beReg = BLOCK_ENTITIES.register(name,
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> beFactory.create(beHolder[0].get(), pos, state, name),
                        block.get()).build(null));
        beHolder[0] = (DeferredHolder<BlockEntityType<?>, BlockEntityType<?>>) (DeferredHolder) beReg;

        ModEntry entry = new ModEntry(name, block, item, null, beHolder[0], false, null, null, null,
                null, null, null, null, 0, null, null, Set.of(), Set.of());
        ENTRIES.put(name, entry);
        ENERGY_ENTRIES.add(entry);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (ModEntry entry : ENERGY_ENTRIES) {
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, entry.blockEntity().get(),
                    (be, side) -> be instanceof AbstractEnergyBE energy ? energy.getEnergyHandler(side) : null);
        }
    }
}
