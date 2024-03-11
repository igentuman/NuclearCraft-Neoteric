package igentuman.nc.container;

import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.multiblock.fusion.FusionReactor;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.antlr.v4.runtime.misc.NotNull;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_CORE_PROXY;
import static igentuman.nc.util.TextUtils.*;

public class FusionCoreContainer extends Container {

    protected FusionCoreBE<?> blockEntity;
    protected PlayerEntity playerEntity;

    protected String name = "fusion_core";
    private int slotIndex = 0;

    protected IItemHandler playerInventory;

    public FusionCoreContainer(int pContainerId, BlockPos pos, PlayerInventory playerInventory) {
        super(FusionReactor.FUSION_CORE_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        blockEntity = (FusionCoreBE<?>) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
        layoutPlayerInventorySlots();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull PlayerEntity pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull PlayerEntity playerIn) {
        return true;
/*        return stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                playerEntity,
                FUSION_BLOCKS.get(name).get()
        ) || stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                playerEntity,
                FUSION_CORE_PROXY.get()
        );*/
    }

    public TextComponent getTitle() {
        return new TranslationTextComponent("block."+MODID+"."+name);
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
