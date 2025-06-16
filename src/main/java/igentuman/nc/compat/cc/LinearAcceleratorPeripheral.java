package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.entity.accelerator.LinearAcceleratorControllerBE;
import javax.annotation.Nonnull;

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
        return "nc_linear_accelerator";
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

}