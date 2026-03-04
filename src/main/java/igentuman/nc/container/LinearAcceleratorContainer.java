package igentuman.nc.container;

import igentuman.nc.block.accelerator.entity.LinearAcceleratorControllerBE;
import igentuman.nc.content.particles.ParticleStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.LINEAR_ACCELERATOR_CONTROLLER_CONTAINER;
import static igentuman.nc.util.TextUtils.*;

public class LinearAcceleratorContainer extends AbstractContainerMenu {

    protected final LinearAcceleratorControllerBE blockEntity;
    protected final Player playerEntity;
    protected final String name = "linear_accelerator_controller";
    protected final IItemHandler playerInventory;

    public LinearAcceleratorContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(LINEAR_ACCELERATOR_CONTROLLER_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        blockEntity = (LinearAcceleratorControllerBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
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
        return blockEntity.beamLength;
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

    public boolean hasParticle() {
        return blockEntity.hasParticle;
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
        return blockEntity.heatMax;
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

    public Double getEfficiency() {
        return blockEntity.efficiency;
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

    public ParticleStack getParticleStack() {
        return blockEntity.getParticleStack();
    }

    public int maxCoolant() {
        return 10000; // TODO: get from config
    }

    public double getHeat() {
        return blockEntity.heat;
    }

    public int getEnergyRequired() {
        return blockEntity.energyRequired;
    }

    public Object getTier() {
        return blockEntity.getTier();
    }

    public int getCooling() {
        return blockEntity.coolingRate;
    }

    public int getHeating() {
        return blockEntity.heatRate;
    }

    public int getNetHeat() {
        return blockEntity.heatRate - blockEntity.coolingRate;
    }

    public boolean isAcceleratorTooHot() {
        return blockEntity.isAcceleratorTooHot();
    }
}
