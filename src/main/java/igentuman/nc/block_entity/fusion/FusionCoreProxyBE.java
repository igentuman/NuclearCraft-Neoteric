package igentuman.nc.block_entity.fusion;

import igentuman.nc.block_entity.MultiblockPortBE;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** One of the 26 invisible cells forming the fusion core's 3x3x3 cage. Proxies item/fluid/energy
 *  capabilities to the controller and exposes the reactor's comparator output plus the
 *  shift-right-click mode toggle - the cage is the reactor's redstone I/O surface (original NCN has
 *  no dedicated fusion port block). */
public class FusionCoreProxyBE extends MultiblockPortBE {

    private int lastComparator = -1;

    public FusionCoreProxyBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
    }

    private FusionReactorControllerBE core() {
        return controller() instanceof FusionReactorControllerBE c ? c : null;
    }

    @Override
    public boolean supportsRedstone() {
        return core() != null;
    }

    @Override
    public String[] redstoneModes() {
        return FusionReactorControllerBE.MODE_KEYS;
    }

    @Override
    public int getRedstoneMode() {
        FusionReactorControllerBE c = core();
        return c != null ? c.redstoneMode : 0;
    }

    @Override
    public int cycleRedstoneMode() {
        FusionReactorControllerBE c = core();
        if (c == null) return 0;
        c.toggleRedstoneMode();
        lastComparator = -1;
        if (level != null) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        return c.redstoneMode;
    }

    @Override
    public int getComparatorOutput() {
        FusionReactorControllerBE c = core();
        return c != null ? c.comparatorSignal(c.redstoneMode) : 0;
    }

    @Override
    public void serverTick() {
        if (level instanceof ServerLevel) {
            FusionReactorControllerBE c = core();
            if (c != null) {
                int signal = c.comparatorSignal(c.redstoneMode);
                if (signal != lastComparator) {
                    lastComparator = signal;
                    level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
                }
            }
        }
        super.serverTick();
    }
}
