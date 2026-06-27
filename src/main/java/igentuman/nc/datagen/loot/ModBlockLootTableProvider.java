package igentuman.nc.datagen.loot;

import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;

public class ModBlockLootTableProvider  extends BlockLootSubProvider {

    public ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.hasBlock()) {
                dropSelf(entry.block().get());
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
