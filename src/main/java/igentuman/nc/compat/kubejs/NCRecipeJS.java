package igentuman.nc.compat.kubejs;

import com.google.gson.JsonObject;
import dev.architectury.fluid.FluidStack;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.recipe.*;

import static igentuman.nc.util.TagUtil.getFirstMatchingFluidByTag;

public class NCRecipeJS extends RecipeJS {

    @Override
    public InputFluid readInputFluid(Object from) {
        if(from instanceof JsonObject jsonObj) {
            if (jsonObj.has("tag")){
               FluidStack fluid = FluidStack.create(getFirstMatchingFluidByTag(jsonObj.get("tag").getAsString()), jsonObj.get("amount").getAsInt());
               return FluidStackJS.of(fluid);
            }
        }

        return super.readInputFluid(from);
    }

    @Override
    public OutputFluid readOutputFluid(Object from) {
        if(from instanceof JsonObject jsonObj) {
            if (jsonObj.has("tag")){
                FluidStack fluid = FluidStack.create(getFirstMatchingFluidByTag(jsonObj.get("tag").getAsString()), jsonObj.get("amount").getAsInt());
                return FluidStackJS.of(fluid);
            }
        }
        return super.readOutputFluid(from);
    }
}
