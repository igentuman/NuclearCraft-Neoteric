package igentuman.nc.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import igentuman.nc.Main;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.multiblock.MultiblockRegistry;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.recipe.fission.BoilingRecipe;
import igentuman.nc.recipe.fission.FissionFuelRecipe;
import igentuman.nc.recipe.fission.FissionRecipes;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.MultiblockStructure;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EmiEntrypoint
public class ModEmiPlugin implements EmiPlugin {

    private final Map<String, EmiRecipeCategory> categories = new HashMap<>();

    private EmiRecipeCategory getOrCreateCategory(ModEntry entry) {
        return categories.computeIfAbsent(entry.name(), name -> {
            ResourceLocation id = Main.rl(name);
            EmiStack icon = entry.hasItem()
                ? EmiStack.of(new ItemStack(entry.item().get()))
                : EmiStack.EMPTY;
            return new EmiRecipeCategory(id, icon);
        });
    }

    @Override
    public void initialize(EmiInitRegistry registry) {
        EmiPlugin.super.initialize(registry);
    }

    @Override
    public void register(EmiRegistry registry) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!entry.hasRecipes() || !entry.isEnabled()) continue;
            EmiRecipeCategory category = getOrCreateCategory(entry);
            registry.addCategory(category);
            if (entry.hasItem()) {
                registry.addWorkstation(category, EmiStack.of(new ItemStack(entry.item().get())));
            }
        }

        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!entry.hasRecipes() || !entry.isEnabled()) continue;
            EmiRecipeCategory category = getOrCreateCategory(entry);

            @SuppressWarnings("unchecked")
            RecipeType<UniversalProcessorRecipe> mcType =
                (RecipeType<UniversalProcessorRecipe>) entry.recipeType().get();

            List<UniversalProcessorRecipe> recipes = recipeManager
                .getAllRecipesFor(mcType)
                .stream()
                .map(RecipeHolder::value)
                .filter(UniversalProcessorRecipe::isComplete)
                .toList();

            for (int i = 0; i < recipes.size(); i++) {
                ResourceLocation recipeId = Main.rl("/" + entry.name() + "/" + i);
                registry.addRecipe(new ProcessorEmiRecipe(category, recipeId, recipes.get(i), entry));
            }
        }

        EmiRecipeCategory mbCategory = new EmiRecipeCategory(
                Main.rl("multiblock_examples"),
                EmiStack.of(new ItemStack(net.minecraft.world.level.block.Blocks.IRON_BLOCK)));
        boolean mbCategoryAdded = false;
        for (MultiblockEntry mb : MultiblockRegistry.ENTRIES.values()) {
            if (!mb.isBuildable()) continue;
            MultiblockStructure structure = mb.getExampleStructure();
            if (structure == null) continue;
            if (!mbCategoryAdded) {
                registry.addCategory(mbCategory);
                mbCategoryAdded = true;
            }
            registry.addRecipe(new MultiblockExampleEmiRecipe(mbCategory, mb, structure));
        }

        registerFissionRecipes(registry, recipeManager);
    }

    private void registerFissionRecipes(EmiRegistry registry, RecipeManager recipeManager) {
        ModEntry controller = ModEntries.get("fission_reactor_controller");
        if (controller == null || !controller.hasItem()) return;
        EmiStack workstation = EmiStack.of(new ItemStack(controller.item().get()));

        EmiRecipeCategory fuelCategory = new EmiRecipeCategory(Main.rl("fission_fuel"), workstation);
        registry.addCategory(fuelCategory);
        registry.addWorkstation(fuelCategory, workstation);
        List<FissionFuelRecipe> fuelRecipes = recipeManager.getAllRecipesFor(FissionRecipes.FUEL_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        for (int i = 0; i < fuelRecipes.size(); i++) {
            registry.addRecipe(new FissionFuelEmiRecipe(fuelCategory, Main.rl("/fission_fuel/" + i), fuelRecipes.get(i)));
        }

        EmiRecipeCategory boilingCategory = new EmiRecipeCategory(Main.rl("fission_boiling"), workstation);
        registry.addCategory(boilingCategory);
        registry.addWorkstation(boilingCategory, workstation);
        List<BoilingRecipe> boilingRecipes = recipeManager.getAllRecipesFor(FissionRecipes.BOILING_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        for (int i = 0; i < boilingRecipes.size(); i++) {
            registry.addRecipe(new BoilingEmiRecipe(boilingCategory, Main.rl("/fission_boiling/" + i), boilingRecipes.get(i)));
        }
    }
}
