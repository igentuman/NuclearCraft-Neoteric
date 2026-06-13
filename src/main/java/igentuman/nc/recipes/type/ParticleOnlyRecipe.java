package igentuman.nc.recipes.type;

import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;

/**
 * Base for chamber recipes whose I/O is particles + energy only (no items, no fluids).
 */
@NothingNullByDefault
public abstract class ParticleOnlyRecipe extends NcRecipe {

    public ParticleStack[] inputParticles;
    public ParticleStack[] outputParticles;
    public long maxEnergy;
    public long minEnergy;
    public long energyReleased;
    public double crossSection;

    public ParticleOnlyRecipe(ResourceLocation id,
                              ParticleStack[] inputParticles,
                              ParticleStack[] outputParticles,
                              long minEnergy,
                              long maxEnergy,
                              long energyReleased,
                              double crossSection) {
        super(id);
        this.inputItems = new ItemStackIngredient[0];
        this.outputItems = new ItemStackIngredient[0];
        this.inputFluids = new FluidStackIngredient[0];
        this.outputFluids = new FluidStackIngredient[0];
        this.inputParticles = inputParticles == null ? new ParticleStack[0] : inputParticles;
        this.outputParticles = outputParticles == null ? new ParticleStack[0] : outputParticles;
        this.minEnergy = minEnergy;
        this.maxEnergy = maxEnergy;
        this.energyReleased = energyReleased;
        this.crossSection = crossSection;

        CATALYSTS.put(codeId, List.of(getToastSymbol()));
        RECIPE_CLASSES.put(codeId, getClass());
    }

    @Override
    public boolean isIncomplete() {
        return inputParticles == null || inputParticles.length == 0
                || outputParticles == null || outputParticles.length == 0;
    }

    public ParticleStack getOutputParticle(int i) {
        return outputParticles != null && i < outputParticles.length ? outputParticles[i] : null;
    }

    public ParticleStack getInputParticle(int i) {
        return inputParticles != null && i < inputParticles.length ? inputParticles[i] : null;
    }

    public int requiredInputs() {
        return inputParticles == null ? 0 : inputParticles.length;
    }

    public int producedOutputs() {
        return outputParticles == null ? 0 : outputParticles.length;
    }

    public long getMaxEnergy() {
        return maxEnergy;
    }

    public long getMinEnergy() {
        return minEnergy;
    }

    public long getEnergyReleased() {
        return energyReleased;
    }

    public double getCrossSection() {
        return crossSection;
    }

    public int getAmount() {
        if (inputParticles == null || inputParticles.length == 0) return 10_000;
        return Math.max(1, inputParticles[0].getAmount());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(inputParticles.length);
        for (ParticleStack p : inputParticles) p.writeBuffer(buffer);
        buffer.writeInt(outputParticles.length);
        for (ParticleStack p : outputParticles) p.writeBuffer(buffer);
        buffer.writeLong(minEnergy);
        buffer.writeLong(maxEnergy);
        buffer.writeLong(energyReleased);
        buffer.writeDouble(crossSection);
    }
}
