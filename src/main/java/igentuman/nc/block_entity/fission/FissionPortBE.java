package igentuman.nc.block_entity.fission;

import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.block_entity.RedstoneModeController;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Fission reactor port: adds redstone signal modes on top of the shared capability-proxy port. */
public class FissionPortBE extends MultiblockPortBE {

    public static final int MODE_NONE = 0;
    public static final int MODE_ENERGY = 1;
    public static final int MODE_HEAT = 2;
    public static final int MODE_PROGRESS = 3;
    public static final int MODE_ITEMS = 4;
    public static final int MODE_SWITCH = 5;
    public static final int MODE_MODERATOR = 6;
    public static final int MODE_COUNT = 7;
    public static final String[] MODE_KEYS = {"none", "energy", "heat", "progress", "items", "switch", "moderator"};

    @NBTField(syncToClient = true)
    public int redstoneMode = MODE_NONE;
    private int lastComparator = 0;

    public FissionPortBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
    }

    /** True when this port's controller exposes redstone signal modes. Server side only. */
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

    /** Advances to the next redstone mode, releasing any control the previous mode held so a port
     *  left in SWITCH/MODERATOR doesn't strand the reactor disabled/throttled. Returns the new mode. */
    @Override
    public int cycleRedstoneMode() {
        int previous = redstoneMode;
        redstoneMode = (redstoneMode + 1) % MODE_COUNT;
        if (controller() instanceof RedstoneModeController rmc) {
            if (previous == MODE_SWITCH) rmc.applyRedstoneInput(MODE_SWITCH, 15);
            else if (previous == MODE_MODERATOR) rmc.applyRedstoneInput(MODE_MODERATOR, 15);
        }
        lastComparator = -1;
        markDirty();
        if (level != null) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        return redstoneMode;
    }

    /** Comparator strength (0-15). Non-output modes report 0. Server side only. */
    public int getComparatorOutput() {
        if (redstoneMode == MODE_NONE || redstoneMode == MODE_SWITCH || redstoneMode == MODE_MODERATOR) return 0;
        return controller() instanceof RedstoneModeController rmc ? rmc.comparatorSignal(redstoneMode) : 0;
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel) || redstoneMode == MODE_NONE) return;
        if (!(controller() instanceof RedstoneModeController rmc)) return;
        if (redstoneMode == MODE_SWITCH || redstoneMode == MODE_MODERATOR) {
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
