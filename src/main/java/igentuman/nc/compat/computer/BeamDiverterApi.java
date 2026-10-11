package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.block_entity.accelerator.BeamDiverterControllerBE;
import li.cil.oc2.api.bus.device.object.Callback;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BeamDiverterApi extends BeamControllerApi<BeamDiverterControllerBE> {

    public BeamDiverterApi(BeamDiverterControllerBE be, ComputerErrors errors) {
        super(be, errors);
    }

    @LuaFunction
    @Callback
    public final @Nullable Map<String, Object> getParticleInfo() {
        return particleInfo(be.particleTypeId, be.particleAmount, be.particleEnergyKeV, be.particleFocusScaled / 10_000D);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final boolean selectOutput(int channel) {
        return be.setSelectedOutputChannel(channel - 1);
    }

    @LuaFunction
    @Callback
    public final int getSelectedOutput() {
        return be.selectedOutputChannel + 1;
    }

    @LuaFunction
    @Callback
    public final boolean isIncompatibleTurn() {
        return be.incompatibleTurn;
    }
}
