package igentuman.nc.compat.jei;

import igentuman.nc.Main;
import igentuman.nc.compat.ae2.JEI2PatternEncoderTransfer;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.multiblock.MultiblockRegistry;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.MultiblockStructure;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin {

    private final Map<String, RecipeType<UniversalProcessorRecipe>> recipeTypes = new HashMap<>();

    @Override
    public ResourceLocation getPluginUid() {
        return Main.rl("jei_plugin");
    }

    private RecipeType<UniversalProcessorRecipe> getOrCreateRecipeType(ModEntry entry) {
        return recipeTypes.computeIfAbsent(entry.name(), name ->
                RecipeType.create(Main.MODID, name, UniversalProcessorRecipe.class));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();

        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!entry.hasRecipes() || !entry.isEnabled()) continue;
            RecipeType<UniversalProcessorRecipe> jeiType = getOrCreateRecipeType(entry);
            registration.addRecipeCategories(new ProcessorRecipeCategory(guiHelper, entry, jeiType));
        }

        registration.addRecipeCategories(new MultiblockExampleCategory(guiHelper));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!entry.hasRecipes() || !entry.hasItem() || !entry.isEnabled()) continue;
            RecipeType<UniversalProcessorRecipe> jeiType = getOrCreateRecipeType(entry);
            registration.addRecipeCatalyst(new ItemStack(entry.item().get()), jeiType);
        }
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        if (!ModList.get().isLoaded("ae2")) return;
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!entry.hasRecipes() || !entry.isEnabled()) continue;
            RecipeType<UniversalProcessorRecipe> jeiType = getOrCreateRecipeType(entry);
            registration.addRecipeTransferHandler(new JEI2PatternEncoderTransfer(jeiType), jeiType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!entry.hasRecipes() || !entry.isEnabled()) continue;

            RecipeType<UniversalProcessorRecipe> jeiType = getOrCreateRecipeType(entry);
            net.minecraft.world.item.crafting.RecipeType<?> mcType = entry.recipeType().get();

            List<UniversalProcessorRecipe> recipes = recipeManager
                    .getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType<UniversalProcessorRecipe>) mcType)
                    .stream()
                    .map(RecipeHolder::value)
                    .filter(UniversalProcessorRecipe::isComplete)
                    .toList();

            registration.addRecipes(jeiType, recipes);
        }

        List<MultiblockStructureRecipe> mbRecipes = new ArrayList<>();
        for (MultiblockEntry mb : MultiblockRegistry.ENTRIES.values()) {
            if (!mb.isBuildable()) continue;
            MultiblockStructure structure = mb.getExampleStructure();
            if (structure == null) continue;
            mbRecipes.add(new MultiblockStructureRecipe(mb, structure));
        }
        if (!mbRecipes.isEmpty()) {
            registration.addRecipes(MultiblockExampleCategory.TYPE, mbRecipes);
        }
    }
}
