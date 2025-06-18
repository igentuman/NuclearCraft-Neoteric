package igentuman.nc.recipes.serializers;

import com.google.gson.JsonObject;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.TargetChamberRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.debugLog;

public class TargetChamberSerializer<RECIPE extends TargetChamberRecipe> extends NcRecipeSerializer<RECIPE> {

    final ITargetChamberFactory<RECIPE> targetChamberFactory;

    public TargetChamberSerializer(ITargetChamberFactory<RECIPE> factory) {
        super(factory);
        targetChamberFactory = factory;
    }

    public interface ITargetChamberFactory<RECIPE extends NcRecipe>  extends IFactory<RECIPE> {
        @Override
        default RECIPE create(ResourceLocation id,
                      ItemStackIngredient[] inputItems, ItemStackIngredient[] outputItems,
                      FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                      double timeMultiplier, double powerMultiplier, double radiationMultiplier, double rarityMultiplier) {
            throw new UnsupportedOperationException("TargetChamberFactory should use make() method instead of create()");
        }
        
        RECIPE make(ResourceLocation id,
                    ItemStackIngredient[] inputItems, ItemStackIngredient[] outputItems,
                    FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                    ParticleStack[] inputParticles, ParticleStack[] outputParticles,
                    long maxEnergy, double crossSection);
    }

    @Override
    public @NotNull RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
        String type = GsonHelper.getAsString(json, "type");


        ParticleStack[] inputParticles = particleStacksFromJson(json, "inputParticles", recipeId);
        ParticleStack[] outputParticles = particleStacksFromJson(json, "outputParticles", recipeId);
        ItemStackIngredient[] inputItems = inputItemsFromJson(json, recipeId);
        ItemStackIngredient[] outputItems = outputItemsFromJson(json, recipeId);
        FluidStackIngredient[] inputFluids = inputFluidsFromJson(json, recipeId);
        FluidStackIngredient[] outputFluids = outputFluidsFromJson(json, recipeId);

        long maxEnergy = GsonHelper.getAsLong(json, "maxEnergy", Long.MAX_VALUE);
        double crossSection = GsonHelper.getAsDouble(json, "crossSection", 5D);

        return this.targetChamberFactory.make(recipeId, inputItems, outputItems, inputFluids, outputFluids, inputParticles,outputParticles, maxEnergy, crossSection);
    }

    public ParticleStack[] readParticles(@NotNull FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        ParticleStack[] items = new ParticleStack[size];
        for(int i = 0; i < size; i++) {
            items[i] = ParticleStack.readBuffer(buffer);
        }
        return items;
    }

    public ItemStackIngredient[] readItems(@NotNull FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        ItemStackIngredient[] items = new ItemStackIngredient[size];
        for(int i = 0; i < size; i++) {
            items[i] = IngredientCreatorAccess.item().read(buffer);
        }
        return items;
    }

    public FluidStackIngredient[] readFluids(@NotNull FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        FluidStackIngredient[] fluids = new FluidStackIngredient[size];
        for(int i = 0; i < size; i++) {
            fluids[i] = IngredientCreatorAccess.fluid().read(buffer);
        }
        return fluids;
    }

    @Override
    public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        try {

            ItemStackIngredient[] inputItems = readItems(buffer);
            ItemStackIngredient[] outputItems = readItems(buffer);
            FluidStackIngredient[] inputFluids = readFluids(buffer);
            FluidStackIngredient[] outputFluids = readFluids(buffer);
            ParticleStack[] inputParticles = readParticles(buffer);
            ParticleStack[] outputParticles = readParticles(buffer);
            long maxEnergy = buffer.readLong();
            double crossSection = buffer.readDouble();

            return this.targetChamberFactory.make(recipeId, inputItems, outputItems, inputFluids,  outputFluids, inputParticles, outputParticles, maxEnergy, crossSection);
        } catch (Exception e) {
            debugLog("Error reading recipe {} from packet. Trace: {} "+ recipeId + e);
        }
        debugLog("Return empty recipe for: {}" + recipeId);

        //return invalid recipe
        return emptyRecipe(recipeId);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            debugLog("Error writing recipe to packet." + e);
            throw e;
        }
    }
}