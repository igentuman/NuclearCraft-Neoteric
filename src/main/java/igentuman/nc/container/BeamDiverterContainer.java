package igentuman.nc.container;

import igentuman.nc.block.beam_diverter.entity.BeamDiverterControllerBE;
import igentuman.nc.content.particles.ParticleStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.*;
import static igentuman.nc.util.TextUtils.*;

public class BeamDiverterContainer extends AbstractContainerMenu {

    protected final BeamDiverterControllerBE blockEntity;
    protected final Player playerEntity;
    protected final String name = "beam_diverter_controller";
    protected final IItemHandler playerInventory;

    public BeamDiverterContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(BEAM_DIVERTER_CONTROLLER_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        blockEntity = (BeamDiverterControllerBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
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
        return energy2Display(blockEntity.energyStorage().getEnergyStored());
    }

    public int getMaxEnergy() {
        return energy2Display(blockEntity.energyStorage().getMaxEnergyStored());
    }

    public boolean hasParticle() {
        return blockEntity.hasParticle;
    }

    public BlockPos getPosition() {
        return blockEntity.getBlockPos();
    }

    public ParticleStack getParticleStack() {
        return blockEntity.getParticleStorage().getClientParticleStack();
    }

    public int getEnergyRequired() {
        return blockEntity.energyPerTick;
    }

    public Object getTier() {
        return blockEntity.getTier();
    }

    public double getFocus() {
        return 100D;
    }

    public double getStrength() {
        return 100D;
    }

    public boolean isAcceleratorTooHot() {
        return false;
    }

    public boolean isEnergyTooHigh() {
        return false;
    }

    public boolean isEnergyTooLow() {
        return false;
    }

    public int getBeamLength() {
        return 5;
    }
}
