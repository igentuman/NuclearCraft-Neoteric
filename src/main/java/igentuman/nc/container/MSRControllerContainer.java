package igentuman.nc.container;

import igentuman.nc.block.fission.entity.MSRControllerBE;
import igentuman.nc.container.elements.NCSlotItemHandler;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.*;

public class MSRControllerContainer extends AbstractContainerMenu {

    protected final MSRControllerBE blockEntity;
    protected final Player playerEntity;
    protected final String name = "msr_controller";
    private int slotIndex = 0;
    protected final IItemHandler playerInventory;

    public MSRControllerContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(FissionReactorRegistration.MSR_CONTROLLER_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        blockEntity = (MSRControllerBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
        layoutPlayerInventorySlots();
        addSlot(new NCSlotItemHandler.Input(blockEntity.contentHandler().itemHandler, 0, 106, 21));
        addSlot(new NCSlotItemHandler.Output(blockEntity.contentHandler().itemHandler, 1, 106, 51));
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if(slot instanceof NCSlotItemHandler.Output || slot instanceof NCSlotItemHandler.Input) {
                if (!this.moveItemStackTo(stack, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, slots.size()-2, slots.size(), true)) {
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
    public boolean stillValid(Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                playerEntity,
                FissionReactorRegistration.FISSION_BLOCKS.get(name).get()
        );
    }

    public double getHeat() {
        return blockEntity.temperature;
    }

    public double getMaxHeat() {
        return MSRControllerBE.MAX_TEMPERATURE;
    }

    public int getSalt() {
        return (int) blockEntity.saltVolume;
    }

    public int getHotSalt() {
        return (int) blockEntity.hotSaltVolume;
    }

    public int getFreeVolume() {
        return (int) blockEntity.freeVolume();
    }

    public int getInputRate() {
        return blockEntity.saltInputRate;
    }

    public int getOutputRate() {
        return blockEntity.saltOutputRate;
    }

    public int getGlobalVolume() {
        return (int) blockEntity.globalVolume();
    }

    public double getReactivity() {
        return blockEntity.reactivity;
    }

    public boolean isPowered() {
        return blockEntity.powered;
    }

    public boolean isCritical() {
        return blockEntity.isCritical;
    }

    private void addSlotRange(IItemHandler handler, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new net.minecraftforge.items.SlotItemHandler(handler, slotIndex, x, y));
            x += dx;
            slotIndex++;
        }
    }

    protected void addSlotBox(IItemHandler handler, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
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

    public Component getTitle() {
        return __("block."+MODID+"."+name);
    }

    public String getHeating() {
        return roundFormat(blockEntity.heatPerTick);
    }

    public boolean isCasingValid() {
        return blockEntity.isCasingValid;
    }

    public boolean isInteriorValid() {
        return blockEntity.isInternalValid;
    }

    public String getValidationResultKey() {
        return blockEntity.validationResult.messageKey;
    }

    public BlockPos getValidationResultData() {
        return blockEntity.errorBlockPos;
    }

    public int getHeight() {
        return blockEntity.getHeight();
    }

    public int getWidth() {
        return blockEntity.getWidth();
    }

    public int getDepth() {
        return blockEntity.getDepth();
    }

    public boolean canAnalyze() {
        return blockEntity.analyzeDelay < 1;
    }

    public BlockPos getPosition() {
        return blockEntity.getBlockPos();
    }

    public int getFuelCellsCount() {
        return blockEntity.fuelCellsCount;
    }

    public FluidTank getFluidTank(int i) {
        return blockEntity.getFluidTank(i);
    }

    public double getTemperature() {
        return blockEntity.temperature;
    }

    public double getOverheatTimer() {
        return blockEntity.overheatTimer;
    }

    public int getPebblesQty() {
        return blockEntity.pebbleCount;
    }

    public double getDepletion() {
        return blockEntity.depletion*100;
    }
}