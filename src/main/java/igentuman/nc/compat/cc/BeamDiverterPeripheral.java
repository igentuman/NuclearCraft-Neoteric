package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.accelerator.entity.RingAcceleratorControllerBE;
import igentuman.nc.block.beam_diverter.entity.BeamDiverterControllerBE;
import igentuman.nc.container.BeamDiverterContainer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class BeamDiverterPeripheral implements IPeripheral {
    private final BeamDiverterControllerBE controller;

    public BeamDiverterPeripheral(BeamDiverterControllerBE controller)
    {
        this.controller = controller;
    }

    @Override
    public boolean equals( IPeripheral other )
    {
        return this == other || other instanceof BeamDiverterPeripheral && ((BeamDiverterPeripheral) other).controller == controller;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "nc_beam_diverter";
    }


    protected boolean isMultiblockAssembled() {
        return controller.isCasingValid && controller.isInternalValid;
    }

    @LuaFunction
    public Object getParticleInfo()
    {
        if(!isMultiblockAssembled() || !controller.hasParticle || controller.getParticleStorage().getClientParticleStack() == null) {
            return null;
        }
        Map<String, Object> particle = new HashMap<String, Object>();
        particle.put("energy", controller.getParticleStorage().getClientParticleStack().getMeanEnergy());
        particle.put("focus", controller.getParticleStorage().getClientParticleStack().getFocus());
        particle.put("amount", controller.getParticleStorage().getClientParticleStack().getAmount());
        particle.put("particle", controller.getParticleStorage().getClientParticleStack().getParticle().getName());
        return particle;
    }


    @LuaFunction(mainThread = true)
    public Object getBeamPortsInfo() {
        if (!isMultiblockAssembled()) {
            return null;
        }
        return controller.getBeamPortsInfo();
    }

    @LuaFunction(mainThread = true)
    public boolean setBeamPortMode(int id, String mode) {
        if (!isMultiblockAssembled()) {
            return false;
        }
        return controller.setBeamPortMode(id, mode);
    }
}