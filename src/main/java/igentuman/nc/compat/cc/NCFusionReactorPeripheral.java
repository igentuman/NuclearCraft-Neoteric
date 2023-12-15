package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.entity.fusion.FusionCoreBE;

import javax.annotation.Nonnull;

public class NCFusionReactorPeripheral implements IPeripheral {
    private final FusionCoreBE<?> reactor;

    public NCFusionReactorPeripheral(FusionCoreBE<?> processorBE)
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
        return this == other || other instanceof NCFusionReactorPeripheral && ((NCFusionReactorPeripheral) other).reactor == reactor;
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
        return reactor.rfAmplificationRatio = Math.min(100, Math.max(amplification, 1));
    }

    @LuaFunction
    public final int getEnergyStored()
    {
        return reactor.energyStorage.getEnergyStored();
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