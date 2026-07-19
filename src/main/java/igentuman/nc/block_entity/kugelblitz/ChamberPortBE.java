package igentuman.nc.block_entity.kugelblitz;

import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.block_entity.RedstoneModeController;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ChamberPortBE extends MultiblockPortBE {

    public static final int MODE_NONE = 0;
    public static final int MODE_ENERGY = 1;
    public static final int MODE_MASS = 2;
    public static final int MODE_PROGRESS = 3;
    public static final int MODE_ITEMS = 4;
    public static final int MODE_FREQUENCY = 5;
    public static final int MODE_TRANSFORMATION_ENERGY_RATE = 6;
    public static final int MODE_COUNT = 7;
    public static final String[] MODE_KEYS = {"none", "energy", "mass", "progress", "items", "frequency", "transformation_energy_rate"};

    @NBTField(syncToClient = true)
    public int redstoneMode = MODE_NONE;
    private int lastComparator = 0;

    public ChamberPortBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
    }

    @Override
    public boolean supportsRedstone() {
        return controller() instanceof RedstoneModeController;
    }

    @Override
    public String[] redstoneModes() {
        return MODE_KEYS;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public int cycleRedstoneMode() {
        redstoneMode = (redstoneMode + 1) % MODE_COUNT;
        lastComparator = -1;
        markDirty();
        if (level != null) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        return redstoneMode;
    }

    @Override
    public int getComparatorOutput() {
        if (redstoneMode == MODE_NONE || redstoneMode == MODE_FREQUENCY || redstoneMode == MODE_TRANSFORMATION_ENERGY_RATE) return 0;
        return controller() instanceof RedstoneModeController rmc ? rmc.comparatorSignal(redstoneMode) : 0;
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel) || redstoneMode == MODE_NONE) return;
        if (!(controller() instanceof RedstoneModeController rmc)) return;
        if (redstoneMode == MODE_FREQUENCY || redstoneMode == MODE_TRANSFORMATION_ENERGY_RATE) {
            rmc.applyRedstoneInput(redstoneMode, level.getBestNeighborSignal(worldPosition));
        } else {
            int signal = rmc.comparatorSignal(redstoneMode);
            if (signal != lastComparator) {
                lastComparator = signal;
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            }
        }
        super.serverTick();
    }
}
