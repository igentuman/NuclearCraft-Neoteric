package igentuman.nc.container;

import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.block.heat_exchanger.entity.HeatExchangerColdCoolantPortBE;
import igentuman.nc.block.heat_exchanger.entity.HeatExchangerHotCoolantPortBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration.HX_PORT_CONTAINER;
import static igentuman.nc.util.TextUtils.__;

public class HeatExchangerPortContainer extends AbstractContainerMenu {

    protected final MultiblockPortBE portBE;
    protected final Player playerEntity;
    private int slotIndex = 0;
    protected final IItemHandler playerInventory;

    public HeatExchangerPortContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(HX_PORT_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory = new InvWrapper(playerInventory);
        portBE = (MultiblockPortBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
        layoutPlayerInventorySlots();
    }

    public BlockPos getPosition() {
        return portBE.getBlockPos();
    }

    public boolean isHotPort() {
        return portBE instanceof HeatExchangerHotCoolantPortBE;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < 36) {
                if (!this.moveItemStackTo(stack, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(pPlayer, stack);
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(Objects.requireNonNull(portBE.getLevel()), portBE.getBlockPos()),
                playerEntity,
                portBE.getBlockState().getBlock()
        );
    }

    public Component getTitle() {
        if (isHotPort()) {
            return __("block." + MODID + "." + HeatExchangerHotCoolantPortBE.NAME);
        }
        return __("block." + MODID + "." + HeatExchangerColdCoolantPortBE.NAME);
    }

    private void addSlotRange(IItemHandler handler, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new SlotItemHandler(handler, slotIndex, x, y));
            x += dx;
            slotIndex++;
        }
    }

    protected void addSlotBox(IItemHandler handler, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            addSlotRange(handler, x, y, horAmount, dx);
            y += dy;
        }
    }

    protected void layoutPlayerInventorySlots() {
        int leftCol = 8;
        int topRow = 153;
        addSlotRange(playerInventory, leftCol, topRow, 9, 18);
        topRow -= 58;
        addSlotBox(playerInventory, leftCol, topRow, 9, 18, 3, 18);
    }

    public byte getComparatorMode() {
        if (portBE instanceof HeatExchangerHotCoolantPortBE hot) {
            return hot.comparatorMode;
        }
        if (portBE instanceof HeatExchangerColdCoolantPortBE cold) {
            return cold.comparatorMode;
        }
        return 1;
    }

    public byte getAnalogSignalStrength() {
        return portBE.analogSignal;
    }

    public FluidTank getFluidTank(int i) {
        return portBE.getFluidTank(i);
    }
}
