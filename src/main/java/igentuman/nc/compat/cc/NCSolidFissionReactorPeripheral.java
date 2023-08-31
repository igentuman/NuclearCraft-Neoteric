package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.entity.fission.FissionControllerBE;

import javax.annotation.Nonnull;

public class NCSolidFissionReactorPeripheral implements IPeripheral {
    private final FissionControllerBE<?> reactor;

    public NCSolidFissionReactorPeripheral(FissionControllerBE<?> processorBE)
    {
        this.reactor = processorBE;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "nc_fission_reactor_controller";
    }


    @Override
    public boolean equals( IPeripheral other )
    {
        return this == other || other instanceof NCSolidFissionReactorPeripheral && ((NCSolidFissionReactorPeripheral) other).reactor == reactor;
    }

    @LuaFunction
    public final String getName() {
        return reactor.getName();
    }

    @LuaFunction
    public final boolean hasRecipe() {
        return reactor.hasRecipe();
    }

    @LuaFunction
    public final int getDepletionProgress()
    {
        return (int) (reactor.getDepletionProgress()*100);
    }


    @LuaFunction
    public final double getMaxHeatCapacity()
    {
        return reactor.getMaxHeat();
    }

    @LuaFunction
    public final void enableReactor()
    {
        reactor.disableForceShutdown();
    }

    @LuaFunction
    public final void disableReactor()
    {
        reactor.forceShutdown();
    }

    @LuaFunction
    public final int getEnergyPerTick()
    {
        return reactor.energyPerTick;
    }


    @LuaFunction
    public final int getEnergyStored()
    {
        return reactor.energyStorage.getEnergyStored();
    }

    @LuaFunction
    public final double getHeatMultiplier()
    {
        return reactor.heatMultiplier;
    }

    @LuaFunction
    public final int getModeratorsCount()
    {
        return reactor.moderatorsCount;
    }

    @LuaFunction
    public final int getHeatSinksCount()
    {
        return reactor.heatSinksCount;
    }

    @LuaFunction
    public final int getFuelCellsCount()
    {
        return reactor.fuelCellsCount;
    }

    @LuaFunction
    public final int getCooling()
    {
        return (int) reactor.coolingPerTick();
    }

    @LuaFunction
    public final int getHeat()
    {
        return (int) reactor.heatPerTick();
    }

    @LuaFunction
    public final int getHeatStored()
    {
        return (int) reactor.heat;
    }

    @LuaFunction
    public final void voidFuel()
    {
        reactor.voidFuel();
    }

    @LuaFunction
    public final Object[] getFuelInSlot()
    {
        return reactor.getFuel();
    }

}
