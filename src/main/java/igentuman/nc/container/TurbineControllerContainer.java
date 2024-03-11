package igentuman.nc.container;

import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.math.BlockPos;

import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.antlr.v4.runtime.misc.NotNull;;

import java.util.Objects;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_CONTROLLER_CONTAINER;
import static igentuman.nc.util.TextUtils.roundFormat;

public class TurbineControllerContainer extends Container {
    protected TurbineControllerBE<?> blockEntity;
    protected PlayerEntity playerEntity;

    protected String name = "turbine_controller";
    private int slotIndex = 0;

    protected IItemHandler playerInventory;

    public TurbineControllerContainer(int pContainerId, BlockPos pos, PlayerInventory playerInventory) {
        super(TURBINE_CONTROLLER_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        blockEntity = (TurbineControllerBE<?>) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
        layoutPlayerInventorySlots();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull PlayerEntity pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull PlayerEntity playerIn) {
        return true;
       /* return stillValid(
                ContainerLevelAccess.create(Objects.requireNonNull(blockEntity.getLevel()), blockEntity.getBlockPos()),
                playerEntity,
                TURBINE_BLOCKS.get(name).get()
        );*/
    }

    public TextComponent getTitle() {
        return new TranslationTextComponent("block."+MODID+"."+name);
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

    public int getMaxEnergy() {
        return blockEntity.energyStorage.getMaxEnergyStored();
    }

    public String getEfficiency() {
        return roundFormat(blockEntity.efficiency);
    }

    public int energyPerTick() {
        return blockEntity.energyPerTick;
    }

    public boolean hasRecipe() {
        return blockEntity.hasRecipe();
    }

    public int getActiveCoils() {
        return blockEntity.getActiveCoils();
    }

    public int getFlow() {
        return blockEntity.getFlow();
    }
}
