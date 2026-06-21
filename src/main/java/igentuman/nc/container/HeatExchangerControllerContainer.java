package igentuman.nc.container;

import igentuman.nc.block.heat_exchanger.entity.HeatExchangerControllerBE;
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

import java.util.Objects;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration.HX_BLOCKS;
import static igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration.HX_CONTROLLER_CONTAINER;
import static igentuman.nc.util.TextUtils.*;

public class HeatExchangerControllerContainer extends AbstractContainerMenu {

    protected final HeatExchangerControllerBE blockEntity;
    protected final Player playerEntity;
    protected final String name = "heat_exchanger_controller";
    private int slotIndex = 0;
    protected final IItemHandler playerInventory;

    public HeatExchangerControllerContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(HX_CONTROLLER_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory = new InvWrapper(playerInventory);
        blockEntity = (HeatExchangerControllerBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
        layoutPlayerInventorySlots();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(Objects.requireNonNull(blockEntity.getLevel()), blockEntity.getBlockPos()),
                playerEntity,
                HX_BLOCKS.get(name).get()
        );
    }

    public Component getTitle() {
        return __("block." + MODID + "." + name);
    }

    public boolean isCasingValid() {
        return blockEntity.isCasingValid;
    }

    public int getDepth() {
        return blockEntity.getDepth();
    }

    public int getWidth() {
        return blockEntity.getWidth();
    }

    public int getHeight() {
        return blockEntity.getHeight();
    }

    public boolean isInteriorValid() {
        return blockEntity.isInternalValid;
    }

    public BlockPos getValidationResultData() {
        return blockEntity.errorBlockPos;
    }

    public String getValidationResultKey() {
        return blockEntity.validationResult.messageKey;
    }

    public int getEnergy() {
        return blockEntity.energyStorage().getEnergyStored();
    }

    public int getMaxEnergy() {
        return energy2Display(blockEntity.energyStorage().getMaxEnergyStored());
    }

    public int energyPerTick() {
        return energy2Display(blockEntity.energyPerTick);
    }

    public int getHeatExchangers() {
        return blockEntity.getHeatExchangers();
    }

    public int getRadiators() {
        return blockEntity.getRadiators();
    }

    public double getHeat() {
        return blockEntity.getHeat();
    }

    public long getMaxHeat() {
        return (long) blockEntity.getMaxHeat();
    }

    public boolean hasRecipe() {
        return blockEntity.hasRecipe();
    }

    public double getProgress() {
        return blockEntity.getProgress();
    }

    public double getColdProgress() {
        return blockEntity.getColdProgress();
    }

    public boolean isRunning() {
        return blockEntity.powered;
    }

    public FluidTank getFluidTank(int i) {
        return blockEntity.getFluidTank(i);
    }

    public BlockPos getPosition() {
        return blockEntity.getBlockPos();
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
}
