package igentuman.nc.recipes.type;

import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@NothingNullByDefault
public abstract class NcRecipe extends AbstractRecipe {

    public NcRecipe(
            ResourceLocation id,
            ItemStackIngredient[] inputItems,
            ItemStack[] outputItems,
            FluidStackIngredient[] inputFluids,
            FluidStack[] outputFluids,
            double timeModifier,
            double powerModifier,
            double radiationModifier
    ) {

        super(id);
        this.inputItems = inputItems;
        this.outputItems = outputItems;
        this.inputFluids = inputFluids;
        this.outputFluids = outputFluids;

        this.timeModifier = timeModifier;
        this.powerModifier = powerModifier;
        this.radiationModifier = radiationModifier;
    }

    public NcRecipe(
            ResourceLocation id,
            ItemStackIngredient[] inputItems,
            ItemStack[] outputItems,
            double timeModifier,
            double powerModifier,
            double radiationModifier
    ) {

        this(id, inputItems, outputItems, new FluidStackIngredient[0], new FluidStack[0], timeModifier, powerModifier, radiationModifier);
    }

    public NcRecipe(
            ResourceLocation id,
            FluidStackIngredient[] inputFluids,
            FluidStack[] outputFluids,
            double timeModifier,
            double powerModifier,
            double radiationModifier
    ) {
            this(id, new ItemStackIngredient[0], new ItemStack[0], inputFluids, outputFluids, timeModifier, powerModifier, radiationModifier);
    }


    @Override
    public void write(FriendlyByteBuf buffer) {

        buffer.writeInt(inputItems.length);
        for (ItemStackIngredient input : inputItems) {
            input.write(buffer);
        }

        buffer.writeInt(outputItems.length);
        for (ItemStack output : outputItems) {
            buffer.writeItem(output);
        }

        buffer.writeInt(inputFluids.length);
        for (FluidStackIngredient input : inputFluids) {
            input.write(buffer);
        }

        buffer.writeInt(outputFluids.length);
        for (FluidStack output : outputFluids) {
            buffer.writeFluidStack(output);
        }
    }

}