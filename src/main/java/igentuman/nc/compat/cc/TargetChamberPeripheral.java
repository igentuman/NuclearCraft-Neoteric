package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.entity.accelerator.TargetChamberControllerBE;

import javax.annotation.Nonnull;

public class TargetChamberPeripheral implements IPeripheral {
    private final TargetChamberControllerBE chamber;

    public TargetChamberPeripheral(TargetChamberControllerBE be)
    {
        this.chamber = be;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "nc_target_chamber";
    }


    @Override
    public boolean equals( IPeripheral other )
    {
        return this == other || other instanceof TargetChamberPeripheral && ((TargetChamberPeripheral) other).chamber == chamber;
    }

    @LuaFunction
    public final boolean isFormed()
    {
        return chamber.isCasingValid && chamber.isInternalValid;
    }

    @LuaFunction
    public final String getName() {
        return chamber.getName();
    }

    @LuaFunction
    public final boolean hasRecipe() {
        return chamber.hasRecipe();
    }
    @LuaFunction
    public final int getDepletionProgress()
    {
        return (int) (chamber.getDepletionProgress()*100);
    }

    @LuaFunction
    public final double getMaxHeatCapacity()
    {
        return chamber.getMaxHeat();
    }

    @LuaFunction
    public final void enableReactor()
    {
        chamber.disableForceShutdown();
    }

    @LuaFunction
    public final void disableReactor()
    {
        chamber.forceShutdown();
    }

    @LuaFunction
    public final int getEnergyPerTick()
    {
        return chamber.energyPerTick;
    }

    @LuaFunction
    public final int getEnergyStored()
    {
        return chamber.energyStorage().getEnergyStored();
    }

    @LuaFunction
    public final int getHeatStored()
    {
        return (int) chamber.heat;
    }

    @LuaFunction
    public final void voidFuel()
    {
        chamber.voidFuel();
    }

    @LuaFunction
    public final Object[] getFuelInSlot()
    {
        return chamber.getFuel();
    }

}
