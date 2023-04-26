package igentuman.nc.recipes.processors;

import igentuman.nc.recipes.ItemStackToItemStackRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.ItemStackIngredientCreator;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.recipes.NcRecipeSerializers;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import static igentuman.nc.compat.GlobalVars.*;
@NothingNullByDefault
public class ManufactoryRecipe extends ItemStackToItemStackRecipe {

    public static String ID = Processors.MANUFACTORY;
    public ManufactoryRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output, double timeModifier, double powerModifier, double heatModifier) {
        super(id, input, output, timeModifier, powerModifier, heatModifier);
        RECIPE_CLASSES.put(ID, this.getClass());
        CATALYSTS.put(ID, List.of(getToastSymbol()));
    }

    public ManufactoryRecipe(ResourceLocation id) {
        super(id, ItemStackIngredientCreator.INSTANCE.from(Ingredient.EMPTY, 1), ItemStack.EMPTY, 1, 1, 1);
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
        return NCProcessors.PROCESSORS.get(ID).get().getName().getString();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(NCProcessors.PROCESSORS.get(ID).get());
    }

    public double getEnergy() {
        return Processors.all().get(ID).config().getPower();
    }

    public int getDuration() {
        return Processors.all().get(ID).config().getTime();
    }

    public double getHeat() {
        return 0;
    }

    public double getRadiation() {
        return 0;
    }
}