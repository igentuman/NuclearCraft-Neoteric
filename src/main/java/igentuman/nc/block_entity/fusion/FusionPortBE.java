package igentuman.nc.block_entity.fusion;

import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.block_entity.RedstoneModeController;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Fusion reactor port: comparator output modes (energy/heat/efficiency/charge) plus redstone input
 *  modes (amplification ratio, on/off switch) proxied to the controller. */
public class FusionPortBE extends MultiblockPortBE {

    public static final int MODE_NONE = 0;
    public static final int MODE_ENERGY = 1;
    public static final int MODE_HEAT = 2;
    public static final int MODE_EFFICIENCY = 3;
    public static final int MODE_CHARGE = 4;
    public static final int MODE_AMPLIFICATION = 5;
    public static final int MODE_SWITCH = 6;
    public static final int MODE_COUNT = 7;
    public static final String[] MODE_KEYS = {"none", "energy", "heat", "efficiency", "charge", "amplification", "switch"};

    @NBTField(syncToClient = true)
    public int redstoneMode = MODE_NONE;
    private int lastComparator = 0;

    public FusionPortBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
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
        int previous = redstoneMode;
        redstoneMode = (redstoneMode + 1) % MODE_COUNT;
        if (controller() instanceof RedstoneModeController rmc) {
            if (previous == MODE_SWITCH) rmc.applyRedstoneInput(MODE_SWITCH, 15);
            else if (previous == MODE_AMPLIFICATION) rmc.applyRedstoneInput(MODE_AMPLIFICATION, 15);
        }
        lastComparator = -1;
        markDirty();
        if (level != null) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        return redstoneMode;
    }

    @Override
    public int getComparatorOutput() {
        if (redstoneMode == MODE_NONE || redstoneMode == MODE_SWITCH || redstoneMode == MODE_AMPLIFICATION) return 0;
        return controller() instanceof RedstoneModeController rmc ? rmc.comparatorSignal(redstoneMode) : 0;
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel) || redstoneMode == MODE_NONE) return;
        if (!(controller() instanceof RedstoneModeController rmc)) return;
        if (redstoneMode == MODE_SWITCH || redstoneMode == MODE_AMPLIFICATION) {
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
