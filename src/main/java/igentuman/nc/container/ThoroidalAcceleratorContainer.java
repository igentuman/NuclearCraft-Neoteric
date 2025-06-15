package igentuman.nc.container;

import igentuman.nc.block.entity.accelerator.ThoroidalAcceleratorControllerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.*;
import static igentuman.nc.util.TextUtils.*;

public class ThoroidalAcceleratorContainer extends AbstractContainerMenu {

    protected final ThoroidalAcceleratorControllerBE blockEntity;
    protected final Player playerEntity;
    protected final String name = "thoroidal_accelerator_controller";
    protected final IItemHandler playerInventory;

    public ThoroidalAcceleratorContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(THOROIDAL_ACCELERATOR_CONTROLLER_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        blockEntity = (ThoroidalAcceleratorControllerBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
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

    public int getMaxHeat() {
        return blockEntity.maxTemperature;
    }

    public double getFocus() {
        return blockEntity.focus;
    }

    public String getVoltage() {
        return scaledFormat(blockEntity.acceleratingVoltage);
    }

    public int getDipoles() {
        return blockEntity.dipoles;
    }

    public int getQuadroupoles() {
        return blockEntity.quadroupoles;
    }

    public String getEfficiency() {
        return blockEntity.efficiency + "%";
    }

    public int getAmplifiers() {
        return blockEntity.amplifiers;
    }

    public int getCoolers() {
        return blockEntity.coolers;
    }

    public double getStrength() {
        return blockEntity.quadStrength + blockEntity.dipoleStrength;
    }
}
