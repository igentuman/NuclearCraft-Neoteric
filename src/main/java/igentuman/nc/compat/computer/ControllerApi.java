package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.multiblock.MultiblockHandler;
import li.cil.oc2.api.bus.device.object.Callback;
import net.minecraft.server.level.ServerLevel;

public class ControllerApi<T extends MultiblockControllerBE> extends MachineApi<T> {

    public ControllerApi(T be, ComputerErrors errors) {
        super(be, errors);
    }

    @LuaFunction
    @Callback
    public final String getMultiblockName() {
        return be.getMultiblockName();
    }

    @LuaFunction
    @Callback
    public final boolean isFormed() {
        return be.formed;
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final int getStructureSize() {
        if (!(be.getLevel() instanceof ServerLevel serverLevel)) return 0;
        MultiblockHandler.MultiblockInstance instance = MultiblockHandler.getInstance(serverLevel, be.getBlockPos());
        if (instance == null || !instance.formed) return 0;
        return instance.cache.getStructurePositions().size();
    }

    protected final void setEnabled(boolean enabled) {
        be.setForceShutdown(!enabled);
    }

    protected final boolean enabled() {
        return !be.forceShutdown;
    }
}
