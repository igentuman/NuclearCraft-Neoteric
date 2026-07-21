package igentuman.nc.block_entity.heat_exchanger;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.multiblock.heat_exchanger.HeatExchangerCache;
import igentuman.nc.recipe.heat_exchanger.HeatExchangerRecipe;
import igentuman.nc.recipe.heat_exchanger.HeatExchangerRecipes;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.HeatBuffer;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.HashSet;
import java.util.Set;

import static net.minecraft.world.level.block.Block.UPDATE_CLIENTS;
import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;

public class HeatExchangerControllerBE extends MultiblockControllerBE {

    public static final int TANK_HOT_IN = 0;
    public static final int TANK_COLD_IN = 1;
    public static final int TANK_HOT_OUT = 2;
    public static final int TANK_COLD_OUT = 3;

    @NBTField(syncToClient = true)
    public int heatExchangers = 0;
    @NBTField(syncToClient = true)
    public int radiators = 0;
    @NBTField(syncToClient = true)
    public int hotCycleOps = 0;
    @NBTField(syncToClient = true)
    public int coldCycleOps = 0;
    @NBTField(syncToClient = true)
    public boolean radiatorsEnabled = false;
    @NBTField(syncToClient = true)
    public final HeatBuffer heatBuffer = new HeatBuffer();

    private int lastN = -1;
    private boolean validatorsReady = false;

    public HeatExchangerControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
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

        if (formed && mbInstance != null && mbInstance.cache instanceof HeatExchangerCache hc) {
            applyCache(hc);
            heatBuffer.heatPerTick = 0;
            heatBuffer.cooldownPerTick = 0;
            coolDown();
            boolean powered = heatExchangers > 0
                    && serverLevel.hasNeighborSignal(worldPosition)
                    && energyStorage != null
                    && energyStorage.getEnergyStored() >= energyPerTick();
            if (powered) {
                processLoops();
            } else if (hotCycleOps != 0 || coldCycleOps != 0) {
                hotCycleOps = 0;
                coldCycleOps = 0;
                wasChanged = true;
            }
        } else {
            clearStats();
        }

        if (wasChanged) {
            assert getLevel() != null;
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), UPDATE_CLIENTS);
            wasChanged = false;
        }
    }

    private void applyCache(HeatExchangerCache hc) {
        if (heatExchangers != hc.heatExchangers || radiators != hc.radiators) {
            heatExchangers = hc.heatExchangers;
            radiators = hc.radiators;
            wasChanged = true;
        }
        ensureValidators();
        if (heatExchangers != lastN) {
            lastN = heatExchangers;
            int perBlock = Multiblocks.hxFluidCapacityPerBlock;
            int cap = Math.max(perBlock, (heatExchangers + 1) * perBlock);
            FluidStackHandler tanks = fluidTanks();
            if (tanks != null) {
                for (int i = 0; i < 4; i++) tanks.setTankCapacity(i, cap);
            }
            heatBuffer.setCapacity((double) heatExchangers * Multiblocks.hxHeatCapacityPerBlock);
            if (heatBuffer.currentHeat > heatBuffer.capacity) heatBuffer.currentHeat = heatBuffer.capacity;
            wasChanged = true;
        }
    }

    private void clearStats() {
        lastN = -1;
        validatorsReady = false;
        if (heatExchangers != 0 || radiators != 0 || hotCycleOps != 0 || coldCycleOps != 0) {
            heatExchangers = 0;
            radiators = 0;
            hotCycleOps = 0;
            coldCycleOps = 0;
            wasChanged = true;
        }
    }

    private void coolDown() {
        if (!radiatorsEnabled || radiators <= 0) return;
        double removed = (double) radiators * Multiblocks.hxRadiatorCooling;
        heatBuffer.cooldownPerTick += removed;
        if (heatBuffer.currentHeat <= 0) return;
        double newHeat = Math.max(0, heatBuffer.currentHeat - removed);
        if (newHeat != heatBuffer.currentHeat) {
            heatBuffer.currentHeat = newHeat;
            wasChanged = true;
        }
    }

    private void processLoops() {
        hotCycleOps = 0;
        coldCycleOps = 0;
        boolean hotRan = processSide(TANK_HOT_IN, TANK_HOT_OUT, true);
        boolean coldRan = processSide(TANK_COLD_IN, TANK_COLD_OUT, false);
        if ((hotRan || coldRan) && energyStorage != null) {
            energyStorage.drainEnergy(energyPerTick());
            wasChanged = true;
        }
    }

    private boolean processSide(int inTank, int outTank, boolean hot) {
        FluidStackHandler tanks = fluidTanks();
        if (tanks == null) return false;
        FluidStack in = tanks.getFluidInTank(inTank);
        if (in.isEmpty()) return false;
        HeatExchangerRecipe r = findRecipe(in, hot);
        if (r == null) return false;

        int inAmount = r.input().amount();
        FluidStack outTemplate = r.output().resolve();
        if (inAmount <= 0 || outTemplate.isEmpty()) return false;
        int outAmount = outTemplate.getAmount();
        int recipeHeat = r.heat();

        double throughput = heatExchangers * Multiblocks.hxThroughputPerBlock;
        long ops = (long) Math.floor(throughput / inAmount);
        ops = Math.min(ops, in.getAmount() / (long) inAmount);
        if (outAmount > 0) {
            int outRoom = tanks.getTankCapacity(outTank) - tanks.getFluidInTank(outTank).getAmount();
            ops = Math.min(ops, outRoom / (long) outAmount);
        }
        if (recipeHeat > 0) {
            ops = Math.min(ops, (long) ((heatBuffer.capacity - heatBuffer.currentHeat) / recipeHeat));
        } else if (recipeHeat < 0) {
            ops = Math.min(ops, (long) (heatBuffer.currentHeat / -recipeHeat));
        }
        if (ops <= 0) return false;

        int fillAmount = (int) (ops * outAmount);
        FluidStack toOutput = new FluidStack(outTemplate.getFluid(), fillAmount);
        if (tanks.fillTank(outTank, toOutput, SIMULATE) < fillAmount) return false;

        tanks.drainTank(inTank, (int) (ops * inAmount), EXECUTE);
        tanks.fillTank(outTank, toOutput, EXECUTE);
        if (recipeHeat > 0) {
            heatBuffer.currentHeat = Math.min(heatBuffer.capacity, heatBuffer.currentHeat + (double) ops * recipeHeat);
            heatBuffer.heatPerTick += (double) ops * recipeHeat;
        } else {
            heatBuffer.currentHeat = Math.max(0, heatBuffer.currentHeat + (double) ops * recipeHeat);
            heatBuffer.cooldownPerTick += (double) ops * -recipeHeat;
        }
        if (hot) hotCycleOps = (int) ops;
        else coldCycleOps = (int) ops;
        wasChanged = true;
        return true;
    }

    private HeatExchangerRecipe findRecipe(FluidStack in, boolean hot) {
        if (!(level instanceof ServerLevel sl)) return null;
        for (RecipeHolder<HeatExchangerRecipe> holder : sl.getRecipeManager().getAllRecipesFor(HeatExchangerRecipes.HX_TYPE.get())) {
            HeatExchangerRecipe r = holder.value();
            if (hot ? !r.isHot() : !r.isCold()) continue;
            if (r.input().test(in)) return r;
        }
        return null;
    }

    public int energyPerTick() {
        return heatExchangers * Multiblocks.hxEnergyPerBlock;
    }

    public boolean isRadiatorsEnabled() {
        return radiatorsEnabled;
    }

    public void toggleRadiators() {
        radiatorsEnabled = !radiatorsEnabled;
        setChanged();
    }

    private FluidStackHandler fluidTanks() {
        FluidCapabilityHandler fh = contentHandler.getFluidHandler();
        return fh != null ? fh.getInternalHandler() : null;
    }

    public IFluidHandler portFluidHandler(boolean hot) {
        FluidStackHandler internal = fluidTanks();
        if (internal == null) return null;
        ensureValidators();
        return hot
                ? internal.createExternalView(new int[]{TANK_HOT_IN}, new int[]{TANK_HOT_OUT})
                : internal.createExternalView(new int[]{TANK_COLD_IN}, new int[]{TANK_COLD_OUT});
    }

    private void ensureValidators() {
        if (validatorsReady) return;
        if (!(level instanceof ServerLevel sl)) return;
        FluidStackHandler internal = fluidTanks();
        if (internal == null) return;
        Set<Fluid> hotInputs = collectInputs(sl, true);
        Set<Fluid> coldInputs = collectInputs(sl, false);
        internal.setTankValidator(TANK_HOT_IN, fs -> hotInputs.contains(fs.getFluid()));
        internal.setTankValidator(TANK_COLD_IN, fs -> coldInputs.contains(fs.getFluid()));
        validatorsReady = true;
    }

    private Set<Fluid> collectInputs(ServerLevel sl, boolean hot) {
        Set<Fluid> fluids = new HashSet<>();
        for (RecipeHolder<HeatExchangerRecipe> holder : sl.getRecipeManager().getAllRecipesFor(HeatExchangerRecipes.HX_TYPE.get())) {
            HeatExchangerRecipe r = holder.value();
            if (hot ? !r.isHot() : !r.isCold()) continue;
            for (FluidStack fs : r.input().getFluids()) fluids.add(fs.getFluid());
        }
        return fluids;
    }

    @Override
    public HeatBuffer heatBuffer() {
        return heatBuffer;
    }

    public double getHeat() {
        return heatBuffer.currentHeat;
    }

    public double getMaxHeat() {
        return heatBuffer.capacity;
    }
}
