package igentuman.nc.container;

import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.multiblock.fusion.FusionReactor;
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
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_CORE_PROXY;
import static igentuman.nc.util.TextUtils.*;

public class FusionCoreContainer extends AbstractContainerMenu {

    protected FusionCoreBE<?> blockEntity;
    protected Player playerEntity;

    protected String name = "fusion_core";
    private int slotIndex = 0;

    protected IItemHandler playerInventory;

    public FusionCoreContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(FusionReactor.FUSION_CORE_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        blockEntity = (FusionCoreBE<?>) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
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
                FUSION_BLOCKS.get(name).get()
        ) || stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                playerEntity,
                FUSION_CORE_PROXY.get()
        );
    }

    public Component getTitle() {
        return Component.translatable("block."+MODID+"."+name);
    }

    public boolean isCasingValid() {
        return blockEntity.isCasingValid;
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
        return blockEntity.reactorHeat;
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
        return blockEntity.energyStorage.getMaxEnergyStored();
    }

    public double getMaxHeat() {
        return blockEntity.getMaxHeat();
    }

    public String getEfficiency() {
        return roundFormat(blockEntity.efficiency*100);
    }

    public int energyPerTick() {
        return blockEntity.energyPerTick;
    }

    public boolean hasRecipe() {
        return blockEntity.hasRecipe();
    }

    public double getElectromagnetsField() {
        return blockEntity.magneticFieldStrength;
    }

    public String getAmplifierVoltage() {
        return scaledFormat(blockEntity.rfAmplification);
    }

    public String getElectromagnetsPower() {
        return scaledFormat(blockEntity.magnetsPower);
    }

    public int getElectromagnetsMaxTemp() {
        return blockEntity.maxMagnetsTemp;
    }

    public String getAmplifierPower() {
        return scaledFormat(blockEntity.rfAmplifiersPower);
    }

    public String getAmplifierMaxTemp() {
        return scaledFormat(blockEntity.minRFAmplifiersTemp);
    }

    public FluidTank getFluidTank(int i) {
        return blockEntity.getFluidTank(i);
    }

    public int getRfAmplifiersPowerRatio() {
        return blockEntity.rfAmplificationRatio;
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    public boolean isReady() {
        return  isCasingValid()
                && hasAmplifiers()
                && hasMagnets()
                && hasCoolant()
                && hasRecipe()
                && getCharge() == 100
                && hasEnoughEnergy();
    }

    public boolean hasEnoughEnergy() {
        return blockEntity.hasEnoughEnergy();
    }

    public boolean hasCoolant() {
        return blockEntity.hasCoolant();
    }

    public boolean hasMagnets() {
        return blockEntity.magnetsPower > 0;
    }

    public boolean hasAmplifiers() {
        return blockEntity.rfAmplifiersPower > 0;
    }

    public int getCharge() {
        return blockEntity.functionalBlocksCharge;
    }

    public double getPlasmaHeat() {
        return blockEntity.plasmaTemperature;
    }

    public double getOptimalTemp() {
        return blockEntity.getOptimalTemperature();
    }

    public int requiredEnergy() {
        return blockEntity.rfAmplifiersPower+blockEntity.magnetsPower;
    }

    public boolean isRunning() {
        return blockEntity.isRunning();
    }

    public int getPlasmaStability() {
        return (int) (blockEntity.getPlasmaStability()*100);
    }
}
