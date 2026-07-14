package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.ae2.JEI2PatternEncoderTransfer;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.multiblock.MultiblockRegistry;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.recipe.fission.FissionRecipes;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.screen.UniversalProcessorScreen;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.MultiblockStructure;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/** JEI plugin registering processor, multiblock, and fission categories plus AE2 recipe transfer. */
@JeiPlugin
public class ModJeiPlugin implements IModPlugin {

    private final Map<String, RecipeType<UniversalProcessorRecipe>> recipeTypes = new HashMap<>();

    @Override
    public ResourceLocation getPluginUid() {
        return NuclearCraft.rl("jei_plugin");
    }

    private RecipeType<UniversalProcessorRecipe> getOrCreateRecipeType(ModEntry entry) {
        return recipeTypes.computeIfAbsent(entry.name(), name ->
                RecipeType.create(NuclearCraft.MODID, name, UniversalProcessorRecipe.class));
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
        registration.addRecipeCategories(new FissionFuelRecipeCategory(guiHelper));
        registration.addRecipeCategories(new BoilingRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!entry.hasRecipes() || !entry.hasItem() || !entry.isEnabled()) continue;
            RecipeType<UniversalProcessorRecipe> jeiType = getOrCreateRecipeType(entry);
            registration.addRecipeCatalyst(new ItemStack(entry.item().get()), jeiType);
        }
        ModEntry controller = ModEntries.get("fission_reactor_controller");
        if (controller != null && controller.hasItem()) {
            ItemStack stack = new ItemStack(controller.item().get());
            registration.addRecipeCatalyst(stack, FissionFuelRecipeCategory.TYPE);
            registration.addRecipeCatalyst(stack, BoilingRecipeCategory.TYPE);
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

        registration.addRecipes(FissionFuelRecipeCategory.TYPE,
                recipeManager.getAllRecipesFor(FissionRecipes.FUEL_TYPE.get())
                        .stream().map(RecipeHolder::value).toList());
        registration.addRecipes(BoilingRecipeCategory.TYPE,
                recipeManager.getAllRecipesFor(FissionRecipes.BOILING_TYPE.get())
                        .stream().map(RecipeHolder::value).toList());
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        JeiHelper.setRuntime(jeiRuntime);
        JeiHelper.setRecipeTypes(recipeTypes);
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(UniversalProcessorScreen.class, new IGuiContainerHandler<UniversalProcessorScreen>() {
            @Override
            public @NotNull Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull UniversalProcessorScreen containerScreen, double mouseX, double mouseY) {
                String name = containerScreen.getRecipeTypeName();
                if (name == null || name.contains("creative")) {
                    return List.of();
                }
                RecipeType<UniversalProcessorRecipe> type = recipeTypes.getOrDefault(name, null);
                if (type == null) return List.of();
                return List.of(IGuiClickableArea.createBasic(112, 74, 18, 18, type));
            }
        });
    }
}
