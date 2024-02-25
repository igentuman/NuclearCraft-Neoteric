package igentuman.nc.recipes.type;

import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;

@NothingNullByDefault
public abstract class NcRecipe extends AbstractRecipe {

    public final double rarityModifier;

    public NcRecipe(
            ResourceLocation id,
            ItemStackIngredient[] inputItems,
            ItemStackIngredient[] outputItems,
            FluidStackIngredient[] inputFluids,
            FluidStackIngredient[] outputFluids,
            double timeModifier,
            double powerModifier,
            double radiationModifier,
            double rarityModifier
    ) {

        super(id);
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
            ResourceLocation id,
            ItemStackIngredient[] inputItems,
            ItemStackIngredient[] outputItems,
            double timeModifier,
            double powerModifier,
            double radiationModifier,
            double rarityModifier
    ) {
        this(id, inputItems, outputItems, new FluidStackIngredient[0], new FluidStackIngredient[0], timeModifier, powerModifier, radiationModifier, rarityModifier);
    }

    public NcRecipe(
            ResourceLocation id,
            FluidStackIngredient[] inputFluids,
            FluidStackIngredient[] outputFluids,
            double timeModifier,
            double powerModifier,
            double radiationModifier,
            double rarityModifier
    ) {
            this(id, new ItemStackIngredient[0], new ItemStackIngredient[0], inputFluids, outputFluids, timeModifier, powerModifier, radiationModifier, rarityModifier);
    }


    @Override
    public void write(FriendlyByteBuf buffer) {

        buffer.writeInt(inputItems.length);
        for (ItemStackIngredient input : inputItems) {
            input.write(buffer);
        }

        buffer.writeInt(outputItems.length);
        for (ItemStackIngredient output : inputItems) {
            output.write(buffer);
        }

        buffer.writeInt(inputFluids.length);
        for (FluidStackIngredient input : inputFluids) {
            input.write(buffer);
        }

        buffer.writeInt(outputFluids.length);
        for (FluidStackIngredient output : outputFluids) {
            output.write(buffer);
        }

        buffer.writeDouble(timeModifier);
        buffer.writeDouble(powerModifier);
        buffer.writeDouble(radiationModifier);
    }
}