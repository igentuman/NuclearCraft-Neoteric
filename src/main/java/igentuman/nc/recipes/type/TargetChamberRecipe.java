package igentuman.nc.recipes.type;

import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;
import static net.minecraft.world.item.Items.BARRIER;

@NothingNullByDefault
public abstract class TargetChamberRecipe extends NcRecipe {

    public long maxEnergy;
    public double crossSection;
    public ParticleStack[] inputParticles;
    public ParticleStack[] outputParticles;

    public TargetChamberRecipe(
            ResourceLocation id,
            ItemStackIngredient[] inputItems,
            ItemStackIngredient[] outputItems,
            FluidStackIngredient[] inputFluids,
            FluidStackIngredient[] outputFluids,
            ParticleStack[] inputParticles,
            ParticleStack[] outputParticles,
            long maxEnergy,
            double crossSection
    ) {

        super(id);
        this.inputItems = inputItems;
        this.outputItems = outputItems;
        this.inputFluids = inputFluids;
        this.outputFluids = outputFluids;
        this.inputParticles = inputParticles;
        this.outputParticles = outputParticles;
        this.maxEnergy = maxEnergy;
        this.crossSection = crossSection;

        CATALYSTS.put(codeId, List.of(getToastSymbol()));
        RECIPE_CLASSES.put(codeId, getClass());
    }

    protected FluidStackIngredient getEmptyFluid()
    {
        return IngredientCreatorAccess.fluid().from(FluidStack.EMPTY);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
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

        buffer.writeInt(inputParticles.length);
        for (ParticleStack input : inputParticles) {
            input.writeBuffer(buffer);
        }

        buffer.writeInt(outputParticles.length);
        for (ParticleStack input : outputParticles) {
            input.writeBuffer(buffer);
        }

        buffer.writeLong(maxEnergy);
        buffer.writeDouble(crossSection);
    }
}