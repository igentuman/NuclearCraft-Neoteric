package igentuman.nc.recipes.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import igentuman.nc.util.NcUtils;
import net.minecraftforge.fluids.FluidStack;
import org.antlr.v4.runtime.misc.NotNull;;

public abstract class FluidStackIngredient implements InputIngredient<FluidStack> {
    protected int amount;
    public int getAmount() {
        return amount;
    }
}