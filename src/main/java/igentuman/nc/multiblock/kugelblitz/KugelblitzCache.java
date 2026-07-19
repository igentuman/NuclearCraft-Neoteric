package igentuman.nc.multiblock.kugelblitz;

import igentuman.nc.api.impl.MultiblockCacheImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class KugelblitzCache extends MultiblockCacheImpl {

    public volatile int transformers;
    public volatile int fluxRegulators;
    public volatile int stabilizers;
    public volatile long center = Long.MIN_VALUE;

    public void resetStats() {
        transformers = 0;
        fluxRegulators = 0;
        stabilizers = 0;
        center = Long.MIN_VALUE;
    }

    public boolean hasCenter() {
        return center != Long.MIN_VALUE;
    }

    public BlockPos centerPos() {
        return BlockPos.of(center);
    }

    @Override
    public void clear() {
        super.clear();
        resetStats();
    }

    @Override
    public void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveNbt(tag, registries);
        tag.putInt("transformers", transformers);
        tag.putInt("fluxRegulators", fluxRegulators);
        tag.putInt("stabilizers", stabilizers);
        tag.putLong("center", center);
    }

    @Override
    public void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadNbt(tag, registries);
        transformers = tag.getInt("transformers");
        fluxRegulators = tag.getInt("fluxRegulators");
        stabilizers = tag.getInt("stabilizers");
        center = tag.contains("center") ? tag.getLong("center") : Long.MIN_VALUE;
    }
}
