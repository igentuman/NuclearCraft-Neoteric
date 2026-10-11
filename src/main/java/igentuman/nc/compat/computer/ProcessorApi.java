package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.handler.SidedContentHandler.RelativeDirection;
import li.cil.oc2.api.bus.device.object.Callback;

import java.util.Locale;

public class ProcessorApi extends MachineApi<GlobalBlockEntity> {

    public ProcessorApi(GlobalBlockEntity be, ComputerErrors errors) {
        super(be, errors);
    }

    @LuaFunction
    @Callback
    public final int getSlotsCount() {
        int items = be.hasInventory() ? be.contentHandler.getItemHandler().getSlots() : 0;
        int tanks = be.hasFluidTanks() ? be.contentHandler.getFluidHandler().getTanks() : 0;
        return items + tanks;
    }

    @LuaFunction
    @Callback
    public final String getSlotMode(int slot, String side) {
        checkSlot(slot);
        return be.contentHandler.getSideMode(slot - 1, side(side)).name().toLowerCase(Locale.ROOT);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final String toggleSlotMode(int slot, String side) {
        checkSlot(slot);
        RelativeDirection direction = side(side);
        be.contentHandler.toggleSideConfig(slot - 1, direction);
        be.setChanged();
        return be.contentHandler.getSideMode(slot - 1, direction).name().toLowerCase(Locale.ROOT);
    }

    private void checkSlot(int slot) {
        if (slot < 1 || slot > getSlotsCount()) throw error("Slot must be between 1 and " + getSlotsCount());
    }

    private RelativeDirection side(String side) {
        try {
            return RelativeDirection.valueOf(side.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw error("Side must be one of front, back, left, right, up, down");
        }
    }
}
