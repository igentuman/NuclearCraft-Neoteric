package igentuman.nc.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import igentuman.nc.NuclearCraft;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.multiblock.MultiblockRegistry;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.recipe.fission.BoilingRecipe;
import igentuman.nc.recipe.fission.FissionFuelRecipe;
import igentuman.nc.recipe.fission.FissionRecipes;
import igentuman.nc.recipe.bomb.NcBlastRecipes;
import igentuman.nc.recipe.bomb.NuclearBlastRecipe;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.entries.Crafter;
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

/** EMI plugin registering processor, multiblock, and fission recipe categories and their recipes. */
@EmiEntrypoint
public class ModEmiPlugin implements EmiPlugin {

    private final Map<String, EmiRecipeCategory> categories = new HashMap<>();

    private EmiRecipeCategory getOrCreateCategory(ModEntry entry) {
        return categories.computeIfAbsent(entry.name(), name -> {
            ResourceLocation id = NuclearCraft.rl(name);
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

        registry.addRecipeHandler(Crafter.ENGINEERS_CRAFTING_TABLE_MENU.get(), new EngineersCrafterEmiRecipeHandler());
        registry.addRecipeHandler(Crafter.ENGINEERS_ENCODER_MENU.get(), new EngineersEncoderEmiRecipeHandler());
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(new ItemStack(Crafter.ENGINEERS_CRAFTING_TABLE_ITEM.get())));

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
                ResourceLocation recipeId = NuclearCraft.rl("/" + entry.name() + "/" + i);
                registry.addRecipe(new ProcessorEmiRecipe(category, recipeId, recipes.get(i), entry));
            }
        }

        EmiRecipeCategory mbCategory = new EmiRecipeCategory(
                NuclearCraft.rl("multiblock_examples"),
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
        registerHeatExchangerRecipes(registry, recipeManager);
        registerNuclearBlastRecipes(registry, recipeManager);
    }

    private void registerHeatExchangerRecipes(EmiRegistry registry, RecipeManager recipeManager) {
        ModEntry controller = ModEntries.get("heat_exchanger_controller");
        if (controller == null || !controller.hasItem()) return;
        EmiStack workstation = EmiStack.of(new ItemStack(controller.item().get()));

        EmiRecipeCategory category = new EmiRecipeCategory(NuclearCraft.rl("heat_exchanger"), workstation);
        registry.addCategory(category);
        registry.addWorkstation(category, workstation);
        List<igentuman.nc.recipe.heat_exchanger.HeatExchangerRecipe> recipes =
                recipeManager.getAllRecipesFor(igentuman.nc.recipe.heat_exchanger.HeatExchangerRecipes.HX_TYPE.get())
                        .stream().map(RecipeHolder::value).toList();
        for (int i = 0; i < recipes.size(); i++) {
            registry.addRecipe(new HeatExchangerEmiRecipe(category, NuclearCraft.rl("/heat_exchanger/" + i), recipes.get(i)));
        }
    }

    private void registerNuclearBlastRecipes(EmiRegistry registry, RecipeManager recipeManager) {
        ModEntry bomb = ModEntries.get("pu_239_bomb");
        if (bomb == null || bomb.block() == null) return;
        EmiStack icon = EmiStack.of(new ItemStack(bomb.block().get()));
        EmiRecipeCategory category = new EmiRecipeCategory(NuclearCraft.rl("nuclear_blast"), icon);
        List<NuclearBlastRecipe> recipes = recipeManager.getAllRecipesFor(NcBlastRecipes.TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        if (recipes.isEmpty()) return;
        registry.addCategory(category);
        for (int i = 0; i < recipes.size(); i++) {
            registry.addRecipe(new NuclearBlastEmiRecipe(category, NuclearCraft.rl("/nuclear_blast/" + i), recipes.get(i)));
        }
    }

    private void registerFissionRecipes(EmiRegistry registry, RecipeManager recipeManager) {
        ModEntry controller = ModEntries.get("fission_reactor_controller");
        if (controller == null || !controller.hasItem()) return;
        EmiStack workstation = EmiStack.of(new ItemStack(controller.item().get()));

        EmiRecipeCategory fuelCategory = new EmiRecipeCategory(NuclearCraft.rl("fission_fuel"), workstation);
        registry.addCategory(fuelCategory);
        registry.addWorkstation(fuelCategory, workstation);
        List<FissionFuelRecipe> fuelRecipes = recipeManager.getAllRecipesFor(FissionRecipes.FUEL_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        for (int i = 0; i < fuelRecipes.size(); i++) {
            registry.addRecipe(new FissionFuelEmiRecipe(fuelCategory, NuclearCraft.rl("/fission_fuel/" + i), fuelRecipes.get(i)));
        }

        EmiRecipeCategory boilingCategory = new EmiRecipeCategory(NuclearCraft.rl("fission_boiling"), workstation);
        registry.addCategory(boilingCategory);
        registry.addWorkstation(boilingCategory, workstation);
        List<BoilingRecipe> boilingRecipes = recipeManager.getAllRecipesFor(FissionRecipes.BOILING_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        for (int i = 0; i < boilingRecipes.size(); i++) {
            registry.addRecipe(new BoilingEmiRecipe(boilingCategory, NuclearCraft.rl("/fission_boiling/" + i), boilingRecipes.get(i)));
        }
    }
}
