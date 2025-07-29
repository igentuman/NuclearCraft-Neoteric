package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.accelerator.entity.LinearAcceleratorControllerBE;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class LinearAcceleratorPeripheral implements IPeripheral {
    private final LinearAcceleratorControllerBE controller;

    public LinearAcceleratorPeripheral(LinearAcceleratorControllerBE controller)
    {
        this.controller = controller;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "nc_accelerator";
    }


    @Override
    public boolean equals( IPeripheral other )
    {
        return this == other || other instanceof LinearAcceleratorPeripheral && ((LinearAcceleratorPeripheral) other).controller == controller;
    }

    @LuaFunction
    public final boolean isFormed()
    {
        return controller.isCasingValid && controller.isInternalValid;
    }

    @LuaFunction
    public final String getName() {
        return controller.getName();
    }

    @LuaFunction
    public final boolean hasParticle() {
        return controller.hasParticle;
    }

    @LuaFunction
    public final int getEnergyStored()
    {
        return controller.energyStorage().getEnergyStored();
    }

    @LuaFunction
    public final int getTemperature()
    {
        return controller.heat;
    }

    @LuaFunction
    public final int getMaxTemperature()
    {
        return controller.maxTemperature;
    }

    @LuaFunction
    public final Object getHeatBufferInfo()
    {
        Map<String, Object> statsData = new HashMap<String, Object>();
        statsData.put("heat_stored", isMultiblockAssembled() ? controller.heat : 0);
        statsData.put("heat_capacity", isMultiblockAssembled() ? controller.heatMax : 0);
        return statsData;
    }

    @LuaFunction
    public final Object getCoolingInfo()
    {
        Map<String, Object> statsData = new HashMap<String, Object>();
        statsData.put("cooling_fluid",isMultiblockAssembled() ? controller.getFluidTank(2).getFluid().getTranslationKey(): "");
        statsData.put("cooling", isMultiblockAssembled() ? controller.coolingRate : 0);

        return statsData;
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

    private boolean isMultiblockAssembled() {
        return controller.isCasingValid && controller.isInternalValid;
    }

    @LuaFunction
    public final int getHeatRate()
    {
        return controller.heatRate;
    }

    @LuaFunction
    public final Object getParticleInfo()
    {
        return controller.getParticleStack();
    }

    @LuaFunction
    public final boolean isAcceleratorOn() {
        return controller.controllerEnabled;
    }

    public final String getAcceleratorType() {
        return "linear_accelerator";
    }

    public void setEnergyPercentage(double percentage)
    {
        if(percentage < 5)
        {
            percentage = 0;
        }
        if(percentage > 100 )
        {
            percentage = 100;
        }

        controller.redstoneLevel = percentage * 0.15D;
    }

}