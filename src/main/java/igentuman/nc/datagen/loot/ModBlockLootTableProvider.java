package igentuman.nc.datagen.loot;

import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.Collections;

/** Generates block loot tables for blocks, ores, storage blocks, and heat sinks. */
public class ModBlockLootTableProvider  extends BlockLootSubProvider {

    public ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        for (HeatSinkEntry entry : ModEntries.HEAT_SINKS.values()) {
            dropSelf(entry.block().get());
        }

        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.hasBlock()) {
                if (entry.name().equals("fusion_reactor_core_proxy") || entry.name().equals("expl_proxy")) {
                    add(entry.block().get(), LootTable.lootTable()); // auto-placed body cell - never drops
                } else {
                    dropSelf(entry.block().get());
                }
            }
            if (entry.materialEntry() != null) {
                if (entry.materialEntry().hasOre()) {
                    MaterialEntry mat = entry.materialEntry();
                    if (mat.hasRawOre()) {
                        add(mat.oreBlock().get(), createOreDrop(mat.oreBlock().get(), mat.rawOre().get()));
                    } else if (mat.hasGem()) {
                        add(mat.oreBlock().get(), createOreDrop(mat.oreBlock().get(), mat.gem().get()));
                    } else {
                        dropSelf(mat.oreBlock().get());
                    }
                }
                if (entry.materialEntry().hasBlock()) {
                    dropSelf(entry.materialEntry().storageBlock().get());
                }
            }
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        ArrayList<Block> blocks = new ArrayList<>();
        for (HeatSinkEntry entry : ModEntries.HEAT_SINKS.values()) {
            blocks.add(entry.block().get());
        }
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.hasBlock()) {
                blocks.add(entry.block().get());
            }
            if (entry.materialEntry() != null) {
                if (entry.materialEntry().hasOre()) {
                    blocks.add(entry.materialEntry().oreBlock().get());
                }
                if (entry.materialEntry().storageBlock() != null) {
                    blocks.add(entry.materialEntry().storageBlock().get());
                }
            }
        }
        return blocks;
    }
}
