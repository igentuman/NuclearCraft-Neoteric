package igentuman.nc.datagen.recipes.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
        return ResourceLocation.fromNamespaceAndPath("tconstruct", name);
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

        return ResourceLocation.fromNamespaceAndPath("tconstruct",ID+"/"+recipeIdReplacements(name.toString()));
    }

    protected String recipeIdReplacements(String val) {
        val = val.replace("nuclearcraft_", "");
        val = val.replace("depleted_fuel", "d_f");
        return val;
    }

    public void build(RecipeOutput output) {
        build(output, getRecipeId());
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

        private JsonObject fluidTagNotEmpty(String tag) {
            JsonObject neoforgeNot = new JsonObject();
            neoforgeNot.addProperty("type", "neoforge:not");
            JsonObject fluidTagNotEmpty = new JsonObject();
            fluidTagNotEmpty.addProperty("type", "neoforge:fluid_tag_empty");
            fluidTagNotEmpty.addProperty("tag", tag);
            neoforgeNot.add("value", fluidTagNotEmpty);
            return neoforgeNot;
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            JsonArray inputJson = new JsonArray();
            JsonObject modLoaded = new JsonObject();
            modLoaded.addProperty("type", "neoforge:mod_loaded");
            modLoaded.addProperty("modid", "tconstruct");
            JsonArray conditions = new JsonArray();
            conditions.add(modLoaded);

            json.add("neoforge:conditions", conditions);

            if(cast) {
                JsonObject castTag = new JsonObject();
                castTag.addProperty("tag", "tconstruct:casts/multi_use/ingot");
                json.add("cast", castTag);
            }
            if(!inputItems.isEmpty()) {
                for(NcIngredient in: inputItems) {
                    json.add("ingredient", serializeIngredient(in.asIngredient()));
                    break;
                }
            }


            if(!outputItems.isEmpty()) {
                for (NcIngredient out: outputItems) {
                    json.add("result", serializeIngredient(out.asIngredient()));
                    break;
                }
            }

            inputJson = new JsonArray();
            for(FluidStackIngredient in: inputFluids) {
                try {
                    String tag = ((JsonObject) in.serialize()).get("tag").getAsString();
                    if (tag != null) {
                        conditions.add(fluidTagNotEmpty(tag));
                    }
                } catch (Exception ignore) {}
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
                    try {
                        String tag = ((JsonObject) out.serialize()).get("tag").getAsString();
                        if (tag != null) {
                            conditions.add(fluidTagNotEmpty(tag));
                        }
                    } catch (Exception ignore) {}
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
