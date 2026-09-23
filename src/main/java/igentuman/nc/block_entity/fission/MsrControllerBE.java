package igentuman.nc.block_entity.fission;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.container.MsrControllerContainer;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.multiblock.fission.MsrCache;
import igentuman.nc.multiblock.fission.MsrLogic;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.NCSounds;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MsrControllerBE extends MultiblockControllerBE {

    public static final double T_AMBIENT = 20.0;
    public static final double MAX_TEMPERATURE = 2000.0;

    @NBTField public boolean enabledByController = false;

    @NBTField(syncToClient = true) public int saltInputRate = 64;
    @NBTField(syncToClient = true) public int saltOutputRate = 64;
    @NBTField(syncToClient = true) public int reactivitySync = 0;
    @NBTField(syncToClient = true) public int temperatureSync = 0;
    @NBTField(syncToClient = true) public int depletionSync = 0;
    @NBTField(syncToClient = true) public int pebbleCountSync = 0;
    @NBTField(syncToClient = true) public int fuelCellsCountSync = 0;
    @NBTField(syncToClient = true) public int overheatTimerSync = 0;
    @NBTField(syncToClient = true) public boolean isCritical = false;
    @NBTField(syncToClient = true) public boolean powered = false;

    public int fuelCellsCount = 0;

    private List<ItemStack> allowedInputs;
    private boolean validatorsReady = false;

    public MsrControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ensureTankValidators();
        tickMultiblock(serverLevel);
        boolean newFormed = mbInstance != null && mbInstance.formed;
        if (formed != newFormed) {
            formed = newFormed;
            wasChanged = true;
        }
        if (formed && mbInstance.cache instanceof MsrCache mc) {
            setFuelCellsCount(mc.fuelCellCount);
            resizeTanks();
        } else {
            setFuelCellsCount(0);
            MsrLogic logic = logic();
            if (logic != null) logic.idle(this);
        }
        if (wasChanged) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            wasChanged = false;
        }
    }

    @Override
    public void clientTick() {
        super.clientTick();
        if (formed && powered) {
            playSound(NCSounds.MSR_RUNNING.get(), 0.4f);
        } else {
            stopSound();
        }
    }

    @Nullable
    private MsrLogic logic() {
        return mbInstance != null && mbInstance.logic instanceof MsrLogic logic ? logic : null;
    }

    public boolean isActive() {
        if (enabledByController) return true;
        return level == null || !level.hasNeighborSignal(worldPosition);
    }

    private void setFuelCellsCount(int count) {
        if (fuelCellsCount == count) return;
        fuelCellsCount = count;
        wasChanged = true;
    }

    public void updateRuntimeDisplay(double reactivity, double temperature, double depletion, int pebbles,
                                     int overheatTimer, boolean critical) {
        int r = (int) Math.round(reactivity * 100);
        int t = (int) Math.round(temperature);
        int d = (int) Math.round(depletion * 100);
        if (r == reactivitySync && t == temperatureSync && d == depletionSync && pebbles == pebbleCountSync
                && fuelCellsCount == fuelCellsCountSync && overheatTimer == overheatTimerSync
                && critical == isCritical && critical == powered) {
            return;
        }
        reactivitySync = r;
        temperatureSync = t;
        depletionSync = d;
        pebbleCountSync = pebbles;
        fuelCellsCountSync = fuelCellsCount;
        overheatTimerSync = overheatTimer;
        isCritical = critical;
        powered = critical;
        wasChanged = true;
    }

    @Nullable
    public FluidStackHandler fluidTanks() {
        FluidCapabilityHandler fh = contentHandler.getFluidHandler();
        return fh != null ? fh.getInternalHandler() : null;
    }

    @Nullable
    public ItemStackHandler itemStacks() {
        return contentHandler.hasItemCapability() ? contentHandler.getItemHandler().getInternalHandler() : null;
    }

    private void ensureTankValidators() {
        if (validatorsReady) return;
        FluidStackHandler tanks = fluidTanks();
        if (tanks == null) return;
        Fluid cold = ModEntries.fluidOf("flibe_molten_salt");
        Fluid hot = ModEntries.fluidOf("flibe_hot_molten_salt");
        tanks.setTankValidator(0, fs -> cold != null && fs.getFluid() == cold);
        tanks.setTankValidator(1, fs -> hot != null && fs.getFluid() == hot);
        validatorsReady = true;
    }

    private void resizeTanks() {
        FluidStackHandler tanks = fluidTanks();
        if (tanks == null) return;
        int vol = (int) Math.max(1, Math.min(Integer.MAX_VALUE, fuelCellsCount * (double) Multiblocks.msrVolumePerFuelCell));
        tanks.setTankCapacity(0, vol);
        tanks.setTankCapacity(1, vol);
    }

    public void handleSliderUpdate(int buttonId, int value) {
        switch (buttonId) {
            case 0 -> saltInputRate = Math.max(0, value);
            case 1 -> saltOutputRate = Math.max(0, value);
        }
        setChanged();
    }

    public void voidFuel() {
        MsrLogic logic = logic();
        if (logic != null) logic.voidFuel();
        ItemStackHandler items = itemStacks();
        if (items != null) items.setStackInSlot(0, ItemStack.EMPTY);
        setChanged();
        markDirty();
    }

    public List<ItemStack> getAllowedInputItems() {
        if (allowedInputs == null) {
            allowedInputs = new ArrayList<>();
            for (FissionFuelEntry entry : ModEntries.FISSION_FUEL.values()) {
                DeferredItem<Item> tr = entry.fuelItems().get("_tr");
                if (tr != null) allowedInputs.add(new ItemStack(tr.get()));
            }
        }
        return allowedInputs;
    }

    @Override
    protected CompoundTag legacyRuntime(CompoundTag tag) {
        return tag.contains("pebbles") ? tag : null;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MsrControllerContainer(containerId, playerInventory, this, containerData);
    }
}
