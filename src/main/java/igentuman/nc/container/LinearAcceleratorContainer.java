package igentuman.nc.container;

import igentuman.nc.block.entity.accelerator.LinearAcceleratorControllerBE;
import igentuman.nc.container.elements.NCSlotItemHandler;
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
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.LINEAR_ACCELERATOR_CONTROLLER_CONTAINER;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.energy2Display;

public class LinearAcceleratorContainer extends AbstractContainerMenu {

    protected final LinearAcceleratorControllerBE blockEntity;
    protected final Player playerEntity;
    protected final String name = "linear_accelerator_controller";
    private int slotIndex = 0;
    protected final IItemHandler playerInventory;

    public LinearAcceleratorContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(LINEAR_ACCELERATOR_CONTROLLER_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        blockEntity = (LinearAcceleratorControllerBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
        layoutPlayerInventorySlots();
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            addSlot(new NCSlotItemHandler.Input(h, 0, 135, 80));
            addSlot(new NCSlotItemHandler.Output(h, 1, 189, 80));
        });
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
                ACCELERATOR_BLOCKS.get(name).get()
        );
    }

    public Component getTitle() {
        return __("block."+MODID+"."+name);
    }

    public boolean isCasingValid() {
        return blockEntity.isCasingValid;
    }

    public int[] getDimensions() {
        return new int[]{getHeight(), getWidth(), getDepth()};
    }

    public int getDepth() {
        return blockEntity.getDepth();
    }

    public int getWidth() {
        return blockEntity.getWidth();
    }

    public int getHeight()
    {
        return blockEntity.getHeight();
    }

    public boolean isInteriorValid() {
        return blockEntity.isInternalValid;
    }

    public BlockPos getValidationResultData() {
        return  blockEntity.errorBlockPos;
    }

    public String getValidationResultKey() {
        return  blockEntity.validationResult.messageKey;
    }

    public int getEnergy() {
        return energy2Display(blockEntity.energyStorage.getEnergyStored());
    }


    private void addSlotRange(IItemHandler handler, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new SlotItemHandler(handler, slotIndex, x, y));
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
        int leftCol = 27;
        int topRow = 163;
        addSlotRange(playerInventory, leftCol, topRow, 9, 18);
        topRow -= 58;
        addSlotBox(playerInventory, leftCol, topRow, 9, 18, 3, 18);
    }

    public ItemStack getResultStack()
    {
        if(blockEntity.recipeInfo.recipe != null) {
            return blockEntity.recipeInfo.recipe.getResultItem();
        }
        return ItemStack.EMPTY;
    }

    public int getMaxEnergy() {
        return energy2Display(blockEntity.energyStorage.getMaxEnergyStored());
    }

    public boolean hasRecipe() {
        return blockEntity.hasRecipe();
    }

    public BlockPos getPosition() {
        return blockEntity.getBlockPos();
    }

    public FluidTank getFluidTank(int i) {
        return blockEntity.getFluidTank(i);
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    public double getProgress() {
        return blockEntity.recipeInfo().getProgress();
    }

}
