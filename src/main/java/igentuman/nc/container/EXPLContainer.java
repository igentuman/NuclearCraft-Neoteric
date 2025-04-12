package igentuman.nc.container;

import igentuman.nc.block.entity.kugelblitz.EXPLBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.*;

public class EXPLContainer extends AbstractContainerMenu {

    protected EXPLBE blockEntity;
    protected Player playerEntity;

    protected String name = "expl";
    private int slotIndex = 0;

    protected IItemHandler playerInventory;

    public EXPLContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(EXPL_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        blockEntity = (EXPLBE) playerEntity.getCommandSenderWorld().getExistingBlockEntity(pos);
        layoutPlayerInventorySlots();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                playerEntity,
                EXPL_BLOCK.get()
        ) || stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                playerEntity,
                EXPL_PROXY_BLOCK.get()
        );
    }

    public Component getTitle() {
        return Component.translatable("block."+MODID+"."+name);
    }

    public int getEnergy() {
        return blockEntity.energyStorage().getEnergyStored();
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

    public int getMaxEnergy() {
        return blockEntity.energyStorage().getMaxEnergyStored();
    }

    public int energyPerTick() {
        return blockEntity.energyPerTick;
    }

    public FluidTank getFluidTank(int i) {
        return blockEntity.getFluidTank(i);
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    public boolean isReady() {
        return true;
        /*return  hasAmplifiers()
                && hasMagnets()
                && hasCoolant()
                && hasRecipe()
                && getCharge() == 100
                && hasEnoughEnergy();*/
    }

   /* public boolean hasEnoughEnergy() {
        return blockEntity.hasEnoughEnergy();
    }

    public boolean hasCoolant() {
        return blockEntity.hasCoolant();
    }*/

/*
    public int getCharge() {
        return blockEntity.functionalBlocksCharge;
    }

    public boolean isRunning() {
        return blockEntity.isRunning();
    }


    public byte analogSignal() {
        return blockEntity.analogSignal;
    }*/
}
