package igentuman.nc.recipes.serializers;

import com.google.gson.JsonObject;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.TargetChamberRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
        default RECIPE create(String codeId,
                      ItemStackIngredient[] inputItems, ItemStackIngredient[] outputItems,
                      FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                      double timeMultiplier, double powerMultiplier, double radiationMultiplier, double extraModifier) {
            throw new UnsupportedOperationException("TargetChamberFactory should use make() method instead of create()");
        }

        RECIPE make(String codeId,
                    ItemStackIngredient[] inputItems, ItemStackIngredient[] outputItems,
                    FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                    ParticleStack[] inputParticles, ParticleStack[] outputParticles,
                    long maxEnergy, double crossSection);
    }

    @Override
    protected @NotNull RECIPE fromJson(@NotNull JsonObject json) {
        String type = GsonHelper.getAsString(json, "type");

        ParticleStack[] inputParticles = particleStacksFromJson(json, "inputParticles");
        ParticleStack[] outputParticles = particleStacksFromJson(json, "outputParticles");
        ItemStackIngredient[] inputItems = inputItemsFromJson(json);
        ItemStackIngredient[] outputItems = outputItemsFromJson(json);
        FluidStackIngredient[] inputFluids = inputFluidsFromJson(json);
        FluidStackIngredient[] outputFluids = outputFluidsFromJson(json);

        long maxEnergy = GsonHelper.getAsLong(json, "maxEnergy", Long.MAX_VALUE);
        double crossSection = GsonHelper.getAsDouble(json, "crossSection", 5D);

        return this.targetChamberFactory.make(getCodeId(), inputItems, outputItems, inputFluids, outputFluids, inputParticles, outputParticles, maxEnergy, crossSection);
    }

    public ParticleStack[] readParticles(@NotNull RegistryFriendlyByteBuf buffer) {
        int size = buffer.readInt();
        ParticleStack[] items = new ParticleStack[size];
        for(int i = 0; i < size; i++) {
            items[i] = ParticleStack.readBuffer(buffer);
        }
        return items;
    }

    @Override
    public ItemStackIngredient[] readItems(@NotNull RegistryFriendlyByteBuf buffer) {
        int size = buffer.readInt();
        ItemStackIngredient[] items = new ItemStackIngredient[size];
        for(int i = 0; i < size; i++) {
            items[i] = IngredientCreatorAccess.item().read(buffer);
        }
        return items;
    }

    @Override
    public FluidStackIngredient[] readFluids(@NotNull RegistryFriendlyByteBuf buffer) {
        int size = buffer.readInt();
        FluidStackIngredient[] fluids = new FluidStackIngredient[size];
        for(int i = 0; i < size; i++) {
            fluids[i] = IngredientCreatorAccess.fluid().read(buffer);
        }
        return fluids;
    }

    @Override
    protected RECIPE fromNetwork(@NotNull RegistryFriendlyByteBuf buffer) {
        try {
            ItemStackIngredient[] inputItems = readItems(buffer);
            ItemStackIngredient[] outputItems = readItems(buffer);
            FluidStackIngredient[] inputFluids = readFluids(buffer);
            FluidStackIngredient[] outputFluids = readFluids(buffer);
            ParticleStack[] inputParticles = readParticles(buffer);
            ParticleStack[] outputParticles = readParticles(buffer);
            long maxEnergy = buffer.readLong();
            double crossSection = buffer.readDouble();

            return this.targetChamberFactory.make(getCodeId(), inputItems, outputItems, inputFluids, outputFluids, inputParticles, outputParticles, maxEnergy, crossSection);
        } catch (Exception e) {
            debugLog("Error reading recipe from packet for type: " + getCodeId() + ". Trace: " + e);
        }
        debugLog("Return empty recipe for type: " + getCodeId());

        //return invalid recipe
        return emptyRecipe();
    }

    @Override
    protected void toNetwork(@NotNull RegistryFriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            debugLog("Error writing recipe to packet." + e);
            throw e;
        }
    }
}
