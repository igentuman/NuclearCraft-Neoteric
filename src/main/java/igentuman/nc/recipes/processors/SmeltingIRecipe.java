package igentuman.nc.recipes.processors;

import igentuman.nc.recipes.type.ItemStackToItemStackRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.setup.recipes.NcRecipeSerializers;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import static igentuman.nc.compat.GlobalVars.*;
@NothingNullByDefault
public class SmeltingIRecipe extends ItemStackToItemStackRecipe {
    public static String ID = "smelting";

    public SmeltingIRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output, double timeModifier, double powerModifier, double heatModifier) {
        super(id, input, new ItemStack[]{output}, timeModifier, powerModifier, heatModifier);
        RECIPE_CLASSES.put(ID, this.getClass());
        CATALYSTS.put(ID, List.of(getToastSymbol()));
    }

    @Override
    public @NotNull RecipeType<ItemStackToItemStackRecipe> getType() {
        return NcRecipeType.SMELTING.get();
    }

    @Override
    public @NotNull RecipeSerializer<ItemStackToItemStackRecipe> getSerializer() {
        return NcRecipeSerializers.SERIALIZERS.get(ID).get();
    }

    @Override
    public @NotNull String getGroup() {
        return NCProcessors.PROCESSORS.get("nuclear_furnace").get().getName().getString();
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(NCProcessors.PROCESSORS.get("nuclear_furnace").get());
    }
}