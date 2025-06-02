package igentuman.nc.datagen.recipes.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import javax.json.Json;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;

public class TConstructRecipeBuilder extends RecipeBuilder<TConstructRecipeBuilder> {

    private List<NcIngredient> inputItems = List.of();
    private List<NcIngredient> outputItems = List.of();
    private List<FluidStackIngredient> inputFluids = List.of();
    private List<FluidStackIngredient> outputFluids = List.of();
    private static TConstructRecipeBuilder instance;

    public String ID;
    private int temperature = 0;
    private List<String> outputItemsText = List.of();
    private int coolingTime = 0;
    private boolean cast = false;
    private int time = 0;

    protected static ResourceLocation ncSerializer(String name) {
        return ResourceLocation.tryBuild("tconstruct", name);
    }

    protected TConstructRecipeBuilder(String id) {
        super(ncSerializer(id));
        ID = id;
    }

    public static TConstructRecipeBuilder get(String id) {
        instance = new TConstructRecipeBuilder(id);
        return instance;
    }

    public TConstructRecipeBuilder items(List<NcIngredient> input, List<NcIngredient> output) {
        instance.inputItems = input;
        instance.outputItems = output;
        return instance;
    }

    public TConstructRecipeBuilder itemsString(List<NcIngredient> input, List<String> output) {
        instance.inputItems = input;
        instance.outputItemsText = output;
        return instance;
    }


    public TConstructRecipeBuilder fluids(List<FluidStackIngredient> input, List<FluidStackIngredient> output) {
        instance.inputFluids = input;
        instance.outputFluids = output;
        return instance;
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
            for(FluidStackIngredient out: outputFluids) {
                name.append(out.getName()).append("-");
            }
        }
        name.replace(name.length()-1, name.length(), "");

        return ResourceLocation.tryBuild("tconstruct",ID+"/"+recipeIdReplacements(name.toString()));
    }

    protected String recipeIdReplacements(String val) {
        val = val.replace("nuclearcraft_", "");
        val = val.replace("depleted_fuel", "d_f");
        return val;
    }

    public void build(Consumer<FinishedRecipe> consumer) {
        build(consumer, getRecipeId());
    }

    public TConstructRecipeBuilder temperature(int temperature) {
        this.temperature = temperature;
        return this;
    }

    private boolean useInputForId = false;

    public TConstructRecipeBuilder useInputForId(boolean b) {
        useInputForId = b;
        return this;
    }

    public TConstructRecipeBuilder coolingTime(int coolingTime) {
        this.coolingTime = coolingTime;
        return this;
    }

    public TConstructRecipeBuilder cast() {
        this.cast = true;
        return this;
    }

    public TConstructRecipeBuilder time(int time) {
        this.time = time;
        return this;
    }

    public class NcRecipeResult extends RecipeResult {

        protected NcRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            JsonArray inputJson = new JsonArray();
            JsonObject value = new JsonObject();
            value.addProperty("type", "forge:mod_loaded");
            value.addProperty("modid", "tconstruct");
            JsonArray conditions = new JsonArray();
            conditions.add(value);
            json.add("conditions", conditions);

            if(cast) {
                JsonObject castTag = new JsonObject();
                castTag.addProperty("tag", "tconstruct:casts/multi_use/ingot");
                json.add("cast", castTag);
            }
            if(!inputItems.isEmpty()) {
                for(Ingredient in: inputItems) {
                    json.add("ingredient", serializeIngredient(in));
                    break;
                }
            }


            if(!outputItems.isEmpty()) {
                for (Ingredient out: outputItems) {
                    json.add("result", serializeIngredient(out));
                    break;
                }
            }

            inputJson = new JsonArray();
            for(FluidStackIngredient in: inputFluids) {
                if(!cast) {
                    inputJson.add(in.serialize());
                } else {
                    json.add("fluid", in.serialize());
                    break;
                }
            }
            if(!inputFluids.isEmpty()) {
                if(!cast) {
                    json.add("inputs", inputJson);
                }
            }

            if(!outputFluids.isEmpty()) {
                for (FluidStackIngredient out: outputFluids) {
                    json.add("result", out.serialize());
                    break;
                }
            }

            if(coolingTime != 0) {
                json.addProperty("cooling_time", coolingTime);
            }
            if(temperature != 0) {
                json.addProperty("temperature", temperature);
            }
            if(time != 0) {
                json.addProperty("time", time);
            }
        }
    }
}