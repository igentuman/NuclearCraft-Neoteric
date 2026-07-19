package igentuman.nc.container;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.kugelblitz.ChamberTerminalBE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;

public class ChamberTerminalContainer extends MultiblockControllerContainer {

    public ChamberTerminalContainer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        super(containerId, playerInventory, extraData);
    }

    public ChamberTerminalContainer(int containerId, Inventory playerInventory,
                                    MultiblockControllerBE blockEntity, ContainerData data) {
        super(containerId, playerInventory, blockEntity, data);
    }

    public ChamberTerminalBE terminal() {
        return getBlockEntity() instanceof ChamberTerminalBE be ? be : null;
    }

    @Override
    protected void layoutPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 27 + col * 18, 105 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 27 + col * 18, 163));
        }
    }
}

