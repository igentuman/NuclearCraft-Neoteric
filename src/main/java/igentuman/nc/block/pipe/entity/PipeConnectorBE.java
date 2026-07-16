package igentuman.nc.block.pipe.entity;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.pipe.ConnectorMode;
import igentuman.nc.pipe.PipeCapabilityType;
import igentuman.nc.pipe.PipeNetwork;
import igentuman.nc.pipe.PipeNetworkManager;
import igentuman.nc.pipe.RedstoneMode;
import igentuman.nc.pipe.cap.NetworkEnergyStorage;
import igentuman.nc.pipe.cap.NetworkFluidHandler;
import igentuman.nc.pipe.cap.NetworkItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.setup.registration.NCBlocks.PIPE_CONNECTOR_BE;

public class PipeConnectorBE extends NuclearCraftBE {

    public static final int CAP_ITEM = 0;
    public static final int CAP_FLUID = 1;
    public static final int CAP_ENERGY = 2;
    public static final int CAP_COUNT = 3;

    private static final int NEIGHBOR_REFRESH_INTERVAL = 100;

    private PipeNetwork network;
    private ConnectorMode mode = ConnectorMode.DEFAULT;
    private RedstoneMode redstoneMode = RedstoneMode.ALWAYS;
    private final Map<Direction, BlockEntity> externalNeighbors = new EnumMap<>(Direction.class);

    private LazyOptional<IItemHandler> itemCap;
    private LazyOptional<IFluidHandler> fluidCap;
    private LazyOptional<IEnergyStorage> energyCap;

    // Per-capability transfer toggles, all disabled by default. Index matches CAP_* / PipeCapabilityType.index.
    private final boolean[] enabledCaps = new boolean[CAP_COUNT];

    public PipeConnectorBE(BlockPos pos, BlockState state) {
        super(PIPE_CONNECTOR_BE.get(), pos, state);
    }

    public PipeNetwork getNetwork() {
        return network;
    }

    public void setNetwork(PipeNetwork network) {
        this.network = network;
    }

    public ConnectorMode getMode() {
        return mode;
    }

    public void setMode(ConnectorMode mode) {
        this.mode = mode;
        invalidateCaps();
        setChanged();
        if (level != null && !level.isClientSide()) {
            PipeNetworkManager.get(level.dimension()).onConnectorConfigChanged(this);
        }
    }

    public ConnectorMode cycleMode() {
        setMode(mode.next());
        return mode;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        setChanged();
    }

    public RedstoneMode cycleRedstoneMode() {
        setRedstoneMode(redstoneMode.next());
        return redstoneMode;
    }

    public boolean isEnabled(PipeCapabilityType type) {
        return enabledCaps[type.index];
    }

    public boolean isCapabilityEnabled(int index) {
        return index >= 0 && index < enabledCaps.length && enabledCaps[index];
    }

    public void setCapabilityEnabled(int index, boolean value) {
        if (index < 0 || index >= enabledCaps.length || enabledCaps[index] == value) {
            return;
        }
        enabledCaps[index] = value;
        setChanged();
        if (level != null && !level.isClientSide()) {
            invalidateCaps();
            PipeNetworkManager.get(level.dimension()).onConnectorConfigChanged(this);
        }
    }

    public void toggleCapability(int index) {
        if (index < 0 || index >= enabledCaps.length) {
            return;
        }
        setCapabilityEnabled(index, !enabledCaps[index]);
    }

    public boolean redstoneAllows() {
        if (redstoneMode == RedstoneMode.ON_SIGNAL) {
            return level != null && level.hasNeighborSignal(getBlockPos());
        }
        return true;
    }

    public BlockEntity getExternalNeighbor(Direction dir) {
        if (externalNeighbors.containsKey(dir)) {
            BlockEntity be = externalNeighbors.get(dir);
            if (be == null) {
                return null;
            }
            if (!be.isRemoved()) {
                return be;
            }
        }
        return refreshNeighbor(dir);
    }

    private BlockEntity refreshNeighbor(Direction dir) {
        BlockEntity result = null;
        if (level instanceof ServerLevel sl) {
            BlockPos np = getBlockPos().relative(dir);
            if (sl.isLoaded(np)) {
                BlockEntity nbe = sl.getBlockEntity(np);
                if (nbe != null && !nbe.isRemoved() && !(nbe instanceof PipeConnectorBE)) {
                    result = nbe;
                }
            }
        }
        externalNeighbors.put(dir, result);
        return result;
    }

    public void onNeighborChanged(Direction dir) {
        externalNeighbors.remove(dir);
    }

    public void maybeRefreshNeighbors() {
        if ((currentTick + (getBlockPos().asLong() & 0xFF)) % NEIGHBOR_REFRESH_INTERVAL == 0) {
            externalNeighbors.clear();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            PipeNetworkManager.get(level.dimension()).registerConnector(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        externalNeighbors.clear();
        if (level != null && !level.isClientSide()) {
            PipeNetworkManager.get(level.dimension()).unregisterConnectorBE(getBlockPos());
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("EnabledCaps")) {
            byte mask = tag.getByte("EnabledCaps");
            for (int i = 0; i < enabledCaps.length; i++) {
                enabledCaps[i] = (mask & (1 << i)) != 0;
            }
        }
        if (tag.contains("Mode")) {
            int i = tag.getInt("Mode");
            ConnectorMode[] modes = ConnectorMode.values();
            mode = i >= 0 && i < modes.length ? modes[i] : ConnectorMode.DEFAULT;
        }
        if (tag.contains("RedstoneMode")) {
            int i = tag.getInt("RedstoneMode");
            RedstoneMode[] rmodes = RedstoneMode.values();
            redstoneMode = i >= 0 && i < rmodes.length ? rmodes[i] : RedstoneMode.ALWAYS;
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (level != null && !level.isClientSide() && mode == ConnectorMode.DEFAULT) {
            if (cap == ForgeCapabilities.ITEM_HANDLER && isEnabled(PipeCapabilityType.ITEM)) {
                if (itemCap == null) {
                    itemCap = LazyOptional.of(() -> new NetworkItemHandler(this));
                }
                return itemCap.cast();
            }
            if (cap == ForgeCapabilities.FLUID_HANDLER && isEnabled(PipeCapabilityType.FLUID)) {
                if (fluidCap == null) {
                    fluidCap = LazyOptional.of(() -> new NetworkFluidHandler(this));
                }
                return fluidCap.cast();
            }
            if (cap == ForgeCapabilities.ENERGY && isEnabled(PipeCapabilityType.ENERGY)) {
                if (energyCap == null) {
                    energyCap = LazyOptional.of(() -> new NetworkEnergyStorage(this));
                }
                return energyCap.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (itemCap != null) {
            itemCap.invalidate();
            itemCap = null;
        }
        if (fluidCap != null) {
            fluidCap.invalidate();
            fluidCap = null;
        }
        if (energyCap != null) {
            energyCap.invalidate();
            energyCap = null;
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        byte mask = 0;
        for (int i = 0; i < enabledCaps.length; i++) {
            if (enabledCaps[i]) {
                mask |= (1 << i);
            }
        }
        tag.putByte("EnabledCaps", mask);
        tag.putInt("Mode", mode.ordinal());
        tag.putInt("RedstoneMode", redstoneMode.ordinal());
    }
}
