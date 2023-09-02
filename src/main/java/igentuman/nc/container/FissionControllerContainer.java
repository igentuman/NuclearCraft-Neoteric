package igentuman.nc.container;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.container.elements.NCSlotItemHandler;
import igentuman.nc.multiblock.fission.FissionReactor;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.numberFormat;

public class FissionControllerContainer extends AbstractContainerMenu {
    protected FissionControllerBE blockEntity;
    protected Player playerEntity;

    protected String name = "fission_reactor_controller";
    private int slotIndex = 0;

    protected IItemHandler playerInventory;

    public FissionControllerContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(FissionReactor.FISSION_CONTROLLER_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        blockEntity = (FissionControllerBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
        layoutPlayerInventorySlots();
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            addSlot(new NCSlotItemHandler.Input(h, 0, 56, 35));
        });
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            addSlot(new NCSlotItemHandler.Output(h, 1, 116, 35));
        });
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
    public boolean stillValid(Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                playerEntity,
                FissionReactor.MULTI_BLOCKS.get(name).get()
        );
    }

    public Component getTitle() {
        return Component.translatable("block."+MODID+"."+name);
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
        return blockEntity.energyStorage.getEnergyStored();
    }

    public double getHeat() {
        return blockEntity.heat;
    }

    public double getProgress() {
        return blockEntity.getDepletionProgress();
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

    public int getFuelCellsCount() {
        return blockEntity.fuelCellsCount;
    }

    public ItemStack getResultStack()
    {
        if(blockEntity.recipeInfo.recipe != null) {
            return blockEntity.recipeInfo.recipe.getResultItem();
        }
        return ItemStack.EMPTY;
    }

    public int getMaxEnergy() {
        return blockEntity.energyStorage.getMaxEnergyStored();
    }

    public double getMaxHeat() {
        return blockEntity.getMaxHeat();
    }

    public String getEfficiency() {
        return numberFormat(blockEntity.efficiency);
    }

    public String getNetHeat() {
        return numberFormat(blockEntity.getNetHeat());
    }

    public double getCooling() {
        return blockEntity.heatSinkCooling;
    }

    public String getHeating() {
        return numberFormat(blockEntity.heatPerTick);
    }

    public int getHeatSinksCount() {
        return blockEntity.heatSinksCount;
    }

    public int getModeratorsCount() {
        return blockEntity.moderatorsCount;
    }

    public int energyPerTick() {
        return blockEntity.energyPerTick;
    }

    public String getHeatMultiplier() {
        return numberFormat(blockEntity.heatMultiplier);
    }

    public boolean hasRecipe() {
        return blockEntity.hasRecipe();
    }
}
