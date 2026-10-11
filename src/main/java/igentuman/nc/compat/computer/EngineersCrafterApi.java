package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import li.cil.oc2.api.bus.device.object.Callback;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class EngineersCrafterApi extends ComputerApi<EngineersCrafterBE> {

    public EngineersCrafterApi(EngineersCrafterBE be, ComputerErrors errors) {
        super(be, errors);
    }

    @Override
    public String typeName() {
        return be.getName();
    }

    @LuaFunction
    @Callback
    public final String getName() {
        return be.getName();
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final int getInventorySlots() {
        return be.getInventorySlots();
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final @Nullable Object getSlotData(int id) {
        return be.getSlotData(id);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final Object[] getPatterns() {
        return be.getPatternsInfo();
    }

    @LuaFunction
    @Callback
    public final Map<String, Object> getEnergy() {
        Map<String, Object> result = new HashMap<>();
        result.put("stored", be.energy.getEnergyStored());
        result.put("capacity", be.energy.getMaxEnergyStored());
        return result;
    }

    @LuaFunction
    @Callback
    public final boolean isCrafting() {
        return be.hasActiveJob();
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final boolean doCrafting(int id, int qty) {
        return be.startCraft(id, qty);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final void cancelCrafting() {
        be.cancelJob();
    }
}
