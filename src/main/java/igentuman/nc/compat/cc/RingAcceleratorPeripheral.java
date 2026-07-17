package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.accelerator.entity.RingAcceleratorControllerBE;

import java.util.HashMap;
import java.util.Map;

public class RingAcceleratorPeripheral  implements IPeripheral {
    private final RingAcceleratorControllerBE controller;

    public RingAcceleratorPeripheral(RingAcceleratorControllerBE controller)
    {
        this.controller = controller;
    }

    @Override
    public String getType() {
        return "ring_accelerator";
    }

    @Override
    public boolean equals( IPeripheral other )
    {
        return this == other || other instanceof RingAcceleratorPeripheral && ((RingAcceleratorPeripheral) other).controller == controller;
    }


    @LuaFunction
    public final boolean isFormed()
    {
        return isMultiblockAssembled();
    }

    @LuaFunction
    public final String getName() {
        return controller.getName();
    }

    @LuaFunction
    public final boolean hasParticle() {
        return isMultiblockAssembled() && controller.hasParticle;
    }

    @LuaFunction
    public final int getEnergyStored()
    {
        return isMultiblockAssembled() ? controller.energyStorage().getEnergyStored() : 0;
    }

    @LuaFunction
    public final int getMinEnergy()
    {
        return isMultiblockAssembled() ? controller.getMinEnergy() : 0;
    }

    @LuaFunction
    public final int getTemperature()
    {
        return isMultiblockAssembled() ? controller.getTemperature() : 0;
    }

    @LuaFunction
    public int getMaxTemperature()
    {
        return isMultiblockAssembled() ? controller.maxTemperature : 0;
    }

    @LuaFunction
    public Object getHeatBufferInfo()
    {
        Map<String, Object> statsData = new HashMap<String, Object>();
        statsData.put("heat_stored", isMultiblockAssembled() ? controller.heatStored : 0);
        statsData.put("heat_capacity", isMultiblockAssembled() ? controller.heatCapacity : 0);
        return statsData;
    }

    @LuaFunction
    public Object getCoolingInfo()
    {
        Map<String, Object> statsData = new HashMap<String, Object>();
        statsData.put("cooling_fluid",isMultiblockAssembled() ? controller.getFluidTank(2).getFluid().getTranslationKey(): "");
        statsData.put("cooling", isMultiblockAssembled() ? controller.coolingRate : 0);

        return statsData;
    }

    protected boolean isMultiblockAssembled() {
        return controller.isCasingValid && controller.isInternalValid;
    }

    @LuaFunction
    public int getHeatRate()
    {
        return isMultiblockAssembled() ? controller.heatRate : 0;
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

    @LuaFunction
    public boolean isAcceleratorOn() {
        return isMultiblockAssembled() && controller.controllerEnabled;
    }

    @LuaFunction(mainThread = true)
    public void setEnergyPercentage(double percentage)
    {
        if(!isMultiblockAssembled()) return;
        if(percentage < 5)
        {
            percentage = 0;
        }
        if(percentage > 100 )
        {
            percentage = 100;
        }

        controller.externalControlled = true;
        controller.isControlledByComputer = true;
        controller.analogSignal = (byte) (percentage * 0.15D);
        controller.accelerationEnergy = percentage / 100D;
    }

    @LuaFunction(mainThread = true)
    public void releaseControl()
    {
        controller.externalControlled = false;
        controller.isControlledByComputer = false;
    }

    @LuaFunction
    public Object getStats()
    {
        Map<String, Object> statsData = new HashMap<String, Object>();
        statsData.put("accelerating_voltage", isMultiblockAssembled() ? controller.acceleratingVoltage : 0);
        statsData.put("dipole_strength", isMultiblockAssembled() ? controller.dipoleStrength : 0);
        statsData.put("quadrupole_strength", isMultiblockAssembled() ? controller.quadStrength : 0);
        statsData.put("input_particle_min_energy", isMultiblockAssembled() ? controller.getMinEnergy() : 0);

        return statsData;
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