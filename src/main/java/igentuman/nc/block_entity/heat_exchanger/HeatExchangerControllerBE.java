package igentuman.nc.block_entity.heat_exchanger;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.recipe.RecipeSources;
import igentuman.nc.recipe.heat_exchanger.HeatExchangerRecipes;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.HeatBuffer;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.List;

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

    private RecipeMap validatorRecipes;

    public HeatExchangerControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ensureValidators();
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
        setRadiatorsEnabled(!radiatorsEnabled);
    }

    public void setRadiatorsEnabled(boolean enabled) {
        radiatorsEnabled = enabled;
        setChanged();
    }

    public FluidStackHandler fluidTanks() {
        FluidCapabilityHandler fh = contentHandler.getFluidHandler();
        return fh != null ? fh.getInternalHandler() : null;
    }

    public ResourceHandler<FluidResource> portFluidHandler(boolean hot) {
        FluidStackHandler internal = fluidTanks();
        if (internal == null) return null;
        ensureValidators();
        return hot
                ? internal.createExternalView(new int[]{TANK_HOT_IN}, new int[]{TANK_HOT_OUT})
                : internal.createExternalView(new int[]{TANK_COLD_IN}, new int[]{TANK_COLD_OUT});
    }

    public void ensureValidators() {
        if (level == null) return;
        RecipeMap current = RecipeSources.recipes(level);
        if (validatorRecipes == current) return;
        FluidStackHandler internal = fluidTanks();
        if (internal == null) return;
        List<FluidIngredient> hotInputs = collectInputs(current, true);
        List<FluidIngredient> coldInputs = collectInputs(current, false);
        internal.setTankValidator(TANK_HOT_IN, stack -> hotInputs.stream().anyMatch(input -> input.test(stack)));
        internal.setTankValidator(TANK_COLD_IN, stack -> coldInputs.stream().anyMatch(input -> input.test(stack)));
        validatorRecipes = current;
    }

    private List<FluidIngredient> collectInputs(RecipeMap recipes, boolean hot) {
        return recipes.byType(HeatExchangerRecipes.HX_TYPE.get()).stream()
                .map(holder -> holder.value())
                .filter(recipe -> hot ? recipe.isHot() : recipe.isCold())
                .map(recipe -> recipe.input().ingredient()).toList();
    }

    @Override
    public void clientTick() {
        super.clientTick();
        ensureValidators();
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
