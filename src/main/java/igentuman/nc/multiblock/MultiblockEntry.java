package igentuman.nc.multiblock;

import igentuman.nc.api.multiblock.AbstractMultiblockCache;
import igentuman.nc.api.multiblock.AbstractMultiblockLogic;
import igentuman.nc.api.multiblock.AbstractMultiblockValidator;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.util.MultiblockStructure;
import igentuman.nc.util.MultiblocksProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/** Immutable definition of a multiblock: its validator, logic, cache suppliers, required blocks, controller, and ports. */
public class MultiblockEntry {

    private final String name;
    private final Supplier<? extends AbstractMultiblockValidator<?>> validatorSupplier;
    private final Supplier<? extends AbstractMultiblockLogic<?>> logicSupplier;
    private final Supplier<? extends AbstractMultiblockCache> cacheSupplier;
    private final List<Supplier<Block>> requiredBlocks;
    private final ModEntry controllerEntry;
    private final List<ModEntry> portEntries;

    public MultiblockEntry(String name,
                           Supplier<? extends AbstractMultiblockValidator<?>> validatorSupplier,
                           Supplier<? extends AbstractMultiblockLogic<?>> logicSupplier,
                           Supplier<? extends AbstractMultiblockCache> cacheSupplier,
                           List<Supplier<Block>> requiredBlocks,
                           ModEntry controllerEntry,
                           List<ModEntry> portEntries) {
        this.name = name;
        this.validatorSupplier = validatorSupplier;
        this.logicSupplier = logicSupplier;
        this.cacheSupplier = cacheSupplier;
        this.requiredBlocks = requiredBlocks != null ? List.copyOf(requiredBlocks) : List.of();
        this.controllerEntry = controllerEntry;
        this.portEntries = portEntries != null ? List.copyOf(portEntries) : List.of();
    }

    public MultiblockEntry(String name,
                           Supplier<? extends AbstractMultiblockValidator<?>> validatorSupplier,
                           Supplier<? extends AbstractMultiblockLogic<?>> logicSupplier,
                           Supplier<? extends AbstractMultiblockCache> cacheSupplier,
                           List<Supplier<Block>> requiredBlocks) {
        this(name, validatorSupplier, logicSupplier, cacheSupplier, requiredBlocks, null, Collections.emptyList());
    }

    public MultiblockEntry(String name,
                           Supplier<? extends AbstractMultiblockValidator<?>> validatorSupplier,
                           Supplier<? extends AbstractMultiblockLogic<?>> logicSupplier,
                           Supplier<? extends AbstractMultiblockCache> cacheSupplier) {
        this(name, validatorSupplier, logicSupplier, cacheSupplier, Collections.emptyList(), null, Collections.emptyList());
    }

    public String name() { return name; }
    public Supplier<? extends AbstractMultiblockValidator<?>> validatorSupplier() { return validatorSupplier; }
    public Supplier<? extends AbstractMultiblockLogic<?>> logicSupplier() { return logicSupplier; }
    public Supplier<? extends AbstractMultiblockCache> cacheSupplier() { return cacheSupplier; }
    public List<Supplier<Block>> requiredBlocks() { return requiredBlocks; }
    public ModEntry controllerEntry() { return controllerEntry; }
    public List<ModEntry> portEntries() { return portEntries; }

    /**
     * Returns true if every block referenced by this multiblock resolves to a registered, non-air block.
     */
    public boolean isBuildable() {
        if (requiredBlocks.isEmpty()) return false;
        for (Supplier<Block> sup : requiredBlocks) {
            Block block;
            try {
                block = sup.get();
            } catch (Exception e) {
                return false;
            }
            if (block == null || block == Blocks.AIR) return false;
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
            if (key == null || !BuiltInRegistries.BLOCK.containsKey(key)) return false;
        }
        return true;
    }

    /**
     * Looks up an example structure for this entry. First checks already-loaded structures in
     * {@link MultiblocksProvider}; falls back to direct classpath load from
     * {@code /data/<namespace>/example_structures/<name>.nbt}. Returns null if no file exists.
     */
    public MultiblockStructure getExampleStructure() {
        for (MultiblockStructure s : MultiblocksProvider.getStructures()) {
            ResourceLocation id = s.getId();
            if (id != null && id.getPath().endsWith("/" + name + ".nbt")) return s;
        }
        return MultiblocksProvider.loadStructureFromClasspath(name);
    }
}
