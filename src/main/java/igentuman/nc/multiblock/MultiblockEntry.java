package igentuman.nc.multiblock;

import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockLogic;
import igentuman.nc.api.multiblock.IMultiblockValidator;
import igentuman.nc.api.impl.MultiblockCacheImpl;
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

public class MultiblockEntry {

    private final String name;
    private final Supplier<IMultiblockValidator> validatorSupplier;
    private final Supplier<IMultiblockLogic> logicSupplier;
    private final Supplier<IMultiblockCache> cacheSupplier;
    private final List<Supplier<Block>> requiredBlocks;
    private final ModEntry controllerEntry;
    private final List<ModEntry> portEntries;

    public MultiblockEntry(String name,
                           Supplier<IMultiblockValidator> validatorSupplier,
                           Supplier<IMultiblockLogic> logicSupplier,
                           Supplier<IMultiblockCache> cacheSupplier,
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
                           Supplier<IMultiblockValidator> validatorSupplier,
                           Supplier<IMultiblockLogic> logicSupplier,
                           Supplier<IMultiblockCache> cacheSupplier,
                           List<Supplier<Block>> requiredBlocks) {
        this(name, validatorSupplier, logicSupplier, cacheSupplier, requiredBlocks, null, Collections.emptyList());
    }

    public MultiblockEntry(String name,
                           Supplier<IMultiblockValidator> validatorSupplier,
                           Supplier<IMultiblockLogic> logicSupplier,
                           Supplier<IMultiblockCache> cacheSupplier) {
        this(name, validatorSupplier, logicSupplier, cacheSupplier, Collections.emptyList(), null, Collections.emptyList());
    }

    public static MultiblockEntry of(String name,
                                     Supplier<IMultiblockValidator> validator,
                                     Supplier<IMultiblockLogic> logic) {
        return new MultiblockEntry(name, validator, logic, MultiblockCacheImpl::new);
    }

    public String name() { return name; }
    public Supplier<IMultiblockValidator> validatorSupplier() { return validatorSupplier; }
    public Supplier<IMultiblockLogic> logicSupplier() { return logicSupplier; }
    public Supplier<IMultiblockCache> cacheSupplier() { return cacheSupplier; }
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
