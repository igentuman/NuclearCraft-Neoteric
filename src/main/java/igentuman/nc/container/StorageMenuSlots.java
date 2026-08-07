package igentuman.nc.container;

import igentuman.nc.handler.storage.UuidBackedItemHandler;
import igentuman.nc.handler.storage.UuidBackedSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public final class StorageMenuSlots {

    private StorageMenuSlots() {}

    @FunctionalInterface
    public interface SlotSink {
        Slot add(Slot slot);
    }

    public static int layout(SlotSink sink, UuidBackedItemHandler inventory, Inventory playerInventory, int rows, int cols) {
        int idx = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                sink.add(new UuidBackedSlot(inventory, idx++, 5 + c * 18, 5 + r * 18));
            }
        }

        int xShift = switch (cols) {
            case 12 -> 32;
            case 13 -> 41;
            default -> 5;
        };
        int invY = 5 + (rows - 1) * 18 + 23;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                sink.add(new Slot(playerInventory, c + r * 9 + 9, xShift + c * 18, invY + r * 18));
            }
        }
        int hotbarY = invY + 18 * 3 + 4;
        for (int c = 0; c < 9; c++) {
            sink.add(new Slot(playerInventory, c, xShift + c * 18, hotbarY));
        }
        return rows * cols;
    }
}
