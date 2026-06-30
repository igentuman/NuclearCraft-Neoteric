package igentuman.nc.block_entity.fission;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.RedstoneModeController;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.fission.ActiveCoolant;
import igentuman.nc.multiblock.fission.FissionReaction;
import igentuman.nc.multiblock.fission.FissionReactorCache;
import igentuman.nc.util.BoilingBuffer;
import igentuman.nc.util.HeatBuffer;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Controller block entity for the fission reactor. The structure validator (off-thread) produces
 * stats into the {@link FissionReactorCache}; this BE runs the {@link FissionReaction} on the main
 * thread each tick when formed, so all world/buffer mutation stays on the main thread.
 *
 * <p>Two runtime fluid subsystems share the controller tanks: active heat-sink coolant (drained
 * every tick regardless of mode) and the boiling steam mode (heat -> steam, toggled by the player).
 * In steam mode the energy output capability is withheld so the reactor produces steam instead of FE.
 */
public class FissionReactorControllerBE extends MultiblockControllerBE implements RedstoneModeController {

    private static final int TOGGLE_ARM_TICKS = 200;
    private static final int TOGGLE_IDLE_TICKS = 2000;
    private final FissionReaction reaction = new FissionReaction();
    private boolean validatorsReady = false;
    private boolean redstoneActivated = true;
    private double moderationFactor = 1.0;

    @NBTField(syncToClient = true)
    public final HeatBuffer heatBuffer = new HeatBuffer();
    @NBTField(syncToClient = true)
    public final BoilingBuffer boilingBuffer = new BoilingBuffer();
    @NBTField(syncToClient = true) public int energyPerTick;
    @NBTField(syncToClient = true) public int reactivity;
    @NBTField(syncToClient = true) public int steamPerTick;
    @NBTField(syncToClient = true) public boolean steamMode;
    @NBTField(syncToClient = true) public int toggleTimer = TOGGLE_IDLE_TICKS;

    public FissionReactorControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    public HeatBuffer heatBuffer() {
        return heatBuffer;
    }

    public boolean isSteamMode() {
        return steamMode;
    }

    @Nullable
    public FluidStackHandler fluidTanks() {
        FluidCapabilityHandler fh = contentHandler.getFluidHandler();
        return fh != null ? fh.getInternalHandler() : null;
    }

    /** Player request (from GUI button) to switch energy/steam mode after the arming delay. */
    public void requestToggleMode() {
        if (!Multiblocks.fissionSupportsBoiling) return;
        if (toggleTimer >= TOGGLE_IDLE_TICKS) {
            toggleTimer = TOGGLE_ARM_TICKS;
            markDirty();
        }
    }

    public BoilingBuffer boilingBuffer() {
        return boilingBuffer;
    }

    /** Restricts each tank to its intended fluid so coolant routed through a port lands in the
     *  matching active tank and the boiling tank only accepts boilable coolants. */
    private void ensureTankValidators() {
        if (validatorsReady) return;
        FluidCapabilityHandler fh = contentHandler.getFluidHandler();
        if (fh == null) return;
        FluidStackHandler internal = fh.getInternalHandler();
        Fluid water = ModEntries.fluidOf("water");
        Fluid helium = ModEntries.fluidOf("helium");
        internal.setTankValidator(ActiveCoolant.BOILING_INPUT_TANK,
                fs -> fs.getFluid() == water || (helium != null && fs.getFluid() == helium));
        for (ActiveCoolant c : ActiveCoolant.VALUES) {
            Fluid f = ModEntries.fluidOf(c.coolant);
            internal.setTankValidator(c.tankIndex(), fs -> f != null && fs.getFluid() == f);
        }
        validatorsReady = true;
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
        tickToggle();
        if (formed && redstoneActivated && mbInstance.cache instanceof FissionReactorCache fc) {
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

    /** Counts down the arming timer; flips mode at zero, then parks the timer at idle. Mirrors the
     *  legacy 200-tick arm / 2000-tick idle behaviour. */
    private void tickToggle() {
        if (toggleTimer >= TOGGLE_IDLE_TICKS) return;
        toggleTimer--;
        wasChanged = true;
        if (toggleTimer < 1) {
            toggleTimer = TOGGLE_IDLE_TICKS;
            steamMode = !steamMode;
            invalidateStructureCaps();
        }
    }

    /** Energy capability is withheld in steam mode; refresh the controller and every structure
     *  member (ports) so connected machines re-resolve the capability. */
    private void invalidateStructureCaps() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        serverLevel.invalidateCapabilities(worldPosition);
        MultiblockHandler.MultiblockInstance instance = MultiblockHandler.getInstance(serverLevel, worldPosition);
        if (instance != null && instance.cache != null) {
            for (long key : instance.cache.getStructurePositions()) {
                serverLevel.invalidateCapabilities(BlockPos.of(key));
            }
        }
    }

    public int getIrradiativeFlux() {
        if (!(level instanceof ServerLevel sl)) return 0;
        if (mbInstance == null || !mbInstance.formed || !(mbInstance.cache instanceof FissionReactorCache fc)) return 0;
        int lines = fc.irradiationLines;
        if (reaction.currentRecipe == null) return 0;
        int heat = reaction.currentRecipe.heat();
        int depletion = reaction.currentRecipe.processTime();
        double hm = Math.pow(heat, 1.8)/1000D;
        double tm = Math.pow(20000D/depletion, 1.8);
        return (int) (lines * (hm + tm));
    }

    public double getFuelMultiplier() {
        return reactivity / 100.0;
    }

    /** Output throttle (0..1) set by MODERATOR-mode ports; 1.0 when none drive it. */
    public double moderationFactor() {
        return moderationFactor;
    }

    @Override
    public int comparatorSignal(int mode) {
        return switch (mode) {
            case FissionPortBE.MODE_ENERGY ->
                    energyStorage != null ? scale(energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored()) : 0;
            case FissionPortBE.MODE_HEAT -> scale((int) heatBuffer.currentHeat, (int) heatBuffer.capacity);
            case FissionPortBE.MODE_PROGRESS -> scale(progress, 100);
            case FissionPortBE.MODE_ITEMS -> {
                IItemHandler items = getItemHandler(null);
                ItemStack fuel = items != null ? items.getStackInSlot(0) : ItemStack.EMPTY;
                yield fuel.isEmpty() ? 0 : scale(fuel.getCount(), fuel.getMaxStackSize());
            }
            default -> 0;
        };
    }

    @Override
    public void applyRedstoneInput(int mode, int signal) {
        if (mode == FissionPortBE.MODE_SWITCH) {
            boolean active = signal > 0;
            if (active != redstoneActivated) {
                redstoneActivated = active;
                markDirty();
            }
        } else if (mode == FissionPortBE.MODE_MODERATOR) {
            double factor = Math.round(signal / 15.0 * 10) / 10.0;
            if (factor != moderationFactor) {
                moderationFactor = factor;
                markDirty();
            }
        }
    }

    private static int scale(int value, int max) {
        if (max <= 0) return 0;
        return Math.max(0, Math.min(15, (int) ((long) value * 15 / max)));
    }

    @Nullable
    @Override
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return steamMode ? null : super.getEnergyHandler(side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag reactionTag = new CompoundTag();
        reaction.save(reactionTag, registries);
        tag.put("Reaction", reactionTag);
        tag.putBoolean("SteamMode", steamMode);
        tag.putInt("ToggleTimer", toggleTimer);
        tag.putBoolean("RedstoneActivated", redstoneActivated);
        tag.putDouble("ModerationFactor", moderationFactor);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Reaction")) reaction.load(tag.getCompound("Reaction"), registries);
        steamMode = tag.getBoolean("SteamMode");
        toggleTimer = tag.contains("ToggleTimer") ? tag.getInt("ToggleTimer") : TOGGLE_IDLE_TICKS;
        redstoneActivated = !tag.contains("RedstoneActivated") || tag.getBoolean("RedstoneActivated");
        moderationFactor = tag.contains("ModerationFactor") ? tag.getDouble("ModerationFactor") : 1.0;
    }
}
