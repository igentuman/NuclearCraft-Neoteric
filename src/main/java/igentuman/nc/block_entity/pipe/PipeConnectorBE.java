package igentuman.nc.block_entity.pipe;

import igentuman.nc.container.PipeConnectorContainer;
import igentuman.nc.api.pipe.ConnectorMode;
import igentuman.nc.api.pipe.PipeCapabilityType;
import igentuman.nc.api.pipe.PipeNetwork;
import igentuman.nc.api.pipe.PipeNetworkManager;
import igentuman.nc.api.pipe.RedstoneMode;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static igentuman.nc.util.TextUtils.__;

public class PipeConnectorBE extends BlockEntity implements MenuProvider {

    public static final int CAP_ITEM = 0;
    public static final int CAP_FLUID = 1;
    public static final int CAP_ENERGY = 2;
    public static final int CAP_COUNT = 3;

    private PipeNetwork network;
    private ConnectorMode mode = ConnectorMode.DEFAULT;
    private RedstoneMode redstoneMode = RedstoneMode.ALWAYS;
    private final boolean[] enabledCaps = new boolean[CAP_COUNT];

    public PipeConnectorBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state);
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
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(getBlockPos());
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
            level.invalidateCapabilities(getBlockPos());
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

    /** Resolves a capability on the machine adjacent to {@code face}, skipping other pipe nodes so
     *  content never routes into the network's own conduit. Returns null off-server or when absent. */
    public <T> T getExternalCapability(Direction face, BlockCapability<T, Direction> cap) {
        if (!(level instanceof ServerLevel sl)) {
            return null;
        }
        BlockPos np = getBlockPos().relative(face);
        if (!sl.isLoaded(np)) {
            return null;
        }
        if (PipeNetworkManager.isPipeNode(sl.getBlockState(np))) {
            return null;
        }
        return sl.getCapability(cap, np, face.getOpposite());
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
        if (level != null && !level.isClientSide()) {
            PipeNetworkManager.get(level.dimension()).unregisterConnectorBE(getBlockPos());
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
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

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
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

    @Override
    public @NotNull Component getDisplayName() {
        return __("block.nuclearcraft.pipe_connector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inv, @NotNull Player player) {
        return new PipeConnectorContainer(windowId, inv, this);
    }
}
