package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaException;
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
    public final int getEnergyPerTick()
    {
        return controller.energyPerTick;
    }

    @LuaFunction
    public final int getEnergyStored()
    {
        return controller.energyStorage().getEnergyStored();
    }

    @LuaFunction
    public final int getRecipeProgress()
    {
        return (int) (controller.recipeInfo().getProgress() * 100);
    }

    @LuaFunction
    public final int getEvaporationRate()
    {
        return controller.evaporation;
    }

    @LuaFunction
    public final int getFeedingRate()
    {
        return (int) controller.feeding;
    }

    @LuaFunction
    public final int getBlackholeMass() {
        return (int) (controller.mass/1000);
    }

    @LuaFunction
    public final int getBlackholeStability() {
        return controller.blackholeStability;
    }

    @LuaFunction
    public final int getQuantumFrequency() {
        return controller.frequency;
    }

    @LuaFunction
    public final int setQuantumFrequency(int frequency) throws LuaException {
        if (frequency < 0 || frequency > 15) {
            throw new LuaException("Frequency must be between 0 and 15");
        }
        controller.frequency = (byte) frequency;
        controller.setChanged();
        return controller.frequency;
    }

    @LuaFunction
    public final int getFluxRegulators() {
        return controller.fluxRegulators;
    }

    @LuaFunction
    public final int getTransformers() {
        return controller.transformers;
    }

    @LuaFunction
    public final int getStabilizers() {
        return controller.stabilizers;
    }

    @LuaFunction
    public final int getTransformationEnergyRate() {
        return controller.energyConvertionRate;
    }

    @LuaFunction
    public final int setTransformationEnergyRate(int rate) throws LuaException {
        if (rate < 0 || rate > 100) {
            throw new LuaException("Rate must be between 0 and 100");
        }
        controller.energyConvertionRate = rate;
        controller.setChanged();
        return controller.energyConvertionRate;
    }
}