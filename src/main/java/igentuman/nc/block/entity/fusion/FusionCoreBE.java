package igentuman.nc.block.entity.fusion;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;

public class FusionCoreBE <RECIPE extends FusionCoreBE.Recipe> extends FusionBE {
    public FusionCoreBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public FusionCoreBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, getName(pBlockState));
    }

    @Override
    public void tickServer() {
        super.tickServer();
    }
    public static class Recipe extends NcRecipe {

        public Recipe(ResourceLocation id, ItemStackIngredient[] input, ItemStack[] output, FluidStackIngredient[] inputFluids, FluidStack[] outputFluids, double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, timeModifier, powerModifier, heatModifier, rarity);
            ID = FissionControllerBE.NAME;
            CATALYSTS.put(ID, List.of(getToastSymbol()));
        }

        protected ItemFuel fuelItem;

        public ItemFuel getFuelItem() {
            if(fuelItem == null) {
                fuelItem = (ItemFuel) getFirstItemStackIngredient(0).getItem();
            }
            return fuelItem;
        }

        @Override
        public @NotNull String getGroup() {
            return FissionReactor.MULTI_BLOCKS.get(ID).get().getName().getString();
        }

        @Override
        public @NotNull ItemStack getToastSymbol() {
            return new ItemStack(FissionReactor.MULTI_BLOCKS.get(ID).get());
        }

        public int getDepletionTime() {
            return (int) (getFuelItem().depletion*20*timeModifier);
        }

        public double getEnergy() {
            return getFuelItem().forge_energy;
        }

        public double getHeat() {
            return getFuelItem().heat;
        }

        public double getRadiation() {
            return ItemRadiation.byItem(getFuelItem())/10;
        }
    }
}
