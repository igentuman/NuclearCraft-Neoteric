package igentuman.nc.block_entity.turbine;

import igentuman.nc.block.turbine.TurbineBladeBlock;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.turbine.TurbineCache;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TurbineRotorBE extends GlobalBlockEntity {

    @NBTField
    public float rotationSpeed = 0f;
    @NBTField
    public boolean turbineFormed = false;

    public TurbineRotorBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Override
    public void serverTick() {
        if (level instanceof ServerLevel serverLevel) {
            boolean formed = false;
            float speed = 0f;
            BlockPos ctrl = MultiblockHandler.getControllerForPos(serverLevel, worldPosition);
            if (ctrl != null) {
                MultiblockHandler.MultiblockInstance inst = MultiblockHandler.getInstance(serverLevel, ctrl);
                if (inst != null && inst.formed && inst.cache instanceof TurbineCache tc) {
                    formed = true;
                    speed = tc.rotationSpeed;
                }
            }
            if (formed != turbineFormed) {
                setBladesHidden(serverLevel, formed);
            }
            if (formed != turbineFormed || Math.abs(speed - rotationSpeed) > 0.001f) {
                turbineFormed = formed;
                rotationSpeed = speed;
                markDirty();
            }
        }
        super.serverTick();
    }

    private void setBladesHidden(ServerLevel level, boolean hidden) {
        Direction.Axis axis = getBlockState().getValue(TurbineRotorBlock.FACING).getAxis();
        for (Direction dir : Direction.values()) {
            if (dir.getAxis() == axis) continue;
            BlockPos.MutableBlockPos p = worldPosition.mutable();
            while (true) {
                p.move(dir);
                BlockState s = level.getBlockState(p);
                if (!(s.getBlock() instanceof TurbineBladeBlock)) break;
                if (s.getValue(TurbineBladeBlock.HIDDEN) != hidden) {
                    level.setBlock(p.immutable(), s.setValue(TurbineBladeBlock.HIDDEN, hidden), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public boolean isTurbineFormed() {
        return turbineFormed;
    }
}
