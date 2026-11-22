package igentuman.nc.handler.storage;

import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/** Native menu slot writing through the linked inventory's shared backing store. */
public class UuidBackedSlot extends ResourceHandlerSlot {
    public UuidBackedSlot(UuidBackedItemHandler handler, int index, int x, int y) {
        super(handler, handler::set, index, x, y);
    }
}
