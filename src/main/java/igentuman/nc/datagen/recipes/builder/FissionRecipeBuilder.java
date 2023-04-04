package igentuman.nc.datagen.recipes.builder;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FissionRecipeBuilder extends NcRecipeBuilder<FissionRecipeBuilder> {

    private final ItemStack input;
    private final ItemStack output;

    protected FissionRecipeBuilder(ItemStack input, ItemStack output) {
        super(ncSerializer("fission_reactor"));
        this.input = input;
        this.output = output;
    }

    public static FissionRecipeBuilder create(ItemStack mainInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output item is empty.");
        }
        return new FissionRecipeBuilder(mainInput,  output);
    }

    public static FissionRecipeBuilder create(Item inputItem, Item outputItem) {
        ItemStack input = new ItemStack(inputItem);
        ItemStack output = new ItemStack(outputItem);
        return new FissionRecipeBuilder(input,  output);
    }

    @Override
    protected FissionRecipeResult getResult(ResourceLocation id) {
        return new FissionRecipeResult(id);
    }

    public void build(Consumer<FinishedRecipe> consumer) {
        build(consumer, output.getItem());
    }

    public class FissionRecipeResult extends RecipeResult {

        protected FissionRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            json.add("input", serializeItemStack(input));
            json.add("output", serializeItemStack(output));
        }
    }
}