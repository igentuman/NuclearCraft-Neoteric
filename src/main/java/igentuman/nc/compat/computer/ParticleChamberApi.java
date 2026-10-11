package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.block_entity.particle.ParticleChamberControllerBE;
import li.cil.oc2.api.bus.device.object.Callback;

import java.util.HashMap;
import java.util.Map;

public class ParticleChamberApi extends BeamControllerApi<ParticleChamberControllerBE> {

    public ParticleChamberApi(ParticleChamberControllerBE be, ComputerErrors errors) {
        super(be, errors);
    }

    @LuaFunction
    @Callback
    public final boolean isControllerEnabled() {
        return enabled();
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void enableController() {
        setEnabled(true);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void disableController() {
        setEnabled(false);
    }

    @LuaFunction
    @Callback
    public final Map<Integer, Map<String, Object>> getInputParticlesInfo() {
        return channels(be.inputParticleRegId, be.inputParticleAmount, be.inputParticleEnergy, be.inputParticleFocus);
    }

    @LuaFunction
    @Callback
    public final Map<Integer, Map<String, Object>> getOutputParticlesInfo() {
        return channels(be.outputParticleRegId, be.outputParticleAmount, be.outputParticleEnergy, be.outputParticleFocus);
    }

    private static Map<Integer, Map<String, Object>> channels(int[] regId, int[] amount, int[] energy, int[] focus) {
        Map<Integer, Map<String, Object>> result = new HashMap<>();
        for (int i = 0; i < regId.length; i++) {
            Map<String, Object> info = particleInfo(regId[i], amount[i], energy[i], focus[i] / 1000D);
            if (info != null) result.put(i + 1, info);
        }
        return result;
    }
}
