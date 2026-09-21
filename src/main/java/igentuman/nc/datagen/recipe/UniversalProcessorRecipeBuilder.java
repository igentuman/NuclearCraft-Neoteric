package igentuman.nc.datagen.recipe;

import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.ItemOutput;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.fluidTag;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.materialTag;

/** Fluent builder that assembles and serializes universal processor recipes with item/fluid I/O. */
public class UniversalProcessorRecipeBuilder implements RecipeBuilder {

    private final String processorName;
    private final List<SizedIngredient> itemInputs = new ArrayList<>();
    private final List<SizedFluidIngredient> fluidInputs = new ArrayList<>();
    private final List<ItemOutput> itemOutputs = new ArrayList<>();
    private final List<FluidOutput> fluidOutputs = new ArrayList<>();
    private int processTime = 200;
    private int energyPerTick = 20;

    private UniversalProcessorRecipeBuilder(String processorName) {
        this.processorName = processorName;
    }

    public static UniversalProcessorRecipeBuilder processor(String name) {
        return new UniversalProcessorRecipeBuilder(name);
    }

    public static UniversalProcessorRecipeBuilder controller(String name) {
        return new UniversalProcessorRecipeBuilder(name);
    }

    // --- Item inputs ---

    public UniversalProcessorRecipeBuilder itemInput(ItemLike item) {
        return itemInput(item, 1);
    }

    public UniversalProcessorRecipeBuilder itemInput(ItemLike item, int count) {
        TagKey<Item> tag = materialTag(item);
        itemInputs.add(new SizedIngredient(tag == null ? Ingredient.of(item) : Ingredient.of(tag), count));
        return this;
    }

    public UniversalProcessorRecipeBuilder itemInput(TagKey<Item> tag) {
        return itemInput(tag, 1);
    }

    public UniversalProcessorRecipeBuilder itemInput(TagKey<Item> tag, int count) {
        itemInputs.add(new SizedIngredient(Ingredient.of(tag), count));
        return this;
    }

    // --- Fluid inputs ---

    public UniversalProcessorRecipeBuilder fluidInput(Fluid fluid, int amount) {
        fluidInputs.add(SizedFluidIngredient.of(fluid, amount));
        return this;
    }

    public UniversalProcessorRecipeBuilder fluidInput(FluidStack fluidStack) {
        fluidInputs.add(SizedFluidIngredient.of(fluidStack.getFluid(), fluidStack.getAmount()));
        return this;
    }

    public UniversalProcessorRecipeBuilder fluidInput(TagKey<Fluid> tag, int amount) {
        fluidInputs.add(SizedFluidIngredient.of(tag, amount));
        return this;
    }

    public UniversalProcessorRecipeBuilder fluidInput(String fluid, int amount) {
        TagKey<Fluid> tag = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", fluid));
        fluidInputs.add(SizedFluidIngredient.of(tag, amount));
        return this;
    }

    // --- Item outputs ---

    public UniversalProcessorRecipeBuilder itemOutput(ItemLike item) {
        return itemOutput(item, 1);
    }

    public UniversalProcessorRecipeBuilder itemOutput(ItemLike item, int count) {
        TagKey<Item> tag = materialTag(item);
        itemOutputs.add(tag == null ? ItemOutput.of(item, count) : ItemOutput.of(tag, count));
        return this;
    }

    public UniversalProcessorRecipeBuilder itemOutput(TagKey<Item> tag, int count) {
        itemOutputs.add(ItemOutput.of(tag, count));
        return this;
    }

    // --- Fluid outputs ---

    public UniversalProcessorRecipeBuilder fluidOutput(Fluid fluid, int amount) {
        TagKey<Fluid> tag = fluidTag(fluid);
        fluidOutputs.add(tag == null ? FluidOutput.of(fluid, amount) : FluidOutput.of(tag, amount));
        return this;
    }

    public UniversalProcessorRecipeBuilder fluidOutput(FluidStack fluidStack) {
        return fluidOutput(fluidStack.getFluid(), fluidStack.getAmount());
    }

    public UniversalProcessorRecipeBuilder fluidOutput(TagKey<Fluid> tag, int amount) {
        fluidOutputs.add(FluidOutput.of(tag, amount));
        return this;
    }

    // --- Parameters ---

    public UniversalProcessorRecipeBuilder processTime(int ticks) {
        this.processTime = ticks;
        return this;
    }

    public UniversalProcessorRecipeBuilder energyPerTick(int energy) {
        this.energyPerTick = energy;
        return this;
    }

    // --- RecipeBuilder overrides ---

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        // Processor recipes don't use advancement unlocking
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String groupName) {
        return this;
    }

    @Override
    public Item getResult() {
        if (itemOutputs.isEmpty()) return Items.AIR;
        ItemOutput first = itemOutputs.getFirst();
        return first.isTag() ? Items.AIR : first.item();
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        UniversalProcessorRecipe recipe = new UniversalProcessorRecipe(
                processorName,
                List.copyOf(itemInputs),
                List.copyOf(fluidInputs),
                List.copyOf(itemOutputs),
                List.copyOf(fluidOutputs),
                processTime,
                energyPerTick
        );
        // Item tag outputs get a load-time tag-empty guard so the recipe drops if the tag is empty.
        // No fluid-tag condition exists in NeoForge 21.1.228; fluid tags fall back to the runtime
        // isComplete() gate.
        List<ICondition> conditions = new ArrayList<>();
        for (ItemOutput output : itemOutputs) {
            if (output.isTag()) {
                conditions.add(new NotCondition(new TagEmptyCondition(output.tag())));
            }
        }
        recipeOutput.accept(id, recipe, null, conditions.toArray(new ICondition[0]));
    }

    /**
     * Convenience method: saves the recipe with an auto-generated ID based on processor name and a suffix.
     */
    public void save(RecipeOutput recipeOutput, String recipeName) {
        save(recipeOutput, ResourceLocation.fromNamespaceAndPath(MODID, processorName + "/" + recipeName));
    }
}
