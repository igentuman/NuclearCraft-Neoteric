package igentuman.nc.recipes.type;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;

public class OreVeinRecipe extends NcRecipe {
    public OreVeinRecipe(ResourceLocation id, ItemStackIngredient[] input, ItemStack[] output, FluidStackIngredient[] inputFluids, FluidStack[] outputFluids, double timeModifier, double powerModifier, double radiation, double rarityModifier) {
        super(id, input, output, timeModifier, powerModifier, radiation, rarityModifier);
        ID = "nc_ore_veins";
        CATALYSTS.put(ID, List.of(getToastSymbol()));
    }

    @Override
    public @NotNull String getGroup() {
        return ID;
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(NC_PARTS.get("research_paper").get());
    }
}
