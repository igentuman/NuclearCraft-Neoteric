package igentuman.nc.recipes.multiblock;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.setup.multiblocks.FissionReactor;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.*;

public class FissionRecipe extends NcRecipe {

    public FissionRecipe(ResourceLocation id, ItemStackIngredient[] input, ItemStack[] output, FluidStackIngredient[] inputFluids, FluidStack[] outputFluids, double timeModifier, double powerModifier, double heatModifier) {
        super(id, input, output, timeModifier, powerModifier, heatModifier);
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
        return 0;
    }
}