package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
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

public class AssemblerBE extends NCProcessorBE<AssemblerBE.Recipe> {
    public AssemblerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.ASSEMBLER);
    }
    @Override
    public String getName() {
        return Processors.ASSEMBLER;
    }

    @NothingNullByDefault
    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id,
                                  ItemStackIngredient[] input, ItemStack[] output,
                                  FluidStackIngredient[] inputFluids, FluidStack[] outputFluids,
                                  double timeModifier, double powerModifier, double heatModifier) {
            super(id, input, output, timeModifier, powerModifier, heatModifier);
            ID = Processors.ASSEMBLER;
            RECIPE_CLASSES.put(ID, this.getClass());
            CATALYSTS.put(ID, List.of(getToastSymbol()));
        }
    }
}
