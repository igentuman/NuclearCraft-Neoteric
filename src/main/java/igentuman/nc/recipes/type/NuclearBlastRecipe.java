package igentuman.nc.recipes.type;

import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.NcRecipeSerializers;
import igentuman.nc.setup.registration.NCBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

public class NuclearBlastRecipe extends NcRecipe {

    public static final String CODE_ID = "nuclear_blast";
    public final double chance;

    public NuclearBlastRecipe(ResourceLocation id, ItemStackIngredient[] inputItems, ItemStackIngredient[] outputItems, double chance) {
        super(id, inputItems, outputItems, 1.0, 1.0, 1.0, 1.0);
        this.chance = chance;
    }

    @Override
    public String getCodeId() {
        return CODE_ID;
    }

    @Override
    public @NotNull String getGroup() {
        return CODE_ID;
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(NCBlocks.PU_239_BOMB_ITEM.get());
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return NcRecipeSerializers.SERIALIZERS.get(CODE_ID).get();
    }

    @Override
    public @NotNull RecipeType<? extends AbstractRecipe> getType() {
        return NcRecipeType.ALL_RECIPES.get(CODE_ID).get();
    }

    public double getChance() {
        return chance;
    }
}
