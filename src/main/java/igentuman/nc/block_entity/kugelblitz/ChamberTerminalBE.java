package igentuman.nc.block_entity.kugelblitz;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.RedstoneModeController;
import igentuman.nc.container.ChamberTerminalContainer;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.multiblock.kugelblitz.KugelblitzLogic;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static igentuman.nc.block_entity.kugelblitz.BlackHoleBE.MAX_MASS;

public class ChamberTerminalBE extends MultiblockControllerBE implements RedstoneModeController {

    @NBTField public long feeding = 0;
    @NBTField public long mass = 0;
    @NBTField public int evaporation = 0;
    @NBTField public byte frequency = 0;
    @NBTField public int energyConvertionRate = 7;
    @NBTField public boolean controllerEnabled = false;
    @NBTField public int transformers = 0;
    @NBTField public int fluxRegulators = 0;
    @NBTField public int stabilizers = 0;
    @NBTField public int blackholeStability = 100;
    @NBTField public int energyPerTick = 0;
    @NBTField public BlockPos blackholePos = BlockPos.ZERO;

    @NBTField public int ticksProcessed = 0;
    @NBTField public int ticksNeeded = 0;
    @NBTField public int processEnergy = 0;
    @NBTField(syncToClient = true) public int progress = 0;

    private boolean fluidValidatorReady = false;

    public ChamberTerminalBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Nullable
    public FluidStackHandler fluidTanks() {
        FluidCapabilityHandler fh = contentHandler.getFluidHandler();
        return fh != null ? fh.getInternalHandler() : null;
    }

    @Nullable
    public ItemCapabilityHandler items() {
        return contentHandler.getItemHandler();
    }

    private void ensureFluidValidator() {
        if (fluidValidatorReady) return;
        FluidStackHandler tanks = fluidTanks();
        if (tanks == null) return;
        var fluid = ModEntries.fluidOf("subliquid_matter");
        tanks.setTankValidator(0, fs -> fluid != null && fs.getFluid() == fluid);
        fluidValidatorReady = true;
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ensureFluidValidator();
        tickMultiblock(serverLevel);
        boolean newFormed = mbInstance != null && mbInstance.formed;
        if (formed != newFormed) {
            formed = newFormed;
            wasChanged = true;
        }
        if (!formed) {
            KugelblitzLogic logic = logic();
            if (logic != null) logic.idle(this);
        }
        if (wasChanged) {
            setChanged();
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            wasChanged = false;
        }
    }

    @Nullable
    private KugelblitzLogic logic() {
        return mbInstance != null && mbInstance.logic instanceof KugelblitzLogic logic ? logic : null;
    }

    public void updateStructureDisplay(int transformerCount, int fluxRegulatorCount, int stabilizerCount,
                                       @Nullable BlockPos center) {
        BlockPos newCenter = center != null ? center : blackholePos;
        if (transformers == transformerCount && fluxRegulators == fluxRegulatorCount
                && stabilizers == stabilizerCount && blackholePos.equals(newCenter)) return;
        transformers = transformerCount;
        fluxRegulators = fluxRegulatorCount;
        stabilizers = stabilizerCount;
        blackholePos = newCenter;
        markDirty();
    }

    public void updateRuntimeDisplay(long newMass, long newFeeding, int newEvaporation, int newStability,
                                     int newEnergyPerTick, int newTicksProcessed, int newTicksNeeded,
                                     int newProcessEnergy, int newProgress) {
        boolean enabled = newMass > 0;
        if (mass == newMass && feeding == newFeeding && evaporation == newEvaporation
                && blackholeStability == newStability && energyPerTick == newEnergyPerTick
                && ticksProcessed == newTicksProcessed && ticksNeeded == newTicksNeeded
                && processEnergy == newProcessEnergy && progress == newProgress && controllerEnabled == enabled) {
            return;
        }
        mass = newMass;
        feeding = newFeeding;
        evaporation = newEvaporation;
        blackholeStability = newStability;
        energyPerTick = newEnergyPerTick;
        ticksProcessed = newTicksProcessed;
        ticksNeeded = newTicksNeeded;
        processEnergy = newProcessEnergy;
        progress = newProgress;
        controllerEnabled = enabled;
        markDirty();
    }

    public void gotEnergy(Direction facing) {
        KugelblitzLogic logic = logic();
        if (logic != null) logic.gotEnergy(facing);
    }

    public void handleSliderUpdate(int buttonId, int ratio) {
        switch (buttonId) {
            case 0 -> energyConvertionRate = ratio;
            case 1 -> frequency = (byte) (0.15D * ratio);
        }
        markDirty();
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public int comparatorSignal(int mode) {
        return switch (mode) {
            case ChamberPortBE.MODE_ENERGY -> energyStorage != null && energyStorage.getMaxEnergyStored() > 0
                    ? (int) ((long) energyStorage.getEnergyStored() * 15 / energyStorage.getMaxEnergyStored()) : 0;
            case ChamberPortBE.MODE_MASS -> (int) (mass * 15 / MAX_MASS);
            case ChamberPortBE.MODE_PROGRESS -> getProgress() * 15 / 100;
            case ChamberPortBE.MODE_ITEMS -> {
                ItemCapabilityHandler inv = items();
                if (inv == null) yield 0;
                ItemStack s = inv.getStackInSlot(0);
                yield s.isEmpty() ? 0 : s.getCount() * 15 / s.getMaxStackSize();
            }
            default -> 0;
        };
    }

    @Override
    public void applyRedstoneInput(int mode, int signal) {
        switch (mode) {
            case ChamberPortBE.MODE_FREQUENCY -> frequency = (byte) Math.clamp(signal, 0, 15);
            case ChamberPortBE.MODE_TRANSFORMATION_ENERGY_RATE -> energyConvertionRate = signal * 100 / 15;
        }
        markDirty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ChamberTerminalContainer(containerId, playerInventory, this, containerData);
    }
}
