package igentuman.nc.datagen.recipes.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
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

    public String ID;

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

    public NcRecipeBuilder fluids(List<FluidStackIngredient> input, List<FluidStack> output) {
        instance.inputFluids = input;
        instance.outputFluids = output;
        return instance;
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
        for(NcIngredient out: outputItems) {
            name.append(out.getName()).append("-");
        }
        for(FluidStackIngredient in: inputFluids) {
            name.append(in.getRepresentations().get(0).getFluid().getFluidType().toString().split(":")[1]).append("-");
        }
        for(FluidStack out: outputFluids) {
            name.append(out.getFluid().getFluidType().toString().split(":")[1]).append("-");
        }
        name.replace(name.length()-1, name.length(), "");
        return new ResourceLocation(MODID, ID+"/"+name);
    }

    public void build(Consumer<FinishedRecipe> consumer) {
        build(consumer, getRecipeId());
    }

    public class NcRecipeResult extends RecipeResult {

        protected NcRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            JsonArray inputJson = new JsonArray();
            for(Ingredient in: inputItems) {
                inputJson.add(serializeIngredient(in));
            }
            if(!inputItems.isEmpty()) {
                json.add("input", inputJson);
            }

            JsonArray outJson = new JsonArray();
            for (Ingredient out: outputItems) {
                outJson.add(serializeIngredient(out));
            }
            if(!outputItems.isEmpty()) {
                json.add("output", outJson);
            }

            inputJson = new JsonArray();
            for(FluidStackIngredient in: inputFluids) {
                inputJson.add(in.serialize());
            }
            if(!inputFluids.isEmpty()) {
                json.add("inputFluids", outJson);
            }

            outJson = new JsonArray();
            for (FluidStack out: outputFluids) {
                outJson.add(serializeFluidStack(out));
            }
            if(!outputFluids.isEmpty()) {
                json.add("outputFluids", outJson);
            }

            json.addProperty("timeModifier", timeModifier);
            json.addProperty("radiation", radiation);
            json.addProperty("powerModifier", powerModifier);
        }
    }
}