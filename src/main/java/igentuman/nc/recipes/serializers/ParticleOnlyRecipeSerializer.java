package igentuman.nc.recipes.serializers;

import com.google.gson.JsonObject;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.ParticleOnlyRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.debugLog;

/**
 * Serializer for chamber recipes whose I/O is particles + energy only.
 */
public class ParticleOnlyRecipeSerializer<RECIPE extends ParticleOnlyRecipe> extends NcRecipeSerializer<RECIPE> {

    private final IParticleOnlyFactory<RECIPE> particleFactory;

    public ParticleOnlyRecipeSerializer(IParticleOnlyFactory<RECIPE> factory) {
        super(factory);
        this.particleFactory = factory;
    }

    public interface IParticleOnlyFactory<RECIPE extends NcRecipe> extends IFactory<RECIPE> {
        @Override
        default RECIPE create(ResourceLocation id,
                              ItemStackIngredient[] inputItems, ItemStackIngredient[] outputItems,
                              FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                              double timeMultiplier, double powerMultiplier, double radiationMultiplier, double rarityMultiplier) {
            throw new UnsupportedOperationException("ParticleOnlyFactory uses make()");
        }

        RECIPE make(ResourceLocation id,
                    ParticleStack[] inputParticles, ParticleStack[] outputParticles,
                    long minEnergy, long maxEnergy, long energyReleased, double crossSection);
    }

    @Override
    public @NotNull RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
        ParticleStack[] inputParticles = particleStacksFromJson(json, "inputParticles", recipeId);
        ParticleStack[] outputParticles = particleStacksFromJson(json, "outputParticles", recipeId);
        long minEnergy = GsonHelper.getAsLong(json, "minEnergy", 0L);
        long maxEnergy = GsonHelper.getAsLong(json, "maxEnergy", Long.MAX_VALUE);
        long energyReleased = GsonHelper.getAsLong(json, "energyReleased", 0L);
        double crossSection = GsonHelper.getAsDouble(json, "crossSection", 1D);
        return particleFactory.make(recipeId, inputParticles, outputParticles, minEnergy, maxEnergy, energyReleased, crossSection);
    }

    public ParticleStack[] readParticles(@NotNull FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        ParticleStack[] items = new ParticleStack[size];
        for (int i = 0; i < size; i++) {
            items[i] = ParticleStack.readBuffer(buffer);
        }
        return items;
    }

    @Override
    public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        try {
            ParticleStack[] inputParticles = readParticles(buffer);
            ParticleStack[] outputParticles = readParticles(buffer);
            long minEnergy = buffer.readLong();
            long maxEnergy = buffer.readLong();
            long energyReleased = buffer.readLong();
            double crossSection = buffer.readDouble();
            return particleFactory.make(recipeId, inputParticles, outputParticles, minEnergy, maxEnergy, energyReleased, crossSection);
        } catch (Exception e) {
            debugLog("Error reading particle-only recipe {} from packet: " + recipeId + " " + e);
        }
        return emptyRecipe(recipeId);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        recipe.write(buffer);
    }
}
