package igentuman.nc.block_entity.heat_exchanger;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
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

    private boolean validatorsReady = false;

    public HeatExchangerControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        tickMultiblock(serverLevel);
        boolean newFormed = mbInstance != null && mbInstance.formed && !mbInstance.dirty;
        if (formed != newFormed) {
            formed = newFormed;
            wasChanged = true;
        }
        if (!formed) clearStats();
        if (wasChanged) {
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), UPDATE_CLIENTS);
            wasChanged = false;
        }
    }

    public void clearStats() {
        if (heatExchangers != 0 || radiators != 0 || hotCycleOps != 0 || coldCycleOps != 0) {
            heatExchangers = 0;
            radiators = 0;
            hotCycleOps = 0;
            coldCycleOps = 0;
            wasChanged = true;
        }
    }

    public void updateRuntimeDisplay(HeatBuffer source, int hotOps, int coldOps) {
        if (heatBuffer.capacity != source.capacity || heatBuffer.currentHeat != source.currentHeat
                || heatBuffer.heatPerTick != source.heatPerTick || heatBuffer.cooldownPerTick != source.cooldownPerTick
                || hotCycleOps != hotOps || coldCycleOps != coldOps) {
            heatBuffer.capacity = source.capacity;
            heatBuffer.currentHeat = source.currentHeat;
            heatBuffer.heatPerTick = source.heatPerTick;
            heatBuffer.cooldownPerTick = source.cooldownPerTick;
            hotCycleOps = hotOps;
            coldCycleOps = coldOps;
            markDirty();
        }
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

    public FluidStackHandler fluidTanks() {
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

    public void ensureValidators() {
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
