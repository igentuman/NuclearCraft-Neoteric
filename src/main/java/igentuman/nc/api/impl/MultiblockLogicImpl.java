package igentuman.nc.api.impl;

import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockLogic;
import igentuman.nc.block_entity.MultiblockPortBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Default multiblock logic that links and unlinks port block entities to the controller on form/break. */
public class MultiblockLogicImpl implements IMultiblockLogic {
    @Override
    public void onFormed(Level level, BlockPos controllerPos, IMultiblockCache cache) {
        long controllerKey = controllerPos.asLong();
        for (long key : cache.getStructurePositions()) {
            if (key == controllerKey) continue;
            BlockPos pos = BlockPos.of(key);
            BlockEntity be = cache.getBlockEntity(level, pos);
            if (be instanceof MultiblockPortBE part) {
                part.setControllerPos(controllerPos);
            }
        }
    }

    @Override
    public void onBroken(Level level, BlockPos controllerPos, IMultiblockCache cache) {
        long controllerKey = controllerPos.asLong();
        for (long key : cache.getStructurePositions()) {
            if (key == controllerKey) continue;
            BlockPos pos = BlockPos.of(key);
            BlockEntity be = cache.getBlockEntity(level, pos);
            if (be instanceof MultiblockPortBE part) {
                part.setControllerPos(null);
            }
        }
    }

    @Override
    public void tickServer(Level level, BlockPos controllerPos, IMultiblockCache cache) {

    }

    @Override
    public void tickClient(Level level, BlockPos controllerPos) {

    }
}
