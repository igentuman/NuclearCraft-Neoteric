package igentuman.nc.datagen.recipes;

import igentuman.nc.datagen.recipes.builder.FissionRecipeBuilder;
import igentuman.nc.setup.registration.Fuel;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;

public class CustomRecipes extends NCRecipes {


    public CustomRecipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    public static void generate(Consumer<FinishedRecipe> consumer) {
        for(List<String> name: Fuel.NC_FUEL.keySet()) {
            List<String> depleted = new ArrayList<>(name);
            depleted.set(0, "depleted");
            FissionRecipeBuilder.create(
                    Fuel.NC_FUEL.get(name).get(),
                    Fuel.NC_DEPLETED_FUEL.get(depleted).get()
            ).build(consumer, rl(String.join("_", name).replaceAll("/|-","")+"_fission"));
        }
    }

}
