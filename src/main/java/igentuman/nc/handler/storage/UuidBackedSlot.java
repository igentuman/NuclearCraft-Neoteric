package igentuman.nc.handler.storage;

import net.neoforged.neoforge.items.SlotItemHandler;

public class UuidBackedSlot extends SlotItemHandler {

    private final UuidBackedItemHandler handler;

    public UuidBackedSlot(UuidBackedItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
        this.handler = handler;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        handler.contentsChanged();
    }
}
