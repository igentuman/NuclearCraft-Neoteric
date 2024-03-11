package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;
import static igentuman.nc.setup.registration.NCProcessors.PROCESSORS_BE;

public class ElectrolyzerBE extends NCProcessorBE<ElectrolyzerBE.Recipe> {
    public ElectrolyzerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.ELECTROLYZER);
    }

    public ElectrolyzerBE() {
        super(PROCESSORS_BE.get(Processors.ELECTROLYZER).get());
    }

    @Override
    public String getName() {
        return Processors.ELECTROLYZER;
    }

    @NothingNullByDefault
    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id,
                      ItemStackIngredient[] input, ItemStack[] output,
                      FluidStackIngredient[] inputFluids, FluidStack[] outputFluids,
                      double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, heatModifier,1);
        }

        @Override
        public String getCodeId() {
            return Processors.ELECTROLYZER;
        }
    }
}
