package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.compat.PlacementRuleRecipe;
import igentuman.nc.compat.ProcessorCategoryLayout;
import igentuman.nc.compat.ae2.JEI2PatternEncoderTransfer;
import igentuman.nc.compat.refinedstorage.RSJeiPatternTransfer;
import igentuman.nc.compat.jei.particle.ParticleIngredientHelper;
import igentuman.nc.compat.jei.particle.ParticleIngredientRenderer;
import igentuman.nc.compat.jei.particle.ParticleType;
import igentuman.nc.handler.crafter.ProcessorRecipes;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.multiblock.MultiblockRegistry;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.recipe.fission.FissionRecipes;
import igentuman.nc.recipe.fusion.FusionRecipes;
import igentuman.nc.recipe.kugelblitz.KugelblitzRecipes;
import igentuman.nc.recipe.particle.ParticleRecipes;
import igentuman.nc.recipe.turbine.TurbineRecipes;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.registration.RecipeSchemaKind;
import igentuman.nc.screen.UniversalProcessorScreen;
import igentuman.nc.setup.entries.Crafter;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.Registers;
import igentuman.nc.util.MultiblockStructure;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import igentuman.nc.recipe.RecipeSources;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/** Registers NuclearCraft recipe categories and transfer handlers for Engineer's menus and storage mods. */
@JeiPlugin
public class ModJeiPlugin implements IModPlugin {

    private final Map<String, IRecipeHolderType<UniversalProcessorRecipe>> recipeTypes = new HashMap<>();

    @Override
    public Identifier getPluginUid() {
        return NuclearCraft.rl("jei_plugin");
    }

    private boolean isProcessorEntry(ModEntry entry) {
        return entry.hasRecipes() && entry.isEnabled()
                && entry.recipeFamily() != null
                && entry.recipeFamily().schemaKind() == RecipeSchemaKind.UNIVERSAL_PROCESSOR;
    }

    private IRecipeHolderType<UniversalProcessorRecipe> getOrCreateRecipeType(ModEntry entry) {
        return recipeTypes.computeIfAbsent(entry.name(), name ->
                IRecipeHolderType.create(NuclearCraft.rl(ProcessorCategoryLayout.categoryName(name))));
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        List<ParticleStack> allParticles = Registers.PARTICLE_DEFINITION_REGISTRY.keySet().stream()
                .map(id -> new ParticleStack(id, 1, 0, 0))
                .toList();
        registration.register(ParticleType.PARTICLE, allParticles, new ParticleIngredientHelper(), new ParticleIngredientRenderer(), ParticleStack.CODEC);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();

        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!isProcessorEntry(entry)) continue;
            IRecipeHolderType<UniversalProcessorRecipe> jeiType = getOrCreateRecipeType(entry);
            registration.addRecipeCategories(new ProcessorRecipeCategory(guiHelper, entry, jeiType));
        }

        registration.addRecipeCategories(new MultiblockExampleCategory(guiHelper));
        registration.addRecipeCategories(new FissionFuelRecipeCategory(guiHelper));
        registration.addRecipeCategories(new BoilingRecipeCategory(guiHelper));
        registration.addRecipeCategories(new HeatExchangerRecipeCategory(guiHelper));
        registration.addRecipeCategories(new NuclearBlastRecipeCategory(guiHelper));
        registration.addRecipeCategories(new FusionRecipeCategory(guiHelper));
        registration.addRecipeCategories(new FusionCoolantRecipeCategory(guiHelper));
        registration.addRecipeCategories(new TurbineRecipeCategory(guiHelper));
        registration.addRecipeCategories(new KugelblitzRecipeCategory(guiHelper));
        registration.addRecipeCategories(new OreVeinRecipeCategory(guiHelper));
        registration.addRecipeCategories(new TargetChamberRecipeCategory(guiHelper));
        registration.addRecipeCategories(new DecayChamberRecipeCategory(guiHelper));
        registration.addRecipeCategories(new CollisionChamberRecipeCategory(guiHelper));
        registration.addRecipeCategories(new FuelInfoCategory(guiHelper));
        registration.addRecipeCategories(new IsotopeInfoCategory(guiHelper));
        registration.addRecipeCategories(new ParticleInfoCategory(guiHelper));
        registration.addRecipeCategories(new ParticleSourceCategory(guiHelper));
        registration.addRecipeCategories(new PlacementRuleCategory(guiHelper, PlacementRuleCategory.HEAT_SINK, "empty_heat_sink"));
        registration.addRecipeCategories(new PlacementRuleCategory(guiHelper, PlacementRuleCategory.COOLER, "empty_cooler"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!isProcessorEntry(entry) || !entry.hasItem()) continue;
            IRecipeHolderType<UniversalProcessorRecipe> jeiType = getOrCreateRecipeType(entry);
            registration.addCraftingStation(jeiType, entry.item().get());
        }
        ModEntry controller = ModEntries.get("fission_reactor_controller");
        if (controller != null && controller.hasItem()) {
            registration.addCraftingStation(FissionFuelRecipeCategory.TYPE, controller.item().get());
            registration.addCraftingStation(BoilingRecipeCategory.TYPE, controller.item().get());
        }
        ModEntry hxController = ModEntries.get("heat_exchanger_controller");
        if (hxController != null && hxController.hasItem()) {
            registration.addCraftingStation(HeatExchangerRecipeCategory.TYPE, hxController.item().get());
        }
        addSpecialRecipeCatalyst(registration, "fusion_reactor_core",
                FusionRecipeCategory.TYPE, FusionCoolantRecipeCategory.TYPE);
        addSpecialRecipeCatalyst(registration, "turbine_controller", TurbineRecipeCategory.TYPE);
        addSpecialRecipeCatalyst(registration, "chamber_terminal", KugelblitzRecipeCategory.TYPE);
        addSpecialRecipeCatalyst(registration, "analyzer", OreVeinRecipeCategory.TYPE);
        addSpecialRecipeCatalyst(registration, "linear_accelerator_controller", ParticleSourceCategory.TYPE);
        addSpecialRecipeCatalyst(registration, "target_chamber_controller", TargetChamberRecipeCategory.TYPE);
        addSpecialRecipeCatalyst(registration, "decay_chamber_controller", DecayChamberRecipeCategory.TYPE);
        addSpecialRecipeCatalyst(registration, "collision_chamber_controller", CollisionChamberRecipeCategory.TYPE);
        registration.addCraftingStation(RecipeTypes.CRAFTING, Crafter.ENGINEERS_CRAFTING_TABLE_ITEM.get());
        addSpecialRecipeCatalyst(registration, "fission_reactor_controller", PlacementRuleCategory.HEAT_SINK);
        addSpecialRecipeCatalyst(registration, "ring_accelerator_controller", PlacementRuleCategory.COOLER);
    }

    private void addSpecialRecipeCatalyst(IRecipeCatalystRegistration registration, String entryName,
                                          IRecipeType<?>... types) {
        ModEntry entry = ModEntries.get(entryName);
        if (entry == null || !entry.hasItem()) return;
        for (IRecipeType<?> type : types) {
            registration.addCraftingStation(type, entry.item().get());
        }
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new EngineersCrafterRecipeTransferHandler(registration.getTransferHelper()), RecipeTypes.CRAFTING);
        registration.addRecipeTransferHandler(new EngineersEncoderRecipeTransferHandler(), RecipeTypes.CRAFTING);
        boolean ae2 = ModList.get().isLoaded("ae2");
        boolean refinedStorage = ModList.get().isLoaded("refinedstorage");
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!isProcessorEntry(entry)) continue;
            IRecipeHolderType<UniversalProcessorRecipe> jeiType = getOrCreateRecipeType(entry);
            if (ProcessorRecipes.isProcessorEntry(entry)) {
                registration.addRecipeTransferHandler(
                        new EngineersEncoderProcessorTransferHandler(jeiType, registration.getTransferHelper()), jeiType);
            }
            if (ae2) {
                registration.addRecipeTransferHandler(new JEI2PatternEncoderTransfer(jeiType, registration.getTransferHelper()), jeiType);
            }
            if (refinedStorage) {
                registration.addRecipeTransferHandler(new RSJeiPatternTransfer(jeiType, registration.getTransferHelper()), jeiType);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeMap recipeMap = RecipeSources.recipes(Minecraft.getInstance().level);

        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!isProcessorEntry(entry)) continue;

            IRecipeHolderType<UniversalProcessorRecipe> jeiType = getOrCreateRecipeType(entry);
            net.minecraft.world.item.crafting.RecipeType<?> mcType = entry.recipeType().get();

            List<RecipeHolder<UniversalProcessorRecipe>> recipes = recipeMap
                    .byType((net.minecraft.world.item.crafting.RecipeType<UniversalProcessorRecipe>) mcType)
                    .stream()
                    .filter(holder -> holder.value().isComplete())
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
                List.copyOf(recipeMap.byType(FissionRecipes.FUEL_TYPE.get())));
        registration.addRecipes(BoilingRecipeCategory.TYPE,
                List.copyOf(recipeMap.byType(FissionRecipes.BOILING_TYPE.get())));
        registration.addRecipes(HeatExchangerRecipeCategory.TYPE,
                List.copyOf(recipeMap.byType(igentuman.nc.recipe.heat_exchanger.HeatExchangerRecipes.HX_TYPE.get())));
        registration.addRecipes(NuclearBlastRecipeCategory.TYPE,
                List.copyOf(recipeMap.byType(igentuman.nc.recipe.bomb.NcBlastRecipes.TYPE.get())));
        registration.addRecipes(FusionRecipeCategory.TYPE,
                List.copyOf(recipeMap.byType(FusionRecipes.FUSION_TYPE.get())));
        registration.addRecipes(FusionCoolantRecipeCategory.TYPE,
                List.copyOf(recipeMap.byType(FusionRecipes.COOLANT_TYPE.get())));
        registration.addRecipes(TurbineRecipeCategory.TYPE,
                List.copyOf(recipeMap.byType(TurbineRecipes.TURBINE_TYPE.get())));
        registration.addRecipes(KugelblitzRecipeCategory.TYPE,
                recipeMap.byType(KugelblitzRecipes.KUGELBLITZ_TYPE.get())
                        .stream().filter(holder -> holder.value().isComplete()).toList());
        registration.addRecipes(TargetChamberRecipeCategory.TYPE,
                List.copyOf(recipeMap.byType(ParticleRecipes.TARGET_CHAMBER_TYPE.get())));
        registration.addRecipes(DecayChamberRecipeCategory.TYPE,
                List.copyOf(recipeMap.byType(ParticleRecipes.DECAY_CHAMBER_TYPE.get())));
        registration.addRecipes(CollisionChamberRecipeCategory.TYPE,
                List.copyOf(recipeMap.byType(ParticleRecipes.COLLISION_CHAMBER_TYPE.get())));
        ModEntry oreVeins = ModEntries.get("nc_ore_veins");
        if (oreVeins != null && oreVeins.hasRecipes()) {
            @SuppressWarnings("unchecked")
            net.minecraft.world.item.crafting.RecipeType<igentuman.nc.recipe.OreVeinRecipe> oreVeinType =
                    (net.minecraft.world.item.crafting.RecipeType<igentuman.nc.recipe.OreVeinRecipe>)
                            oreVeins.recipeType().get();
            registration.addRecipes(OreVeinRecipeCategory.TYPE,
                    List.copyOf(recipeMap.byType(oreVeinType)));
        }

        registration.addRecipes(FuelInfoCategory.TYPE, fuelInfoRecipes());
        registration.addRecipes(IsotopeInfoCategory.TYPE, isotopeInfoRecipes());
        registration.addRecipes(ParticleInfoCategory.TYPE, particleInfoRecipes());
        registration.addRecipes(ParticleSourceCategory.TYPE, igentuman.nc.compat.ParticleSourceRecipe.all());
        registration.addRecipes(PlacementRuleCategory.HEAT_SINK, PlacementRuleRecipe.heatSinks());
        registration.addRecipes(PlacementRuleCategory.COOLER, PlacementRuleRecipe.coolers());
    }

    private List<ParticleInfoRecipe> particleInfoRecipes() {
        List<ParticleInfoRecipe> out = new ArrayList<>();
        igentuman.nc.particle.ParticleCatalog.fromRegistry().definitions()
                .forEach((id, definition) -> out.add(new ParticleInfoRecipe(id, definition)));
        return out;
    }

    private List<FuelInfoRecipe> fuelInfoRecipes() {
        List<FuelInfoRecipe> out = new ArrayList<>();
        for (FissionFuelEntry entry : ModEntries.FISSION_FUEL.values()) {
            FuelInfoRecipe r = new FuelInfoRecipe(entry.group, entry.name);
            if (!r.getVariants().isEmpty()) out.add(r);
        }
        return out;
    }

    private List<IsotopeInfoRecipe> isotopeInfoRecipes() {
        List<IsotopeInfoRecipe> out = new ArrayList<>();
        for (String name : ModEntries.ISOTOPES.keySet()) {
            IsotopeInfoRecipe r = new IsotopeInfoRecipe(name);
            if (!r.getVariants().isEmpty()) out.add(r);
        }
        return out;
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        JeiHelper.setRuntime(jeiRuntime);
        JeiHelper.setRecipeTypes(recipeTypes);
        hideFuelAndIsotopeVariants(jeiRuntime);
    }

    @Override
    public void onRuntimeUnavailable() {
        JeiHelper.clearRuntime();
    }

    private static final String[] FUEL_VARIANT_SUFFIXES = {"_ox", "_ni", "_za", "_tr"};
    private static final String[] ISOTOPE_VARIANT_SUFFIXES = {"_ox", "_ni", "_za"};

    private void hideFuelAndIsotopeVariants(IJeiRuntime jeiRuntime) {
        List<ItemStack> itemsToHide = new ArrayList<>();
        List<FluidStack> fluidsToHide = new ArrayList<>();

        for (FissionFuelEntry entry : ModEntries.FISSION_FUEL.values()) {
            for (String suffix : FUEL_VARIANT_SUFFIXES) {
                if (entry.fuelItems().containsKey(suffix)) {
                    itemsToHide.add(new ItemStack(entry.fuelItems().get(suffix).get()));
                }
                if (entry.depletedItems().containsKey(suffix)) {
                    itemsToHide.add(new ItemStack(entry.depletedItems().get(suffix).get()));
                }
            }
            for (MaterialEntry mat : entry.fluids()) {
                if (mat == null || !mat.hasFluid()) continue;
                String matName = mat.name;
                boolean isVariant = false;
                for (String suf : FUEL_VARIANT_SUFFIXES) {
                    if (matName.endsWith(suf)) { isVariant = true; break; }
                }
                if (!isVariant) continue;
                fluidsToHide.add(new FluidStack(mat.materialFluid().source().get(), 1000));
                if (mat.bucket() != null) {
                    itemsToHide.add(new ItemStack(mat.bucket().get()));
                }
            }
        }
        for (IsotopeEntry entry : ModEntries.ISOTOPES.values()) {
            for (String suffix : ISOTOPE_VARIANT_SUFFIXES) {
                if (entry.variants().containsKey(suffix)) {
                    itemsToHide.add(new ItemStack(entry.variants().get(suffix).get()));
                }
            }
            for (MaterialEntry mat : entry.fluids()) {
                if (mat == null || !mat.hasFluid()) continue;
                String matName = mat.name;
                boolean isVariant = false;
                for (String suf : ISOTOPE_VARIANT_SUFFIXES) {
                    if (matName.endsWith(suf)) { isVariant = true; break; }
                }
                if (!isVariant) continue;
                fluidsToHide.add(new FluidStack(mat.materialFluid().source().get(), 1000));
                if (mat.bucket() != null) {
                    itemsToHide.add(new ItemStack(mat.bucket().get()));
                }
            }
        }

        if (!itemsToHide.isEmpty()) {
            jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, itemsToHide);
        }
        if (!fluidsToHide.isEmpty()) {
            jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(NeoForgeTypes.FLUID_STACK, fluidsToHide);
        }
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
                IRecipeHolderType<UniversalProcessorRecipe> type = recipeTypes.get(name);
                if (type == null) return List.of();
                return List.of(IGuiClickableArea.createBasic(112, 74, 18, 18, type));
            }
        });
    }
}
