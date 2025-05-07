package igentuman.nc.container;

import igentuman.nc.block.entity.kugelblitz.ChamberPortBE;
import igentuman.nc.container.elements.NCSlotItemHandler;
import igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.CHAMBER_PORT_CONTAINER;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.roundFormat;

public class ChamberPortContainer extends AbstractContainerMenu {
    protected final ChamberPortBE blockEntity;
    protected final Player playerEntity;
    protected final String name = "chamber_port";
    private int slotIndex = 0;
    protected final IItemHandler playerInventory;

    public ChamberPortContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(CHAMBER_PORT_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        blockEntity = (ChamberPortBE) playerEntity.getCommandSenderWorld().getExistingBlockEntity(pos);
        layoutPlayerInventorySlots();
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            addSlot(new NCSlotItemHandler.Input(h, 0, 56, 35));
            addSlot(new NCSlotItemHandler.Output(h, 1, 116, 35));
        });
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
                KugelblitzRegistration.KUGELBLITZ_BLOCKS.get(name).get()
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
        int topRow = 153;
        addSlotRange(playerInventory, leftCol, topRow, 9, 18);
        topRow -= 58;
        addSlotBox(playerInventory, leftCol, topRow, 9, 18, 3, 18);
    }

    public FluidTank getFluidTank(int i) {
        return blockEntity.getFluidTank(i);
    }

    public int getMaxEnergy() {
        return 0;
    }

    public double getEnergy() {
        return 0;
    }

    public int energyPerTick() {
        return blockEntity.energyPerTick();
    }

    public double getProgress() {
        return blockEntity.getProgress();
    }

    public BlockPos getPosition() {
        return blockEntity.getBlockPos();
    }

    public byte getComparatorMode() {
        return blockEntity.comparatorMode;
    }

    public byte getAnalogSignalStrength() {
        return blockEntity.analogSignal;
    }
}
