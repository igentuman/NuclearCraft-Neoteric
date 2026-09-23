package igentuman.nc.api.multiblock;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.MultiblockPortBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class AbstractMultiblockLogic<C extends AbstractMultiblockCache> implements IMultiblockLogic {

    public void onFormed(ServerLevel level, BlockPos controllerPos, C cache) {
        linkPorts(level, controllerPos, cache.ports());
    }

    public void onBroken(ServerLevel level, BlockPos controllerPos, C cache) {
        unlinkPorts(level, controllerPos, cache.ports());
        resetRuntime();
    }

    public void onPortsChanged(ServerLevel level, BlockPos controllerPos, C cache, Set<Long> removed) {
        unlinkPorts(level, controllerPos, removed);
        linkPorts(level, controllerPos, cache.ports());
    }

    public void tickServer(ServerLevel level, BlockPos controllerPos, C cache) {
    }

    public void resetRuntime() {
    }

    public void saveRuntime(CompoundTag tag, HolderLookup.Provider registries) {
    }

    public void loadRuntime(CompoundTag tag, HolderLookup.Provider registries) {
    }

    @SuppressWarnings("unchecked")
    public final void onPortsChangedUnchecked(ServerLevel level, BlockPos controllerPos,
                                              AbstractMultiblockCache cache, Set<Long> removed) {
        onPortsChanged(level, controllerPos, (C) cache, removed);
    }

    protected final void linkPorts(ServerLevel level, BlockPos controllerPos, Set<Long> ports) {
        BlockEntity controllerEntity = level.getBlockEntity(controllerPos);
        for (long key : ports) {
            BlockPos pos = BlockPos.of(key);
            MultiblockPortBE port = portAt(level, pos);
            if (port == null) continue;
            if (!controllerPos.equals(port.getControllerPos())) {
                port.setControllerPos(controllerPos);
                level.invalidateCapabilities(pos);
            }
            if (controllerEntity instanceof MultiblockControllerBE controller) {
                port.configureFromController(controller);
            }
        }
    }

    protected final void unlinkPorts(ServerLevel level, BlockPos controllerPos, Set<Long> ports) {
        for (long key : ports) {
            BlockPos pos = BlockPos.of(key);
            MultiblockPortBE port = portAt(level, pos);
            if (port == null || !controllerPos.equals(port.getControllerPos())) continue;
            port.setControllerPos(null);
            port.clearStructureConfiguration();
            level.invalidateCapabilities(pos);
        }
    }

    @Nullable
    private static MultiblockPortBE portAt(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return null;
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof MultiblockPortBE port ? port : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void onFormed(Level level, BlockPos controllerPos, IMultiblockCache cache) {
        if (level instanceof ServerLevel serverLevel && cache instanceof AbstractMultiblockCache typed) {
            onFormed(serverLevel, controllerPos, (C) typed);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void onBroken(Level level, BlockPos controllerPos, IMultiblockCache cache) {
        if (level instanceof ServerLevel serverLevel && cache instanceof AbstractMultiblockCache typed) {
            onBroken(serverLevel, controllerPos, (C) typed);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void tickServer(Level level, BlockPos controllerPos, IMultiblockCache cache) {
        if (level instanceof ServerLevel serverLevel && cache instanceof AbstractMultiblockCache typed) {
            tickServer(serverLevel, controllerPos, (C) typed);
        }
    }

    @Override
    public void tickClient(Level level, BlockPos controllerPos) {
    }
}
