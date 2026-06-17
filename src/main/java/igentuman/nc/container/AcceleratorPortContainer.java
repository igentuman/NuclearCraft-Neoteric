package igentuman.nc.container;

import igentuman.nc.block.accelerator.entity.AcceleratorPortBE;
import igentuman.nc.container.elements.NCSlotItemHandler;
import igentuman.nc.handler.config.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_PORT_CONTAINER;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.energy2Display;

public class AcceleratorPortContainer extends AbstractContainerMenu {

    protected final AcceleratorPortBE portBE;
    protected final Player playerEntity;
    protected final String name = "accelerator_port";
    private int slotIndex = 0;
    protected final IItemHandler playerInventory;

    public AcceleratorPortContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(ACCELERATOR_PORT_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        portBE = (AcceleratorPortBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
        layoutPlayerInventorySlots();
        portBE.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            addSlot(new NCSlotItemHandler.Input(h, 0, 71, 26));
            addSlot(new NCSlotItemHandler.Output(h, 1, 89, 26));
        });
    }

    public BlockPos getPosition() {
        return portBE.getBlockPos();
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            } else {
                if (ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0) {
                    if (!this.moveItemStackTo(stack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 28) {
                    if (!this.moveItemStackTo(stack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 37 && !this.moveItemStackTo(stack, 1, 28, false)) {
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
                ACCELERATOR_BLOCKS.get(name).get()
        );
    }

    public Component getTitle() {
        return __("block."+MODID+"."+name);
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
        int leftCol = 8;
        int topRow = 142;
        addSlotRange(playerInventory, leftCol, topRow, 9, 18);
        topRow -= 58;
        addSlotBox(playerInventory, leftCol, topRow, 9, 18, 3, 18);
    }
    public int getEnergy() {
        return energy2Display(portBE.getEnergyStored());
    }

    public int getMaxEnergy() {
        return energy2Display(portBE.getMaxEnergyStored());
    }

    public int energyPerTick() {
        return energy2Display(portBE.energyPerTick());
    }

    public byte getComparatorMode() {
        return portBE.redstoneMode;
    }

    public byte getAnalogSignalStrength() {
        return portBE.analogSignal;
    }

    public FluidTank getFluidTank(int i) {
        return portBE.getFluidTank(i);
    }

    public int getEnergyRequired() {
        return portBE.getEnergyRequired();
    }

    public CommonConfig.GTCEUCompatibilityConfig.GTCEUTier getTier() {
        return portBE.getTier();
    }
}
