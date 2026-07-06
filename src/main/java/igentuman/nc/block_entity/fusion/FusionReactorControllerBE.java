package igentuman.nc.block_entity.fusion;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.block_entity.RedstoneModeController;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.multiblock.fusion.FusionReaction;
import igentuman.nc.multiblock.fusion.FusionReactorCache;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Controller for the fusion reactor. The structure validator (off-thread) writes stats into the
 * {@link FusionReactorCache}; this BE runs the {@link FusionReaction} on the main thread each tick
 * when formed. FE-only output. Also owns the 3x3x3 proxy cage placed around itself.
 */
public class FusionReactorControllerBE extends MultiblockControllerBE implements RedstoneModeController {

    private final FusionReaction reaction = new FusionReaction();
    private boolean redstoneActivatedByPort = true;

    @NBTField(syncToClient = true) public int functionalBlocksCharge;
    @NBTField(syncToClient = true) public int energyPerTick;
    @NBTField(syncToClient = true) public int amplificationAdjustment = 50;
    @NBTField(syncToClient = true) public boolean running;

    @NBTField public double reactorHeat;
    @NBTField public long plasmaTemperature;
    @NBTField public long chargeAmount;
    @NBTField public double efficiency;
    @NBTField public double maxHeat;
    @NBTField public int rfAmplificationRatio = 100;

    public FusionReactorControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Nullable
    public FluidStackHandler fluidTanks() {
        FluidCapabilityHandler fh = contentHandler.getFluidHandler();
        return fh != null ? fh.getInternalHandler() : null;
    }

    public boolean isRunning() {
        return running;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public double getMaxHeat() {
        return maxHeat;
    }

    public double rfAmplifierRatio() {
        return Math.max(0, Math.min((rfAmplificationRatio / 100.0) * (amplificationAdjustment / 100.0), 1.0));
    }

    /** GUI slider (1..100) tuning the amplification ratio. */
    public void setAmplificationAdjustment(int value) {
        int clamped = Math.min(100, Math.max(1, value));
        if (clamped != amplificationAdjustment) {
            amplificationAdjustment = clamped;
            markDirty();
        }
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        tickMultiblock(serverLevel);
        boolean newFormed = mbInstance != null && mbInstance.formed;
        if (formed != newFormed) {
            formed = newFormed;
            wasChanged = true;
        }
        if (formed && redstoneActivatedByPort && mbInstance.cache instanceof FusionReactorCache fc) {
            reaction.tick(this, fc);
        } else {
            reaction.idle(this);
        }
        if (wasChanged) {
            assert getLevel() != null;
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            wasChanged = false;
        }
    }

    @Override
    public void onControllerPlaced(ServerLevel level) {
        placeProxyCage(level);
        super.onControllerPlaced(level);
    }

    @Override
    public void onControllerRemoved(ServerLevel level) {
        removeProxyCage(level);
        super.onControllerRemoved(level);
    }

    private void placeProxyCage(ServerLevel level) {
        Block proxy = ModEntries.get("fusion_reactor_core_proxy").block().get();
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos p = worldPosition.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (!s.isAir() && !s.is(proxy)) return; // blocked - build no cage
                }
            }
        }
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos p = worldPosition.offset(x, y, z);
                    level.setBlock(p, proxy.defaultBlockState(), Block.UPDATE_ALL);
                    if (level.getBlockEntity(p) instanceof MultiblockPortBE part) {
                        part.setControllerPos(worldPosition);
                    }
                }
            }
        }
    }

    private void removeProxyCage(ServerLevel level) {
        Block proxy = ModEntries.get("fusion_reactor_core_proxy").block().get();
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos p = worldPosition.offset(x, y, z);
                    if (level.getBlockState(p).is(proxy)) {
                        level.removeBlock(p, false);
                    }
                }
            }
        }
    }

    @Override
    public int comparatorSignal(int mode) {
        return switch (mode) {
            case FusionPortBE.MODE_ENERGY ->
                    energyStorage != null ? scale(energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored()) : 0;
            case FusionPortBE.MODE_HEAT -> scale((int) reactorHeat, (int) maxHeat);
            case FusionPortBE.MODE_EFFICIENCY -> Math.max(0, Math.min(15, (int) (efficiency * 15)));
            case FusionPortBE.MODE_CHARGE -> scale(functionalBlocksCharge, 100);
            default -> 0;
        };
    }

    @Override
    public void applyRedstoneInput(int mode, int signal) {
        if (mode == FusionPortBE.MODE_SWITCH) {
            boolean active = signal > 0;
            if (active != redstoneActivatedByPort) {
                redstoneActivatedByPort = active;
                markDirty();
            }
        } else if (mode == FusionPortBE.MODE_AMPLIFICATION) {
            int ratio = Math.max(0, Math.min(100, (int) (signal / 0.15)));
            if (ratio != rfAmplificationRatio) {
                rfAmplificationRatio = ratio;
                markDirty();
            }
        }
    }

    private static int scale(int value, int max) {
        if (max <= 0) return 0;
        return Math.max(0, Math.min(15, (int) ((long) value * 15 / max)));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag reactionTag = new CompoundTag();
        reaction.save(reactionTag);
        tag.put("Reaction", reactionTag);
        tag.putBoolean("RedstoneActivated", redstoneActivatedByPort);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Reaction")) reaction.load(tag.getCompound("Reaction"));
        redstoneActivatedByPort = !tag.contains("RedstoneActivated") || tag.getBoolean("RedstoneActivated");
    }
}
