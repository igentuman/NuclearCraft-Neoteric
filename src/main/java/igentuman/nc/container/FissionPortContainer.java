package igentuman.nc.container;

import igentuman.nc.block.entity.fission.FissionPortBE;
import igentuman.nc.container.elements.NCSlotItemHandler;
import igentuman.nc.multiblock.fission.FissionReactor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static igentuman.nc.NuclearCraft.MODID;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class FissionPortContainer extends Container {
    protected FissionPortBE portBE;
    protected PlayerEntity playerEntity;


    protected String name = "fission_reactor_port";
    private int slotIndex = 0;

    protected IItemHandler playerInventory;

    public FissionPortContainer(int pContainerId, BlockPos pos, PlayerInventory playerInventory) {
        super(FissionReactor.FISSION_PORT_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        portBE = (FissionPortBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);

        layoutPlayerInventorySlots();
        portBE.getCapability(ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
            addSlot(new NCSlotItemHandler.Input(h, 0, 56, 35));
        });
        portBE.getCapability(ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
            addSlot(new NCSlotItemHandler.Output(h, 1, 116, 35));
        });
    }

    public BlockPos getPosition() {
        return portBE.getBlockPos();
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity pPlayer, int index) {
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
                 if (index < 28) {
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
    public boolean stillValid(PlayerEntity playerIn) {
        return true;
        /*return stillValid(
                ContainerLevelAccess.create(portBE.getLevel(), portBE.getBlockPos()),
                playerEntity,
                FissionReactor.FISSION_BLOCKS.get(name).get()
        );*/
    }

    public TextComponent getTitle() {
        return new TranslationTextComponent("block."+MODID+"."+name);
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
        int topRow = 153;
        addSlotRange(playerInventory, leftCol, topRow, 9, 18);
        topRow -= 58;
        addSlotBox(playerInventory, leftCol, topRow, 9, 18, 3, 18);
    }
    public int getEnergy() {
        return portBE.getEnergyStored();
    }
    public double getProgress() {
        return portBE.getDepletionProgress();
    }

    public int getMaxEnergy() {
        return portBE.getMaxEnergyStored();
    }

    public int energyPerTick() {
        return portBE.energyPerTick();
    }

    public byte getComparatorMode() {
        return portBE.comparatorMode;
    }

    public byte getAnalogSignalStrength() {
        return portBE.analogSignal;
    }

    public FluidTank getFluidTank(int i) {
        return portBE.getFluidTank(i);
    }

    public boolean getMode() {
        return portBE.getMode();
    }

    public int getSteamPerTick() {
        return portBE.getSteamPerTick();
    }
}
