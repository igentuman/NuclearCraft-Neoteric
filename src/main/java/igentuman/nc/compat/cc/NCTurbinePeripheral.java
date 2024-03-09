package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.entity.turbine.TurbineControllerBE;

import javax.annotation.Nonnull;

public class NCTurbinePeripheral implements IPeripheral {
    private final TurbineControllerBE<?> turbine;

    public NCTurbinePeripheral(TurbineControllerBE<?> turbine)
    {
        this.turbine = turbine;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "nc_turbine";
    }


    @Override
    public boolean equals( IPeripheral other )
    {
        return this == other || other instanceof NCTurbinePeripheral && ((NCTurbinePeripheral) other).turbine == turbine;
    }

    @LuaFunction
    public final String getName() {
        return turbine.getName();
    }

    @LuaFunction
    public final boolean hasRecipe() {
        return turbine.hasRecipe();
    }

    @LuaFunction
    public final void enableTurbine()
    {
        turbine.disableForceShutdown();
    }

    @LuaFunction
    public final void disableTurbine()
    {
        turbine.forceShutdown();
    }

    @LuaFunction
    public final int getEnergyPerTick()
    {
        return turbine.energyPerTick;
    }


    @LuaFunction
    public final int getEnergyStored()
    {
        return turbine.energyStorage.getEnergyStored();
    }

}