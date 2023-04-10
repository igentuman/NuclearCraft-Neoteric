package igentuman.nc.recipes.processors;

import igentuman.nc.recipes.ItemStackToItemStackRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.setup.recipes.NcRecipeSerializers;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;


@NothingNullByDefault
public class SmeltingIRecipe extends ItemStackToItemStackRecipe {

    public SmeltingIRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
        super(id, input, output);
    }

    @Override
    public RecipeType<ItemStackToItemStackRecipe> getType() {
        return NcRecipeType.SMELTING.get();
    }

    @Override
    public RecipeSerializer<ItemStackToItemStackRecipe> getSerializer() {
        return NcRecipeSerializers.SMELTING.get();
    }

    @Override
    public String getGroup() {
        return NCProcessors.PROCESSORS.get("nuclear_furnace").get().getName().getString();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(NCProcessors.PROCESSORS.get("nuclear_furnace").get());
    }
}