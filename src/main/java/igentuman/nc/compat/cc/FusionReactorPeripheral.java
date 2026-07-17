package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.fusion.entity.FusionCoreBE;

import javax.annotation.Nonnull;

public class FusionReactorPeripheral implements IPeripheral {
    private final FusionCoreBE reactor;

    public FusionReactorPeripheral(FusionCoreBE processorBE)
    {
        this.reactor = processorBE;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "nc_fusion_reactor_core";
    }


    @Override
    public boolean equals( IPeripheral other )
    {
        return this == other || other instanceof FusionReactorPeripheral && ((FusionReactorPeripheral) other).reactor == reactor;
    }

    @LuaFunction
    public final boolean isFormed()
    {
        return reactor.isCasingValid && reactor.isInternalValid;
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
    public final int setRFAmplification(int amplification)
    {
        reactor.rfAmplificationRatio = Math.min(100, Math.max(amplification, 1));
        reactor.setChanged();
        return reactor.rfAmplificationRatio;
    }

    @LuaFunction
    public final int getEnergyStored()
    {
        return reactor.energyStorage().getEnergyStored();
    }

    @LuaFunction
    public final double getPlasmaStability()
    {
        return reactor.getControlPartsEfficiency();
    }


    @LuaFunction
    public final int getHeatStored()
    {
        return (int) reactor.reactorHeat;
    }

    @LuaFunction
    public final void voidFuel()
    {
        reactor.voidFuel();
    }

}