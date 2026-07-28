package igentuman.nc.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import igentuman.nc.block.crafter.entity.EngineersCrafterBE;

import javax.annotation.Nonnull;

public class EngineersCrafterPeripheral implements IPeripheral {
    private final EngineersCrafterBE crafter;

    public EngineersCrafterPeripheral(EngineersCrafterBE crafter) {
        this.crafter = crafter;
    }

    @Nonnull
    @Override
    public String getType() {
        return "nc_engineers_crafter";
    }

    @Override
    public boolean equals(IPeripheral other) {
        return this == other || other instanceof EngineersCrafterPeripheral && ((EngineersCrafterPeripheral) other).crafter == crafter;
    }

    @LuaFunction
    public final String getName() {
        return crafter.getName();
    }

    @LuaFunction
    public final int getInventorySlots() {
        return crafter.getInventorySlots();
    }

    @LuaFunction
    public final Object getSlotData(int id) {
        return crafter.getSlotData(id);
    }

    @LuaFunction
    public final Object[] getPatterns() {
        return crafter.getPatternsInfo();
    }

    @LuaFunction(mainThread = true)
    public final boolean doCrafting(int id, int qty) {
        return crafter.startCraft(id, qty);
    }
}
