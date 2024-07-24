package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;

public class GasScrubberBE extends NCProcessorBE<GasScrubberBE.Recipe> {
    public GasScrubberBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.GAS_SCRUBBER);
    }
    @Override
    public String getName() {
        return Processors.GAS_SCRUBBER;
    }

    protected void processRecipe() {
        if(!hasRecipe()) {
            updateRecipe();
        }
        if(!hasRecipe()) {
            isActive = false;
            return;
        }

        if(energyStorage.getEnergyStored() < energyPerTick()*skippedTicks) {
            isActive = false;
            return;
        }
        if(!canProcessRecipe()) {
            return;
        }
        recipeInfo.process(speedMultiplier()*skippedTicks);
        if(recipeInfo.radiation != 1D) {
            for(int x = -1; x <= 1; x++) {
                for(int z = -1; z <= 1; z++) {
                    RadiationManager.get(getLevel()).addRadiation(getLevel(), (recipeInfo.radiation/1000000)*speedMultiplier()*skippedTicks, worldPosition.getX()+x*16, worldPosition.getY(), worldPosition.getZ()+z*16);
                }
            }
        }
        isActive = true;
        setChanged();
        if(!recipeInfo.isCompleted() && hasRecipe()) {
            energyStorage.consumeEnergy(energyPerTick()*skippedTicks);
        }
    }

    @NothingNullByDefault
    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id,
                      ItemStackIngredient[] input, ItemStackIngredient[] output,
                      FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                      double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, 1);
        }

        @Override
        public String getCodeId() {
            return Processors.GAS_SCRUBBER;
        }
    }
}
