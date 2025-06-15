package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.entity.accelerator.ThoroidalAcceleratorControllerBE;

import javax.annotation.Nonnull;

public class ThoroidalAcceleratorPeripheral implements IPeripheral {
    private final ThoroidalAcceleratorControllerBE controller;

    public ThoroidalAcceleratorPeripheral(ThoroidalAcceleratorControllerBE controller)
    {
        this.controller = controller;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "nc_thoroidal_accelerator";
    }


    @Override
    public boolean equals( IPeripheral other )
    {
        return this == other || other instanceof ThoroidalAcceleratorPeripheral && ((ThoroidalAcceleratorPeripheral) other).controller == controller;
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
    public final boolean hasRecipe() {
        return controller.hasRecipe();
    }

    @LuaFunction
    public final int getEnergyStored()
    {
        return controller.energyStorage().getEnergyStored();
    }

}