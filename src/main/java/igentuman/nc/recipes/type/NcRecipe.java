package igentuman.nc.recipes.type;

import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;
import static net.minecraft.world.item.Items.BARRIER;

@NothingNullByDefault
public abstract class NcRecipe extends AbstractRecipe {

    public double rarityModifier;

    public NcRecipe(
            String codeId,
            ItemStackIngredient[] inputItems,
            ItemStackIngredient[] outputItems,
            FluidStackIngredient[] inputFluids,
            FluidStackIngredient[] outputFluids,
            double timeModifier,
            double powerModifier,
            double radiationModifier,
            double rarityModifier
    ) {

        super(codeId);
        this.inputItems = inputItems;
        this.outputItems = outputItems;
        this.inputFluids = inputFluids;
        this.outputFluids = outputFluids;

        this.timeModifier = timeModifier;
        this.powerModifier = powerModifier;
        this.radiationModifier = radiationModifier;
        this.rarityModifier = rarityModifier;
        CATALYSTS.put(codeId, List.of(getToastSymbol()));
        RECIPE_CLASSES.put(codeId, getClass());
    }


    public NcRecipe(
            String codeId,
            ItemStackIngredient[] inputItems,
            ItemStackIngredient[] outputItems,
            double timeModifier,
            double powerModifier,
            double radiationModifier,
            double rarityModifier
    ) {
        this(codeId, inputItems, outputItems, new FluidStackIngredient[0], new FluidStackIngredient[0], timeModifier, powerModifier, radiationModifier, rarityModifier);
    }

    public NcRecipe(
            String codeId,
            FluidStackIngredient[] inputFluids,
            FluidStackIngredient[] outputFluids,
            double timeModifier,
            double powerModifier,
            double radiationModifier,
            double rarityModifier
    ) {
            this(codeId, new ItemStackIngredient[0], new ItemStackIngredient[0], inputFluids, outputFluids, timeModifier, powerModifier, radiationModifier, rarityModifier);
    }

    public NcRecipe(String codeId) {
        super(codeId);
    }

    public static ItemStackIngredient getBarrier()
    {
        return IngredientCreatorAccess.item().from(new ItemStack(BARRIER));
    }

    protected FluidStackIngredient getEmptyFluid()
    {
        return IngredientCreatorAccess.fluid().from(FluidStack.EMPTY);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(inputItems.length);
        for (ItemStackIngredient input : inputItems) {
            if(input == null || input.getRepresentations().isEmpty()) {
                input = getBarrier();
            }
            input.write(buffer);
        }

        buffer.writeInt(outputItems.length);
        for (ItemStackIngredient output : outputItems) {
            if(output == null || output.getRepresentations().isEmpty()) {
                output = getBarrier();
            }
            output.write(buffer);
        }

        buffer.writeInt(inputFluids.length);
        for (FluidStackIngredient input : inputFluids) {
            if(input == null) {
                input = getEmptyFluid();
            }
            input.write(buffer);
        }

        buffer.writeInt(outputFluids.length);
        for (FluidStackIngredient output : outputFluids) {
            if(output == null) {
                output = getEmptyFluid();
            }
            output.write(buffer);
        }

        buffer.writeDouble(timeModifier);
        buffer.writeDouble(powerModifier);
        buffer.writeDouble(radiationModifier);
    }
}