package igentuman.nc.compat.jei;

import igentuman.nc.block.accelerator.entity.LinearAcceleratorControllerBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.block.fusion.entity.FusionCoreBE;
import igentuman.nc.block.kugelblitz.entity.ChamberTerminalBE;
import igentuman.nc.block.turbine.entity.TurbineControllerBE;
import igentuman.nc.client.NcClient;
import igentuman.nc.client.gui.fission.FissionControllerScreen;
import igentuman.nc.client.gui.processor.NCProcessorScreen;
import igentuman.nc.compat.jei.ingredient.ParticleStackHelper;
import igentuman.nc.compat.jei.ingredient.ParticleStackListFactory;
import igentuman.nc.compat.jei.ingredient.ParticleStackRenderer;
import igentuman.nc.compat.jei.ingredient.ParticleType;
import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleSources;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.Particles;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.MekChemicalConversionRecipe;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.OreVeinRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

import static igentuman.nc.NuclearCraft.*;
import static igentuman.nc.compat.GlobalVars.*;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.ION_SOURCES;
import static igentuman.nc.util.ModUtil.isMekanismLoaded;

@JeiPlugin
public  class JEIPlugin implements IModPlugin {
    public static HashMap<String, RecipeType<? extends NcRecipe>> recipeTypes;

    public static final RecipeType<TargetChamberControllerBE.Recipe> TARGET_CHAMBER = new RecipeType<>(rl("target_chamber"), TargetChamberControllerBE.Recipe.class);
    public static final RecipeType<ChamberTerminalBE.Recipe> KUGELBLITZ = new RecipeType<>(rl("kugelblitz_chamber"), ChamberTerminalBE.Recipe.class);
    public static final RecipeType<FissionControllerBE.Recipe> FISSION = new RecipeType<>(rl(FissionControllerBE.NAME), FissionControllerBE.Recipe.class);
    public static final RecipeType<FusionCoreBE.Recipe> FUSION = new RecipeType<>(rl("fusion_core"), FusionCoreBE.Recipe.class);
    public static final RecipeType<FusionCoreBE.FusionCoolantRecipe> FUSION_COOLANT = new RecipeType<>(rl("fusion_coolant"), FusionCoreBE.FusionCoolantRecipe.class);
    public static final RecipeType<LinearAcceleratorControllerBE.CoolantRecipe> ACCELERATOR_COOLANT = new RecipeType<>(rl("accelerator_coolant"), LinearAcceleratorControllerBE.CoolantRecipe.class);
    public static final RecipeType<FissionControllerBE.FissionBoilingRecipe> FISSION_BOILING = new RecipeType<>(rl("fission_boiling"), FissionControllerBE.FissionBoilingRecipe.class);
    public static final RecipeType<TurbineControllerBE.Recipe> TURBINE_CONTROLLER = new RecipeType<>(rl(TurbineControllerBE.NAME), TurbineControllerBE.Recipe.class);
    public static final RecipeType<MekChemicalConversionRecipe> CHEMICAL_TO_FLUID = new RecipeType<>(rl("mek_chemical_to_fluid"), MekChemicalConversionRecipe.class);;
    public static final RecipeType<OreVeinRecipe> ORE_VEINS = new RecipeType<>(rl("nc_ore_veins"), OreVeinRecipe.class);

    private static HashMap<String, RecipeType<? extends NcRecipe>> getRecipeTypes() {
        if (recipeTypes == null) {
            recipeTypes = new HashMap<>();
            for (String name : RECIPE_CLASSES.keySet()) {
                if (Processors.all().containsKey(name) && !Processors.all().get(name).isRegistered()) {
                    continue;
                }
                recipeTypes.put(name, new RecipeType<>(rl(name), RECIPE_CLASSES.get(name)));
            }
        }
        return recipeTypes;
    }


    public ResourceLocation getPluginUid() {
        return rl("jei_plugin");
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        List<ParticleStack> particleStacks = ParticleStackListFactory.create();
        ParticleStackHelper particleStackHelper = new ParticleStackHelper();
        ParticleStackRenderer particleStackRenderer = new ParticleStackRenderer();
        registration.register(ParticleType.Particle, particleStacks, particleStackHelper, particleStackRenderer);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        IRecipeManager recipeManager = jeiRuntime.getRecipeManager();

        for(String name: Processors.all().keySet()) {
            if(Processors.registered().containsKey(name)) {
                continue;
            }
            ResourceLocation categoryToHide = rl(name);
            if(recipeManager.getRecipeType(categoryToHide).isPresent()) {
                recipeManager.hideRecipeCategory(recipeManager.getRecipeType(categoryToHide).get());
            }
        }
    }

    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        for (String name : getRecipeTypes().keySet()) {
            if (!Processors.all().containsKey(name)) continue;
            registration.addRecipeCategories(new ProcessorCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), getRecipeType(name)));
        }
        registration.addRecipeCategories(new OreVeinCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), ORE_VEINS));
        registration.addRecipeCategories(new FusionCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), FUSION));
        registration.addRecipeCategories(new FusionCoolantCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), FUSION_COOLANT));
        registration.addRecipeCategories(new AcceleratorCoolantCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), ACCELERATOR_COOLANT));
        registration.addRecipeCategories(new FissionBoilingCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), FISSION_BOILING));
        registration.addRecipeCategories(new TurbineControllerCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), TURBINE_CONTROLLER));
        registration.addRecipeCategories(new FissionCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), FISSION));
        registration.addRecipeCategories(new KugelblitzCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), KUGELBLITZ));
        registration.addRecipeCategories(new TargetChamberCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), TARGET_CHAMBER));
        if(isMekanismLoaded()) {
            registration.addRecipeCategories(new MekChemicalConversionCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), CHEMICAL_TO_FLUID));
        }

        registration.addRecipeCategories(
                new MultiblockStructureCategory(registration.getJeiHelpers().getGuiHelper())
        );
        registration.addRecipeCategories(
                new ParticleInfoCategory(registration.getJeiHelpers().getGuiHelper())
        );
        registration.addRecipeCategories(
                new ParticleSourceCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    public <TYPE> RecipeType<TYPE> getRecipeType(String name) {
        return (RecipeType<TYPE>) recipeTypes.get(name);
    }

    public <TYPE> RecipeType<TYPE> getRecipeType(RecipeType<? extends AbstractRecipe> in) {
        return (RecipeType<TYPE>) in;
    }


    public void registerRecipes(IRecipeRegistration registration) {
        try {
            for (String name : getRecipeTypes().keySet()) {
                if(List.of(
                        "fusion_core", "fusion_coolant",
                        "fission_reactor_controller", "fission_boiling", "target_chamber",
                        "nc_ore_veins", "turbine_controller", "kugelblitz_chamber"
                ).contains(name)) {
                    continue;
                }
                registration.addRecipes(
                        getRecipeType(name),
                        NcRecipeType.ALL_RECIPES.get(name).getRecipes(NcClient.tryGetClientWorld()));
            }
            registration.addRecipes(
                    getRecipeType(TARGET_CHAMBER),
                    NcRecipeType.ALL_RECIPES.get("target_chamber").getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(KUGELBLITZ),
                    NcRecipeType.ALL_RECIPES.get("kugelblitz_chamber").getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(FUSION),
                    NcRecipeType.ALL_RECIPES.get("fusion_core").getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(FUSION_COOLANT),
                    NcRecipeType.ALL_RECIPES.get("fusion_coolant").getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(ACCELERATOR_COOLANT),
                    NcRecipeType.ALL_RECIPES.get("accelerator_coolant").getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(FISSION),
                    NcRecipeType.ALL_RECIPES.get(FissionControllerBE.NAME).getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(FISSION_BOILING),
                    NcRecipeType.ALL_RECIPES.get("fission_boiling").getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(TURBINE_CONTROLLER),
                    NcRecipeType.ALL_RECIPES.get(TurbineControllerBE.NAME).getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(ORE_VEINS),
                    NcRecipeType.ALL_RECIPES.get("nc_ore_veins").getRecipes(NcClient.tryGetClientWorld()));
            if(isMekanismLoaded()) {
                registration.addRecipes(getRecipeType(CHEMICAL_TO_FLUID), MekChemicalConversionRecipe.getRecipes());
            }

            List<MultiblockStructureRecipe> multiblockRecipes = loadMultiblockStructures();
            registration.addRecipes(MultiblockStructureCategory.TYPE, multiblockRecipes);
            registration.addRecipes(ParticleInfoCategory.TYPE, particleRecipes());
            registration.addRecipes(ParticleSourceCategory.TYPE, particleSourceRecipes());
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Error registering recipes for JEI: " + ex.getMessage());
        }
    }

    private List<ParticleSourceRecipe> particleSourceRecipes() {
        List<ParticleSourceRecipe> recipes = new ArrayList<>();
        for (String item: ParticleSources.sources.keySet()) {
            if(ParticleSources.sources.get(item).getParticle() == null) {
                continue;
            }
            recipes.add(new ParticleSourceRecipe(rl(item), new ItemStack(ION_SOURCES.get(item).get()), null, ParticleSources.sources.get(item).getParticle()));
        }

        for (String fluid: ParticleSources.fluidSources.keySet()) {
            recipes.add(new ParticleSourceRecipe(rl(fluid), null, IngredientCreatorAccess.fluid().from(fluid, 1).getRepresentations().get(0), ParticleSources.fluidSources.get(fluid).getParticle()));
        }
        return recipes;
    }

    private List<ParticleRecipe> particleRecipes() {
        List<ParticleRecipe> recipes = new ArrayList<>();
        for (Particle particle : Particles.particles.values()) {
            recipes.add(new ParticleRecipe(rl(particle.getName()), particle));
        }
        return recipes;
    }

    private List<MultiblockStructureRecipe> loadMultiblockStructures() {
        List<MultiblockStructureRecipe> recipes = new ArrayList<>();
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        List<String> structures = List.of(
            "fission_reactor.nbt",
            "fusion_reactor.nbt",
            "kugelblitz_chamber.nbt",
            "leacher.nbt",
            "turbine.nbt"
        );

        for (String file : structures) {
            Optional<Resource> structure = resourceManager.getResource(rl("structures/" + file));
            CompoundTag nbt = null;
            try {
                nbt = NbtIo.readCompressed(structure.get().open());
            } catch (IOException e) {
                continue;
            }
            recipes.add(new MultiblockStructureRecipe(rl(file), nbt, file));
        }
        return recipes;
    }

    private <T extends AbstractContainerScreen<?>> void addRecipeClickArea(IGuiHandlerRegistration registration, Class<? extends T> containerScreenClass, int xPos, int yPos, int width, int height, RecipeType<?>... recipeTypes) {
        if(recipeTypes == null) return;
        registration.addGuiContainerHandler(containerScreenClass, new IGuiContainerHandler<T>() {
            @Override
            public @NotNull Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull T containerScreen, double mouseX, double mouseY) {
                NCProcessorScreen<?> screen = (NCProcessorScreen<?>) containerScreen;
                String name = screen.getRecipeTypeName();
                IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(xPos, yPos, width, height, getRecipeTypes().get(name));
                return List.of(clickableArea);
            }
        });
    }

    public  void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        for (String name : getRecipeTypes().keySet()) {
            if (!Processors.registered().containsKey(name)) continue;
            addRecipeClickArea(registration, NCProcessorScreen.class, 67, 74, 18, 18, getRecipeType(name));
        }
        registration.addRecipeClickArea(FissionControllerScreen.class,72, 38, 36, 26, FISSION);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        for(String  name: getRecipeTypes().keySet()) {
            if(!CATALYSTS.containsKey(name)) continue;
            for(ItemStack stack: CATALYSTS.get(name)) {
                registry.addRecipeCatalyst(stack, getRecipeType(name));
            }
        }

        if(CATALYSTS.containsKey(FissionControllerBE.NAME)) {
            registry.addRecipeCatalyst(CATALYSTS.get(FissionControllerBE.NAME).get(0), FISSION);
        }

        registry.addRecipeCatalyst(ACCELERATOR_BLOCKS.get("linear_accelerator_controller").get(), ACCELERATOR_COOLANT);
        registry.addRecipeCatalyst(ACCELERATOR_BLOCKS.get("thoroidal_accelerator_controller").get(), ACCELERATOR_COOLANT);
        if(CATALYSTS.containsKey("nc_ore_veins")) {
            registry.addRecipeCatalyst(CATALYSTS.get("nc_ore_veins").get(0), ORE_VEINS);
        }
       // registry.addRecipeCatalyst(new ItemStack(NC_FOOD.get("smore").get()));
    }
}
