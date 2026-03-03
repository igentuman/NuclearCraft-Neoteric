package igentuman.nc.recipes.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import igentuman.api.platform.NCRecipeSerializerFactory;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.type.EmptyRecipe;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.util.JsonConstants;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;


import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.recipes.NcRecipeSerializers.SERIALIZERS;
import static igentuman.nc.recipes.type.NcRecipe.getBarrier;

public class NcRecipeSerializer<RECIPE extends NcRecipe> implements RecipeSerializer<RECIPE> {

    protected final IFactory<RECIPE> factory;
    private String codeId;

    public NcRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    /**
     * Get the recipe type code ID for this serializer, derived from its registry name.
     */
    protected String getCodeId() {
        if (codeId == null) {
            codeId = BuiltInRegistries.RECIPE_SERIALIZER.getKey(this).getPath();
        }
        return codeId;
    }

    // --- Codec / StreamCodec (NeoForge 1.21.1 API) ---

    @Override
    public @NotNull MapCodec<RECIPE> codec() {
        return NCRecipeSerializerFactory.createCodec(this::fromJson);
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, RECIPE> streamCodec() {
        return NCRecipeSerializerFactory.createStreamCodec(this::fromNetwork, this::toNetwork);
    }

    // --- JSON parsing helpers ---

    protected FluidStackIngredient[] inputFluidsFromJson(JsonObject json) {
        FluidStackIngredient[] inputFluids = new FluidStackIngredient[0];
        if(json.has("inputFluids")) {
            if (GsonHelper.isArrayNode(json, "inputFluids")) {
                JsonElement input = GsonHelper.getAsJsonArray(json, "inputFluids");
                inputFluids = new FluidStackIngredient[input.getAsJsonArray().size()];
                int i = 0;
                for (JsonElement in : input.getAsJsonArray()) {
                    try {
                        inputFluids[i] = IngredientCreatorAccess.fluid().deserialize(in);
                    } catch (Exception ex) {
                        debugLog("Unable to parse input fluid for recipe type: " + getCodeId());
                        inputFluids[i] = IngredientCreatorAccess.fluid().from(FluidStack.EMPTY);
                    }
                    i++;
                }
            } else {
                JsonElement inputJson = GsonHelper.getAsJsonObject(json, "inputFluids");
                try {
                inputFluids = new FluidStackIngredient[]{IngredientCreatorAccess.fluid().deserialize(inputJson)};
                } catch (Exception ex) {
                    debugLog("Unable to parse input fluid for recipe type: " + getCodeId());
                    inputFluids[0] = IngredientCreatorAccess.fluid().from(FluidStack.EMPTY);
                }
            }
        }

        return inputFluids;
    }

    protected FluidStackIngredient[] outputFluidsFromJson(JsonObject json) {
        FluidStackIngredient[] outputFluids = new FluidStackIngredient[0];

            if(json.has("outputFluids")) {
                if (GsonHelper.isArrayNode(json, "outputFluids")) {
                    JsonElement output = GsonHelper.getAsJsonArray(json, "outputFluids");
                    outputFluids = new FluidStackIngredient[output.getAsJsonArray().size()];
                    int i = 0;
                    for (JsonElement out : output.getAsJsonArray()) {
                        try {
                            outputFluids[i] = IngredientCreatorAccess.fluid().deserialize(out.getAsJsonObject());
                        } catch (Exception ex) {
                            debugLog("Unable to parse output fluid for recipe type: " + getCodeId());
                            outputFluids[i] = IngredientCreatorAccess.fluid().from(FluidStack.EMPTY);
                        }
                        i++;
                    }
                } else {
                    JsonElement output = GsonHelper.getAsJsonObject(json, "outputFluids");
                    try {
                        outputFluids = new FluidStackIngredient[]{IngredientCreatorAccess.fluid().deserialize(output.getAsJsonObject())};
                    } catch (Exception ex) {
                        debugLog("Unable to parse output fluid for recipe type: " + getCodeId());
                        outputFluids[0] = IngredientCreatorAccess.fluid().from(FluidStack.EMPTY);
                    }
                }
            }

            return outputFluids;
    }

    protected ParticleStack[] particleStacksFromJson(JsonObject json, String fieldName) {
        ParticleStack[] inputParticles = new ParticleStack[0];

        if (json.has(fieldName)) {
            if (GsonHelper.isArrayNode(json, fieldName)) {
                JsonElement input = GsonHelper.getAsJsonArray(json, fieldName);
                inputParticles = new ParticleStack[input.getAsJsonArray().size()];
                int i = 0;
                for (JsonElement in : input.getAsJsonArray()) {
                    inputParticles[i] = ParticleStack.fromJSON(in);
                    i++;
                }
            } else {
                JsonElement inputJson = GsonHelper.getAsJsonObject(json, fieldName);
                inputParticles = new ParticleStack[1];
                inputParticles[0] = ParticleStack.fromJSON(inputJson);
            }
        }
        return inputParticles;
    }

    protected ItemStackIngredient[] inputItemsFromJson(JsonObject json) {
        ItemStackIngredient[] inputItems = new ItemStackIngredient[0];

        if (json.has(JsonConstants.INPUT)) {
            if (GsonHelper.isArrayNode(json, JsonConstants.INPUT)) {
                JsonElement input = GsonHelper.getAsJsonArray(json, JsonConstants.INPUT);
                inputItems = new ItemStackIngredient[input.getAsJsonArray().size()];
                int i = 0;
                for (JsonElement in : input.getAsJsonArray()) {
                    try {
                        inputItems[i] = IngredientCreatorAccess.item().deserialize(in);
                    } catch (Exception ex) {
                        debugLog("Unable to parse input for recipe type: " + getCodeId());
                        inputItems[i] = getBarrier();
                    }
                    i++;
                }
            } else {
                JsonElement inputJson = GsonHelper.getAsJsonObject(json, JsonConstants.INPUT);
                try {
                    inputItems = new ItemStackIngredient[]{IngredientCreatorAccess.item().deserialize(inputJson)};
                } catch (Exception ex) {
                    debugLog("Unable to parse input for recipe type: " + getCodeId());
                    inputItems[0] = getBarrier();
                }
            }
        }

        return inputItems;
    }

    protected ItemStackIngredient[] outputItemsFromJson(JsonObject json) {
        ItemStackIngredient[] outputItems = new ItemStackIngredient[0];
        if(json.has(JsonConstants.OUTPUT)) {
            if (GsonHelper.isArrayNode(json, JsonConstants.OUTPUT)) {
                JsonElement output = GsonHelper.getAsJsonArray(json, JsonConstants.OUTPUT);
                outputItems = new ItemStackIngredient[output.getAsJsonArray().size()];

                int i = 0;
                for (JsonElement out : output.getAsJsonArray()) {
                    try {
                        outputItems[i] = IngredientCreatorAccess.item().deserialize(out);
                    } catch (JsonSyntaxException ex) {
                        debugLog("Error parsing output itemstack for recipe type: " + getCodeId());
                        outputItems[i] = getBarrier();
                    }
                    i++;
                }
            } else {
                JsonElement output = GsonHelper.getAsJsonObject(json, JsonConstants.OUTPUT);
                try {
                    outputItems = new ItemStackIngredient[]{IngredientCreatorAccess.item().deserialize(output.getAsJsonObject())};
                } catch (Exception ex) {
                    debugLog("Unable to parse output for recipe type: " + getCodeId());
                    outputItems[0] = getBarrier();
                }
            }
        }

        return outputItems;
    }

    // --- JSON deserialization ---

    protected @NotNull RECIPE fromJson(@NotNull JsonObject json) {
        String type = GsonHelper.getAsString(json, "type");
        if(Processors.all().containsKey(type) && !Processors.all().get(type).config().isRegistered()) {
            return emptyRecipe();
        }

        ItemStackIngredient[] inputItems = inputItemsFromJson(json);
        ItemStackIngredient[] outputItems = outputItemsFromJson(json);
        FluidStackIngredient[] inputFluids = inputFluidsFromJson(json);
        FluidStackIngredient[] outputFluids = outputFluidsFromJson(json);

        double timeModifier = 1D;
        double powerModifier = 1D;
        double radiation = 1D;
        double rarityModifier = 1D;
        double temperature = 1D;
        try {
            timeModifier = GsonHelper.getAsDouble(json, "timeModifier", 1.0);
            powerModifier = GsonHelper.getAsDouble(json, "powerModifier", 1.0);
            radiation = GsonHelper.getAsDouble(json, "radiation", 1.0);
            rarityModifier = GsonHelper.getAsDouble(json, "rarityModifier", 1.0);
            temperature = GsonHelper.getAsDouble(json, "temperature", 1.0);
            if (temperature > 1) {
                rarityModifier = temperature;
            }
        } catch (Exception ex) {
            debugLog("Unable to parse params for recipe type: " + getCodeId());
        }
        return this.factory.create(getCodeId(), inputItems, outputItems, inputFluids, outputFluids, timeModifier, powerModifier, radiation, rarityModifier);
    }

    @SuppressWarnings("unchecked")
    RECIPE emptyRecipe() {
        return (RECIPE) new EmptyRecipe(getCodeId());
    }

    // --- Network I/O ---

    public ItemStackIngredient[] readItems(@NotNull RegistryFriendlyByteBuf buffer) {
        int size = buffer.readInt();
        ItemStackIngredient[] items = new ItemStackIngredient[size];
        for(int i = 0; i < size; i++) {
            items[i] = IngredientCreatorAccess.item().read(buffer);
        }
        return items;
    }

    public FluidStackIngredient[] readFluids(@NotNull RegistryFriendlyByteBuf buffer) {
        int size = buffer.readInt();
        FluidStackIngredient[] fluids = new FluidStackIngredient[size];
        for(int i = 0; i < size; i++) {
            fluids[i] = IngredientCreatorAccess.fluid().read(buffer);
        }
        return fluids;
    }

    protected RECIPE fromNetwork(@NotNull RegistryFriendlyByteBuf buffer) {
        try {
            ItemStackIngredient[] inputItems = readItems(buffer);
            ItemStackIngredient[] outputItems = readItems(buffer);
            FluidStackIngredient[] inputFluids = readFluids(buffer);
            FluidStackIngredient[] outputFluids = readFluids(buffer);

            double timeModifier = buffer.readDouble();
            double powerModifier = buffer.readDouble();
            double radiation = buffer.readDouble();

            return this.factory.create(getCodeId(), inputItems, outputItems, inputFluids,  outputFluids, timeModifier, powerModifier, radiation, 1);
        } catch (Exception e) {
            debugLog("Error reading recipe from packet for type: " + getCodeId() + ". Trace: " + e);
        }
        debugLog("Return empty recipe for type: " + getCodeId());

        //return invalid recipe
        return emptyRecipe();
    }

    protected void toNetwork(@NotNull RegistryFriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            debugLog("Error writing recipe to packet." + e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends NcRecipe> {
        RECIPE create(String codeId,
                      ItemStackIngredient[] inputItems, ItemStackIngredient[] outputItems,
                      FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                      double timeMultiplier, double powerMultiplier, double radiationMultiplier, double extraModifier);
    }
}
