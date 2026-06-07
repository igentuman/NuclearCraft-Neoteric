package igentuman.nc.container;

import igentuman.nc.block.entity.ChargingStationBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCBlocks.CHARGING_STATION_BLOCK;
import static igentuman.nc.setup.registration.NCBlocks.CHARGING_STATION_CONTAINER;
import static igentuman.nc.util.TextUtils.__;

public class ChargingStationContainer extends AbstractContainerMenu {

    public final ChargingStationBE blockEntity;
    private final Player player;

    public ChargingStationContainer(int windowId, BlockPos pos, Inventory inv) {
        super(CHARGING_STATION_CONTAINER.get(), windowId);
        this.player = inv.player;
        this.blockEntity = (ChargingStationBE) player.level().getBlockEntity(pos);

        addSlot(new SlotItemHandler(blockEntity.items, 0, 80, 35) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 10 + col * 18, 96 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 10 + col * 18, 154));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player p, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(p, stack);
        return original;
    }

    @Override
    public boolean stillValid(@NotNull Player p) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, CHARGING_STATION_BLOCK.get());
    }

    public Component getTitle() {
        return __("block." + MODID + ".charging_station");
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }
}
