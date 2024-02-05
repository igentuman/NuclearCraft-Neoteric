package igentuman.nc.datagen.recipes.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import igentuman.nc.container.elements.NCSlotItemHandler;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.InputIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.recipes.ingredient.creator.FluidStackIngredientCreator;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.MODID;

public class NcRecipeBuilder extends RecipeBuilder<NcRecipeBuilder> {

    private List<NcIngredient> inputItems = List.of();
    private List<NcIngredient> outputItems = List.of();
    private List<FluidStackIngredient> inputFluids = List.of();
    private List<FluidStack> outputFluids = List.of();
    private static NcRecipeBuilder instance;
    private double timeModifier = 1D;
    private double radiation = 1D;
    private double powerModifier = 1D;

    public double coolingRate = 0;
    public double heatRequired = 0;

    public String ID;
    private double rarityModifier = 1D;
    private double temperature = 0D;
    private List<String> outputItemsText = List.of();

    protected NcRecipeBuilder(String id) {
        super(ncSerializer(id));
        ID = id;
    }

    public static NcRecipeBuilder get(String id) {
        instance = new NcRecipeBuilder(id);
        return instance;
    }

    public NcRecipeBuilder items(List<NcIngredient> input, List<NcIngredient> output) {
        instance.inputItems = input;
        instance.outputItems = output;
        return instance;
    }

    public NcRecipeBuilder itemsString(List<NcIngredient> input, List<String> output) {
        instance.inputItems = input;
        instance.outputItemsText = output;
        return instance;
    }

    public NcRecipeBuilder fluids(List<FluidStackIngredient> input, List<FluidStack> output) {
        instance.inputFluids = input;
        instance.outputFluids = output;
        return instance;
    }


    public NcRecipeBuilder modifiers(double timeModifier, double radiation, double powerModifier, double rarity) {
        this.timeModifier = timeModifier;
        this.radiation = radiation;
        this.powerModifier = powerModifier;
        this.rarityModifier = rarity;
        return this;
    }

    public NcRecipeBuilder modifiers(double timeModifier, double radiation, double powerModifier) {
        this.timeModifier = timeModifier;
        this.radiation = radiation;
        this.powerModifier = powerModifier;
        return this;
    }

    @Override
    protected NcRecipeResult getResult(ResourceLocation id) {
        return new NcRecipeResult(id);
    }

    public ResourceLocation getRecipeId()
    {
        StringBuilder name = new StringBuilder();
        for (NcIngredient in: inputItems) {
            name.append(in.getName()).append("-");
        }
        for(FluidStackIngredient in: inputFluids) {
            name.append(in.getName()).append("-");
        }
        if(useInputForId) {
            for(FluidStack out: outputFluids) {
                name.append(out.getFluid().getRegistryName().getPath()).append("-");
            }
        }
        name.replace(name.length()-1, name.length(), "");

        return new ResourceLocation(MODID, ID+"/"+recipeIdReplacements(name.toString()));
    }

    protected String recipeIdReplacements(String val) {
        val = val.replace("nuclearcraft_", "");
        val = val.replace("depleted_fuel", "d_f");
        return val;
    }

    public void build(Consumer<FinishedRecipe> consumer) {
        build(consumer, getRecipeId());
    }

    public NcRecipeBuilder temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public NcRecipeBuilder coolingRate(double coolingRate) {
        this.coolingRate = coolingRate;
        return this;
    }

    public NcRecipeBuilder heatRequired(double heatRequired) {
        this.heatRequired = heatRequired;
        return this;
    }

    private boolean useInputForId = false;

    public NcRecipeBuilder useInputForId(boolean b) {
        useInputForId = b;
        return this;
    }

    public class NcRecipeResult extends RecipeResult {

        protected NcRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            JsonArray inputJson = new JsonArray();

            if(!inputItems.isEmpty()) {
                for(Ingredient in: inputItems) {
                    inputJson.add(serializeIngredient(in));
                }
                json.add("input", inputJson);
            }

            JsonArray outJson = new JsonArray();

            if(!outputItems.isEmpty()) {
                for (Ingredient out: outputItems) {
                    outJson.add(serializeIngredient(out));
                }
                json.add("output", outJson);
            }

            if(!outputItemsText.isEmpty()) {
                outJson = new JsonArray();
                for(String out: outputItemsText) {
                    JsonObject item = new JsonObject();
                    item.addProperty("item", out);
                    outJson.add(item);
                }
                json.add("output", outJson);
            }

            inputJson = new JsonArray();
            for(FluidStackIngredient in: inputFluids) {
                inputJson.add(in.serialize());
            }
            if(!inputFluids.isEmpty()) {
                json.add("inputFluids", inputJson);
            }

            outJson = new JsonArray();
            for (FluidStack out: outputFluids) {
                outJson.add(serializeFluidStack(out));
            }
            if(!outputFluids.isEmpty()) {
                json.add("outputFluids", outJson);
            }
            if(heatRequired > 0) {
                json.addProperty("heatRequired", heatRequired);
            }
            if(coolingRate > 0) {
                json.addProperty("coolingRate", coolingRate);
            }
            if(timeModifier > 0) {
                json.addProperty("timeModifier", timeModifier);
            }
            if(radiation > 0) {
                json.addProperty("radiation", radiation);
            }
            if(powerModifier > 0) {
                json.addProperty("powerModifier", powerModifier);
            }
            if(rarityModifier != 1D && rarityModifier != 0) {
                json.addProperty("rarityModifier", rarityModifier);
            }
            if(temperature != 0D) {
                json.addProperty("temperature", temperature);
            }
        }
    }
}