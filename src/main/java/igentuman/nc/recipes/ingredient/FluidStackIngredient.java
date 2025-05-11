package igentuman.nc.recipes.ingredient;

import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public abstract class FluidStackIngredient implements InputIngredient<@NotNull FluidStack> {

    protected int amount;

    public int getAmount() {
        if(amount == 0) {
            try {
                amount = getRepresentations().get(0).getAmount();
            } catch (Exception ignored) {}
        }
        return amount;
    }

    public FluidStackIngredient copy() {
        return IngredientCreatorAccess.fluid().from(this.serialize());
    }

    public void setAmount(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = i;
    }
}