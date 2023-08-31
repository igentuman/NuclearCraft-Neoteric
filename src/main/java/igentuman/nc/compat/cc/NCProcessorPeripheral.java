package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.entity.processor.NCProcessorBE;

import javax.annotation.Nonnull;

public class NCProcessorPeripheral implements IPeripheral {
    private final NCProcessorBE<?> processorBE;

    public NCProcessorPeripheral(NCProcessorBE<?> processorBE)
    {
        this.processorBE = processorBE;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return getName();
    }


    @Override
    public boolean equals( IPeripheral other )
    {
        return this == other || other instanceof NCProcessorPeripheral && ((NCProcessorPeripheral) other).processorBE == processorBE;
    }

    @LuaFunction
    public final String getName() {
        return processorBE.getName();
    }

    @LuaFunction
    public final boolean hasRecipe() {
        return processorBE.hasRecipe();
    }

    @LuaFunction
    public final int getRecipeProgress()
    {
        return processorBE.getRecipeProgress();
    }

    @LuaFunction
    public final int toggleSlotMode(int slotId, int direction)
    {
        return processorBE.toggleSideConfig(slotId, direction);
    }

    @LuaFunction
    public final int getSlotMode(int slotId, int direction)
    {
        return processorBE.getSlotMode(slotId, direction).ordinal();
    }

    @LuaFunction
    public final int getSlotsCount()
    {
        return processorBE.getSlotsCount();
    }

    @LuaFunction
    public final void voidSlotContent(int id)
    {
        processorBE.voidSlotContent(id);
    }

    @LuaFunction
    public final Object[] getSlotContent(int id)
    {
        return processorBE.getSlotContent(id);
    }

}
