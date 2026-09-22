package igentuman.nc.block_entity;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.container.MultiblockPortContainer;
import igentuman.nc.util.NBTField;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** Base block entity for non-controller multiblock parts (ports); proxies capabilities to its controller. */
public class MultiblockPortBE extends GlobalBlockEntity implements MenuProvider {

    /** Constructs a port BE; lets registration pick a subclass while sharing the type/pos/state/name wiring. */
    @FunctionalInterface
    public interface Factory {
        MultiblockPortBE create(BlockEntityType<?> type, BlockPos pos, BlockState state, String name);
    }

    @Nullable
    @NBTField(syncToClient = true)
    public BlockPos controllerPos;
    private boolean sampleControlSignal;
    private int controlSignalSample;

    public MultiblockPortBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            BlockPos owner = igentuman.nc.multiblock.MultiblockHandler.getControllerForPos(serverLevel, worldPosition);
            if (owner != null) setControllerPos(owner);
            if (controllerPos != null) igentuman.nc.multiblock.MultiblockLevelState.get(serverLevel)
                    .structureAt(controllerPos)
                    .filter(record -> record.state() == igentuman.nc.multiblock.StructureLifecycleState.FORMED)
                    .ifPresent(this::configureFromStructure);
        }
    }

    @Override
    public void serverTick() {
        controlSignalSample = sampleControlSignal && controllerPos != null && level != null
                ? level.getBestNeighborSignal(worldPosition) : 0;
        if (wasChanged) {
            wasChanged = false;
            setChanged();
        }
    }

    public void setControllerPos(@Nullable BlockPos pos) {
        if (!java.util.Objects.equals(controllerPos, pos)) controlSignalSample = 0;
        if (pos == null) sampleControlSignal = false;
        this.controllerPos = pos;
        markDirty();
    }

    public void configureFromStructure(StructureRecord record) {
        sampleControlSignal = !record.machineId().getPath().equals("beam_diverter")
                && record.roles().getOrDefault(igentuman.nc.multiblock.geometry.StructureRole.SERVICE_PORT,
                        java.util.List.of()).contains(worldPosition);
    }

    public void clearStructureConfiguration() {
        sampleControlSignal = false;
        controlSignalSample = 0;
    }

    public int controlSignalSample() {
        return isRemoved() || controllerPos == null ? 0 : controlSignalSample;
    }

    @Override
    public void onChunkUnloaded() {
        controlSignalSample = 0;
        super.onChunkUnloaded();
    }

    @Nullable
    public IParticleHandler getParticleHandler(@Nullable Direction side) {
        return null;
    }

    public void recordParticleTransfer(igentuman.nc.api.particle.ParticleStack stack, long gameTime,
                                       Direction direction) {
    }

    @Nullable
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    @Nullable
    public MultiblockControllerBE controller() {
        if (controllerPos == null || level == null || !level.hasChunkAt(controllerPos)) return null;
        BlockEntity be = level.getBlockEntity(controllerPos);
        return be instanceof MultiblockControllerBE c ? c : null;
    }

    private static final String[] NO_REDSTONE_MODES = new String[0];

    /** Mode-label keys this port exposes for the unified redstone switch button; empty = no button.
     *  Each key resolves to lang {@code message.nuclearcraft.redstone_mode.<key>}. Override per port. */
    public String[] redstoneModes() {
        return NO_REDSTONE_MODES;
    }

    /** Index of the currently selected redstone mode. */
    public int getRedstoneMode() {
        return 0;
    }

    /** Advances to the next redstone mode (server side); returns the new mode index. */
    public int cycleRedstoneMode() {
        return 0;
    }

    /** True when this port's controller exposes redstone signal modes. Server side only. */
    public boolean supportsRedstone() {
        return false;
    }

    /** Comparator strength (0-15) for the current mode; 0 when not an output mode. Server side only. */
    public int getComparatorOutput() {
        return 0;
    }

    @Nullable
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        MultiblockControllerBE c = controller();
        return c != null ? c.getItemHandler(side) : super.getItemHandler(side);
    }

    @Nullable
    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        MultiblockControllerBE c = controller();
        return c != null ? c.getFluidHandler(side) : super.getFluidHandler(side);
    }

    @Nullable
    @Override
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        MultiblockControllerBE c = controller();
        return c != null ? c.getEnergyHandler(side) : super.getEnergyHandler(side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (controllerPos != null) tag.putLong("controllerPos", controllerPos.asLong());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("controllerPos")) controllerPos = BlockPos.of(tag.getLong("controllerPos"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.nuclearcraft." + name);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MultiblockPortContainer(containerId, playerInventory, this, containerData);
    }
}
