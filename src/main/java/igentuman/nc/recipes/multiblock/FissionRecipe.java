package igentuman.nc.recipes.multiblock;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.recipes.ItemStackToItemStackRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.setup.multiblocks.FissionReactor;
import igentuman.nc.setup.recipes.NcRecipeSerializers;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.*;

@NothingNullByDefault
public class FissionRecipe extends ItemStackToItemStackRecipe {

    public static String ID = FissionControllerBE.NAME;

    public FissionRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output, double timeModifier, double powerModifier, double heatModifier) {
        super(id, input, output, timeModifier, powerModifier, heatModifier);
        CATALYSTS.put(ID, List.of(getToastSymbol()));
    }

    @Override
    public RecipeType<ItemStackToItemStackRecipe> getType() {
        return NcRecipeType.RECIPES.get(ID).get();
    }

    @Override
    public RecipeSerializer<ItemStackToItemStackRecipe> getSerializer() {
        return NcRecipeSerializers.SERIALIZERS.get(ID).get();
    }

    @Override
    public String getGroup() {
        return FissionReactor.MULTI_BLOCKS.get(ID).get().getName().getString();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(FissionReactor.MULTI_BLOCKS.get(ID).get());
    }

    public int getDepletionTime() {
        return (int) (((ItemFuel)getFirstInputStack().getItem()).depletion*20*timeModifier);
    }

    public double getEnergy() {
        return ((ItemFuel)getFirstInputStack().getItem()).forge_energy;
    }

    public double getHeat() {
        return ((ItemFuel)getFirstInputStack().getItem()).heat;
    }

    public double getRadiation() {
        return 0;
    }
}