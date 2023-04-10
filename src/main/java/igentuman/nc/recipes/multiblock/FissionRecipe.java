package igentuman.nc.recipes.multiblock;

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

@NothingNullByDefault
public class FissionRecipe extends ItemStackToItemStackRecipe {

    public FissionRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
        super(id, input, output);
    }

    @Override
    public RecipeType<ItemStackToItemStackRecipe> getType() {
        return NcRecipeType.FISSION.get();
    }

    @Override
    public RecipeSerializer<ItemStackToItemStackRecipe> getSerializer() {
        return NcRecipeSerializers.FISSION.get();
    }

    @Override
    public String getGroup() {
        return FissionReactor.MULTI_BLOCKS.get("fission_reactor_controller").get().getName().getString();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(FissionReactor.MULTI_BLOCKS.get("fission_reactor_controller").get());
    }

    public int getDepletionTime() {
        return ((ItemFuel)getFirstInputStack().getItem()).depletion*20;
    }

    public int getEnergy() {
        return ((ItemFuel)getFirstInputStack().getItem()).forge_energy;
    }

    public double getHeat() {
        return ((ItemFuel)getFirstInputStack().getItem()).heat;
    }

    public double getRadiation() {
        return 0;
    }
}