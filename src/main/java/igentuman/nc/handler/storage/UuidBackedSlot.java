package igentuman.nc.handler.storage;

import net.minecraftforge.items.SlotItemHandler;

/**
 * Slot over a {@link UuidBackedItemHandler} that notifies the backing store on every change. Vanilla
 * slot merges ({@code safeInsert} growing an existing stack) mutate the stored stack in place without
 * calling {@code setStackInSlot}, so without this the store would never be marked dirty for those edits.
 */
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
