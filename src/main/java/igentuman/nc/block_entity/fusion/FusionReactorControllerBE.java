package igentuman.nc.block_entity.fusion;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.client.particle.FusionBeamParticleData;
import igentuman.nc.container.FusionReactorContainer;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.multiblock.fusion.FusionReaction;
import igentuman.nc.multiblock.fusion.FusionReactorCache;
import igentuman.nc.recipe.fusion.FusionRecipe;
import igentuman.nc.recipe.fusion.FusionRecipes;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Controller block entity for the fusion reactor; manages the core cage, runs the reaction, and emits beam particles. */
public class FusionReactorControllerBE extends MultiblockControllerBE {

    public static final int MODE_ENERGY = 0;
    public static final int MODE_HEAT = 1;
    public static final int MODE_EFFICIENCY = 2;
    public static final int MODE_COUNT = 3;
    public static final String[] MODE_KEYS = {"energy", "heat", "efficiency"};

    private final FusionReaction reaction = new FusionReaction();

    private boolean fluidValidatorsReady = false;

    @NBTField(syncToClient = true) public int functionalBlocksCharge;
    @NBTField(syncToClient = true) public int energyPerTick;
    @NBTField(syncToClient = true) public int amplificationAdjustment = 50;
    @NBTField(syncToClient = true) public boolean running;
    @NBTField(syncToClient = true) public int redstoneMode = MODE_HEAT;

    @NBTField public double reactorHeat;
    @NBTField public long plasmaTemperature;
    @NBTField public long chargeAmount;
    @NBTField public double efficiency;
    @NBTField public double maxHeat;
    @NBTField public int inputRedstoneSignal;
    @NBTField public int rfAmplificationRatio;

    public FusionReactorControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Nullable
    public FluidStackHandler fluidTanks() {
        FluidCapabilityHandler fh = contentHandler.getFluidHandler();
        return fh != null ? fh.getInternalHandler() : null;
    }

    /** Restricts fuel input tanks to fluids known to fusion recipes: tank 0 accepts any recipe's
     *  first input fluid, tank 1 any recipe's second input fluid. */
    private void ensureFluidValidators(ServerLevel level) {
        if (fluidValidatorsReady) return;
        FluidStackHandler tanks = fluidTanks();
        if (tanks == null) return;
        List<FluidIngredient> inputA = new ArrayList<>();
        List<FluidIngredient> inputB = new ArrayList<>();
        for (RecipeHolder<FusionRecipe> holder : level.getRecipeManager().getAllRecipesFor(FusionRecipes.FUSION_TYPE.get())) {
            FusionRecipe r = holder.value();
            inputA.add(r.inputA().ingredient());
            inputB.add(r.inputB().ingredient());
        }
        tanks.setTankValidator(0, fs -> anyMatch(inputA, fs));
        tanks.setTankValidator(1, fs -> anyMatch(inputB, fs));
        fluidValidatorsReady = true;
    }

    private static boolean anyMatch(List<FluidIngredient> ingredients, FluidStack fs) {
        for (FluidIngredient ing : ingredients) {
            if (ing.test(fs)) return true;
        }
        return false;
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
        return Math.clamp((rfAmplificationRatio / 100.0) * (amplificationAdjustment / 100.0), 0, 1.0);
    }

    /** GUI slider (1..100) tuning the amplification ratio. */
    public void setAmplificationAdjustment(int value) {
        int clamped = Math.clamp(value, 1, 100);
        if (clamped != amplificationAdjustment) {
            amplificationAdjustment = clamped;
            markDirty();
        }
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ensureFluidValidators(serverLevel);
        tickMultiblock(serverLevel);
        boolean newFormed = mbInstance != null && mbInstance.formed;
        if (formed != newFormed) {
            formed = newFormed;
            wasChanged = true;
        }
        if (formed) {
            updateRedstoneInput(serverLevel);
            if (hasRedstoneSignal() && mbInstance.cache instanceof FusionReactorCache fc) {
                reaction.tick(this, fc);
                if (running && energyPerTick > 0) {
                    renderBeam(serverLevel, fc.size);
                }
            } else {
                reaction.idle(this);
            }
        } else {
            reaction.idle(this);
        }
        if (wasChanged) {
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            wasChanged = false;
        }
    }

    /** Emits four beam particles tracing the plasma ring, one per edge of the square. */
    private void renderBeam(ServerLevel level, int size) {
        BlockPos mid = worldPosition.above();
        float beamLength = size * 2 + 4;
        sendBeamData(level, new FusionBeamParticleData(Direction.EAST, beamLength, 0.35f),
                mid.relative(Direction.NORTH, size + 2).relative(Direction.WEST, size + 2));
        sendBeamData(level, new FusionBeamParticleData(Direction.EAST, beamLength, 0.35f),
                mid.relative(Direction.SOUTH, size + 2).relative(Direction.WEST, size + 2));
        sendBeamData(level, new FusionBeamParticleData(Direction.SOUTH, beamLength, 0.35f),
                mid.relative(Direction.EAST, size + 2).relative(Direction.NORTH, size + 2));
        sendBeamData(level, new FusionBeamParticleData(Direction.SOUTH, beamLength, 0.35f),
                mid.relative(Direction.WEST, size + 2).relative(Direction.NORTH, size + 2));
    }

    private void sendBeamData(ServerLevel level, FusionBeamParticleData data, BlockPos from) {
        Vec3 vec = Vec3.atCenterOf(from);
        for (ServerPlayer player : level.players()) {
            level.sendParticles(player, data, true, vec.x, vec.y, vec.z, 1, 0, 0, 0, 0);
        }
    }

    /** Strongest redstone signal touching any cell of the 3x3x3 core cage. Drives amplification. */
    private void updateRedstoneInput(ServerLevel level) {
        int signal = level.getBestNeighborSignal(worldPosition);
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    signal = Math.max(signal, level.getBestNeighborSignal(worldPosition.offset(x, y, z)));
                }
            }
        }
        int ratio = Math.clamp((int) (signal / 0.15), 0, 100);
        if (signal != inputRedstoneSignal || ratio != rfAmplificationRatio) {
            inputRedstoneSignal = signal;
            rfAmplificationRatio = ratio;
            markDirty();
        }
    }

    public boolean hasRedstoneSignal() {
        return inputRedstoneSignal > 0;
    }

    /** Cycles the comparator output source (energy -> heat -> efficiency). Called from a cage cell. */
    public void toggleRedstoneMode() {
        redstoneMode = (redstoneMode + 1) % MODE_COUNT;
        markDirty();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** Comparator strength (0-15) for the given output mode. */
    public int comparatorSignal(int mode) {
        return switch (mode) {
            case MODE_ENERGY ->
                    energyStorage != null ? scale(energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored()) : 0;
            case MODE_HEAT -> scale((int) reactorHeat, (int) maxHeat);
            case MODE_EFFICIENCY -> Math.clamp((int) (efficiency * 15), 0, 15);
            default -> 0;
        };
    }

    private static int scale(int value, int max) {
        if (max <= 0) return 0;
        return Math.clamp((int) ((long) value * 15 / max), 0, 15);
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

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FusionReactorContainer(containerId, playerInventory, this, containerData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag reactionTag = new CompoundTag();
        reaction.save(reactionTag);
        tag.put("Reaction", reactionTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Reaction")) reaction.load(tag.getCompound("Reaction"));
    }
}
