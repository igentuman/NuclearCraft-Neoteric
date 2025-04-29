package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.entity.kugelblitz.ChamberTerminalBE;

import javax.annotation.Nonnull;

public class KugelblitzPeripheral implements IPeripheral {
    private final ChamberTerminalBE controller;

    public KugelblitzPeripheral(ChamberTerminalBE controller)
    {
        this.controller = controller;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "nc_kugelblitz";
    }


    @Override
    public boolean equals( IPeripheral other )
    {
        return this == other || other instanceof KugelblitzPeripheral && ((KugelblitzPeripheral) other).controller == controller;
    }

    @LuaFunction
    public final String getName() {
        return controller.getName();
    }

    @LuaFunction
    public final boolean hasRecipe() {
        return controller.hasRecipe();
    }

    @LuaFunction
    public final int getEnergyPerTick()
    {
        return controller.energyPerTick;
    }


    @LuaFunction
    public final int getEnergyStored()
    {
        return controller.energyStorage.getEnergyStored();
    }

}