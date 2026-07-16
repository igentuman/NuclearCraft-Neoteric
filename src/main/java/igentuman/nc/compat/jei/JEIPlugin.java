package igentuman.nc.compat.jei;

import igentuman.nc.block.accelerator.entity.LinearAcceleratorControllerBE;
import igentuman.nc.block.collision_chamber.entity.CollisionChamberControllerBE;
import igentuman.nc.block.decay_chamber.entity.DecayChamberControllerBE;
import igentuman.nc.block.fission.entity.MSRControllerBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.block.fusion.entity.FusionCoreBE;
import igentuman.nc.block.kugelblitz.entity.ChamberTerminalBE;
import igentuman.nc.block.turbine.entity.TurbineControllerBE;
import igentuman.nc.block.heat_exchanger.entity.HeatExchangerControllerBE;
import igentuman.nc.client.NcClient;
import igentuman.nc.client.gui.fission.FissionControllerScreen;
import igentuman.nc.client.gui.fission.MSRControllerScreen;
import igentuman.nc.client.gui.processor.NCProcessorScreen;
import igentuman.nc.compat.jei.ProcessorRecipeTransferHandler;
import igentuman.nc.compat.jei.ingredient.ParticleStackHelper;
import igentuman.nc.compat.jei.ingredient.ParticleStackListFactory;
import igentuman.nc.compat.jei.ingredient.ParticleStackRenderer;
import igentuman.nc.compat.jei.ingredient.ParticleType;
import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleSources;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.Particles;
import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.handler.config.ClientConfig;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.MekChemicalConversionRecipe;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.NuclearBlastRecipe;
import igentuman.nc.recipes.type.OreVeinRecipe;
import igentuman.nc.multiblock.accelerator.AcceleratorRegistration;
import igentuman.nc.multiblock.accelerator.CoolerDef;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.multiblock.fission.HeatSinkDef;
import igentuman.nc.setup.registration.FissionFuel;
import igentuman.nc.setup.registration.NCFluids;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

import static igentuman.nc.NuclearCraft.*;
import static igentuman.nc.compat.GlobalVars.*;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.radiation.ItemRadiation.getItemByName;
import static igentuman.nc.setup.registration.NCItems.ION_SOURCES;
import static igentuman.nc.setup.registration.NCItems.UNKNOWN_INGREDIENT;
import static igentuman.nc.util.ModUtil.isMekanismLoaded;
import static igentuman.nc.util.TextUtils.__;
import static net.minecraft.world.item.Items.AIR;

@JeiPlugin
public  class JEIPlugin implements IModPlugin {
    public static HashMap<String, RecipeType<? extends NcRecipe>> recipeTypes;

    public static final RecipeType<TargetChamberControllerBE.Recipe> TARGET_CHAMBER = new RecipeType<>(rl("target_chamber"), TargetChamberControllerBE.Recipe.class);
    public static final RecipeType<CollisionChamberControllerBE.Recipe> COLLISION_CHAMBER = new RecipeType<>(rl("collision_chamber"), CollisionChamberControllerBE.Recipe.class);
    public static final RecipeType<DecayChamberControllerBE.Recipe> DECAY_CHAMBER = new RecipeType<>(rl("decay_chamber"), DecayChamberControllerBE.Recipe.class);
    public static final RecipeType<ChamberTerminalBE.Recipe> KUGELBLITZ = new RecipeType<>(rl("kugelblitz_chamber"), ChamberTerminalBE.Recipe.class);
    public static final RecipeType<FissionControllerBE.Recipe> FISSION = new RecipeType<>(rl(FissionControllerBE.NAME), FissionControllerBE.Recipe.class);
    public static final RecipeType<MSRControllerBE.Recipe> MSR = new RecipeType<>(rl(MSRControllerBE.NAME), MSRControllerBE.Recipe.class);
    public static final RecipeType<FusionCoreBE.Recipe> FUSION = new RecipeType<>(rl("fusion_core"), FusionCoreBE.Recipe.class);
    public static final RecipeType<FusionCoreBE.FusionCoolantRecipe> FUSION_COOLANT = new RecipeType<>(rl("fusion_coolant"), FusionCoreBE.FusionCoolantRecipe.class);
    public static final RecipeType<LinearAcceleratorControllerBE.CoolantRecipe> ACCELERATOR_COOLANT = new RecipeType<>(rl("accelerator_coolant"), LinearAcceleratorControllerBE.CoolantRecipe.class);
    public static final RecipeType<NuclearBlastRecipe> NUCLEAR_BLAST = new RecipeType<>(rl("nuclear_blast"), NuclearBlastRecipe.class);
    public static final RecipeType<FissionControllerBE.FissionBoilingRecipe> FISSION_BOILING = new RecipeType<>(rl("fission_boiling"), FissionControllerBE.FissionBoilingRecipe.class);
    public static final RecipeType<TurbineControllerBE.Recipe> TURBINE_CONTROLLER = new RecipeType<>(rl(TurbineControllerBE.NAME), TurbineControllerBE.Recipe.class);
    public static final RecipeType<HeatExchangerControllerBE.Recipe> HEAT_EXCHANGER = new RecipeType<>(rl(HeatExchangerControllerBE.NAME), HeatExchangerControllerBE.Recipe.class);
    public static final RecipeType<MekChemicalConversionRecipe> CHEMICAL_TO_FLUID = new RecipeType<>(rl("mek_chemical_to_fluid"), MekChemicalConversionRecipe.class);;
    public static final RecipeType<OreVeinRecipe> ORE_VEINS = new RecipeType<>(rl("nc_ore_veins"), OreVeinRecipe.class);
    public static final RecipeType<HeatSinkPlacementRecipe> HEAT_SINK_PLACEMENT = new RecipeType<>(rl("heat_sink_placement"), HeatSinkPlacementRecipe.class);
    public static final RecipeType<CoolerPlacementRecipe> COOLER_PLACEMENT = new RecipeType<>(rl("cooler_placement"), CoolerPlacementRecipe.class);

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
        
        if (ClientConfig.MISC_CONFIG.HIDE_PARTICLES.get()) {
            List<ParticleStack> particleStacks = ParticleStackListFactory.create();
            jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(ParticleType.Particle, particleStacks);
        }

        hideFuelAndIsotopeVariants(jeiRuntime);
    }

    private static final String[] FUEL_VARIANT_SUFFIXES = {"_ox", "_ni", "_za", "_tr"};
    private static final String[] ISOTOPE_VARIANT_SUFFIXES = {"_ox", "_ni", "_za"};

    private void hideFuelAndIsotopeVariants(IJeiRuntime jeiRuntime) {
        List<ItemStack> itemsToHide = new ArrayList<>();
        List<FluidStack> fluidsToHide = new ArrayList<>();

        for (Map.Entry<List<String>, net.minecraftforge.registries.RegistryObject<Item>> e : FissionFuel.NC_FUEL.entrySet()) {
            String variant = e.getKey().get(3);
            if (!variant.isEmpty()) itemsToHide.add(new ItemStack(e.getValue().get()));
        }
        for (Map.Entry<List<String>, net.minecraftforge.registries.RegistryObject<Item>> e : FissionFuel.NC_DEPLETED_FUEL.entrySet()) {
            String variant = e.getKey().get(3);
            if (!variant.isEmpty()) itemsToHide.add(new ItemStack(e.getValue().get()));
        }
        for (Map.Entry<String, net.minecraftforge.registries.RegistryObject<Item>> e : FissionFuel.NC_ISOTOPES.entrySet()) {
            String key = e.getKey();
            for (String suf : ISOTOPE_VARIANT_SUFFIXES) {
                if (key.endsWith(suf)) {
                    itemsToHide.add(new ItemStack(e.getValue().get()));
                    break;
                }
            }
        }

        for (Map.Entry<String, NCFluids.FluidEntry> e : NCFluids.NC_MATERIALS.entrySet()) {
            String key = e.getKey();
            boolean isFuelFluid = key.startsWith("fuel_") || key.startsWith("depleted_fuel_");
            boolean isIsotopeFluid = isKnownIsotopeFluid(key);
            if (!isFuelFluid && !isIsotopeFluid) continue;
            String suffixMatch = matchVariantSuffix(key, isFuelFluid ? FUEL_VARIANT_SUFFIXES : ISOTOPE_VARIANT_SUFFIXES);
            if (suffixMatch == null) continue;
            fluidsToHide.add(new FluidStack(e.getValue().getStill(), 1000));
            net.minecraft.world.item.BucketItem bucket = e.getValue().getBucket();
            if (bucket != null) itemsToHide.add(new ItemStack(bucket));
        }

        if (!itemsToHide.isEmpty()) {
            jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, itemsToHide);
        }
        if (!fluidsToHide.isEmpty()) {
            jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(ForgeTypes.FLUID_STACK, fluidsToHide);
        }
    }

    private static String matchVariantSuffix(String key, String[] suffixes) {
        for (String s : suffixes) {
            if (key.endsWith(s)) return s;
        }
        return null;
    }

    private static boolean isKnownIsotopeFluid(String key) {
        for (String iso : Materials.isotopes()) {
            if (key.equals(iso)) return true;
            for (String suf : ISOTOPE_VARIANT_SUFFIXES) {
                if (key.equals(iso + suf)) return true;
            }
        }
        return false;
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
        registration.addRecipeCategories(new HeatExchangerCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), HEAT_EXCHANGER));
        registration.addRecipeCategories(new FissionCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), FISSION));
        registration.addRecipeCategories(new MSRCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), MSR));
        registration.addRecipeCategories(new KugelblitzCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), KUGELBLITZ));
        registration.addRecipeCategories(new TargetChamberCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), TARGET_CHAMBER));
        registration.addRecipeCategories(new NuclearBlastCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), NUCLEAR_BLAST));
        registration.addRecipeCategories(new CollisionChamberCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), COLLISION_CHAMBER));
        registration.addRecipeCategories(new DecayChamberCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), DECAY_CHAMBER));
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
        registration.addRecipeCategories(
                new HeatSinkPlacementCategory(registration.getJeiHelpers().getGuiHelper())
        );
        registration.addRecipeCategories(
                new CoolerPlacementCategory(registration.getJeiHelpers().getGuiHelper())
        );
        registration.addRecipeCategories(
                new FuelInfoCategory(registration.getJeiHelpers().getGuiHelper())
        );
        registration.addRecipeCategories(
                new IsotopeInfoCategory(registration.getJeiHelpers().getGuiHelper())
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
                        "fission_reactor_controller", "msr_controller", "fission_boiling", "target_chamber",
                        "collision_chamber", "decay_chamber", "nuclear_blast",
                        "nc_ore_veins", "turbine_controller", "heat_exchanger_controller", "kugelblitz_chamber"
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
                    getRecipeType(COLLISION_CHAMBER),
                    NcRecipeType.ALL_RECIPES.get("collision_chamber").getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(DECAY_CHAMBER),
                    NcRecipeType.ALL_RECIPES.get("decay_chamber").getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(NUCLEAR_BLAST),
                    NcRecipeType.ALL_RECIPES.get("nuclear_blast").getRecipes(NcClient.tryGetClientWorld()));
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
                    getRecipeType(MSR),
                    NcRecipeType.ALL_RECIPES.get(MSRControllerBE.NAME).getRecipes(NcClient.tryGetClientWorld()));
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
                    getRecipeType(HEAT_EXCHANGER),
                    NcRecipeType.ALL_RECIPES.get(HeatExchangerControllerBE.NAME).getRecipes(NcClient.tryGetClientWorld()));
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
            registration.addRecipes(HeatSinkPlacementCategory.TYPE, heatSinkPlacementRecipes());
            registration.addRecipes(CoolerPlacementCategory.TYPE, coolerPlacementRecipes());
            registration.addRecipes(FuelInfoCategory.TYPE, fuelInfoRecipes());
            registration.addRecipes(IsotopeInfoCategory.TYPE, isotopeInfoRecipes());
            
            // Add ingredient info for chamber controller
            registration.addIngredientInfo(
                List.of(new ItemStack(KUGELBLITZ_BLOCKS.get("chamber_terminal").get()), new ItemStack(UNKNOWN_INGREDIENT.get())),
                VanillaTypes.ITEM_STACK,
                __("jei.info.nuclearcraft.kugelblitz.description"),
                Component.literal(""),
                __("jei.info.nuclearcraft.kugelblitz.problem"),
                Component.literal(""),
                __("jei.info.nuclearcraft.kugelblitz.input_output")
            );
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
            ResourceLocation resLoc = rl(item);
            if(item.contains(":")) {
                resLoc = ResourceLocation.tryParse(item);
            }
            Item sourceItem = AIR;
            if(ION_SOURCES.containsKey(item)) {
                sourceItem = ION_SOURCES.get(item).get();
            } else {
                sourceItem = getItemByName(item);
            }
            recipes.add(new ParticleSourceRecipe(resLoc, new ItemStack(sourceItem), null, ParticleSources.sources.get(item).getParticle()));
        }

        for (String fluid: ParticleSources.fluidSources.keySet()) {
            ResourceLocation resLoc = rl(fluid);
            if(fluid.contains(":")) {
                resLoc = ResourceLocation.tryParse(fluid);
            }
            recipes.add(new ParticleSourceRecipe(resLoc, null, IngredientCreatorAccess.fluid().from(fluid, 1).getRepresentations().get(0), ParticleSources.fluidSources.get(fluid).getParticle()));
        }
        return recipes;
    }

    private List<FuelInfoRecipe> fuelInfoRecipes() {
        List<FuelInfoRecipe> out = new ArrayList<>();
        for (String group : FuelManager.all().keySet()) {
            for (String name : FuelManager.all().get(group).keySet()) {
                FuelInfoRecipe r = new FuelInfoRecipe(group, name);
                if (!r.getVariants().isEmpty()) out.add(r);
            }
        }
        return out;
    }

    private List<IsotopeInfoRecipe> isotopeInfoRecipes() {
        List<IsotopeInfoRecipe> out = new ArrayList<>();
        for (String name : Materials.isotopes()) {
            IsotopeInfoRecipe r = new IsotopeInfoRecipe(name);
            if (!r.getVariants().isEmpty()) out.add(r);
        }
        return out;
    }

    private List<ParticleRecipe> particleRecipes() {
        List<ParticleRecipe> recipes = new ArrayList<>();
        for (Particle particle : Particles.particles.values()) {
            recipes.add(new ParticleRecipe(rl(particle.getName()), particle));
        }
        return recipes;
    }

    private List<HeatSinkPlacementRecipe> heatSinkPlacementRecipes() {
        List<HeatSinkPlacementRecipe> recipes = new ArrayList<>();
        
        for (Map.Entry<String, HeatSinkDef> entry : FissionReactorRegistration.heatsinks.entrySet()) {
            String heatSinkName = entry.getKey();
            HeatSinkDef heatSinkDef = entry.getValue();
            
            // Skip empty and active heat sinks as they don't have placement rules
            if (heatSinkName.equals("empty") || heatSinkName.equals("active")) {
                continue;
            }
            
            // Get the heat sink item
            String blockKey = heatSinkName + "_heat_sink";
            if (FissionReactorRegistration.FISSION_BLOCKS.containsKey(blockKey)) {
                ItemStack heatSinkItem = new ItemStack(FissionReactorRegistration.FISSION_BLOCKS.get(blockKey).get());
                recipes.add(new HeatSinkPlacementRecipe(rl(blockKey), heatSinkDef, heatSinkItem));
            }
        }
        
        return recipes;
    }

    private List<CoolerPlacementRecipe> coolerPlacementRecipes() {
        List<CoolerPlacementRecipe> recipes = new ArrayList<>();
        
        for (Map.Entry<String, CoolerDef> entry : AcceleratorRegistration.COOLERS.entrySet()) {
            String coolerName = entry.getKey();
            CoolerDef coolerDef = entry.getValue();
            
            // Skip empty coolers as they don't have placement rules
            if (coolerName.equals("empty")) {
                continue;
            }
            
            // Get the cooler item
            String blockKey = coolerName + "_cooler";
            if (AcceleratorRegistration.ACCELERATOR_BLOCKS.containsKey(blockKey)) {
                ItemStack coolerItem = new ItemStack(AcceleratorRegistration.ACCELERATOR_BLOCKS.get(blockKey).get());
                recipes.add(new CoolerPlacementRecipe(rl(blockKey), coolerDef, coolerItem));
            }
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
            "linear_accelerator.nbt",
            "ring_accelerator.nbt",
            "target_chamber.nbt",
            "decay_chamber.nbt",
            "collision_chamber.nbt",
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
                if(name.contains("creative")) {
                    return List.of();
                }
                IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(xPos, yPos, width, height, getRecipeTypes().get(name));
                return List.of(clickableArea);
            }
        });
    }

    public  void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        for (String name : getRecipeTypes().keySet()) {
            if (!Processors.registered().containsKey(name) || name.contains("creative")) continue;
            addRecipeClickArea(registration, NCProcessorScreen.class, 67, 74, 18, 18, getRecipeType(name));
        }
        registration.addRecipeClickArea(FissionControllerScreen.class,72, 38, 36, 26, FISSION);
        registration.addRecipeClickArea(MSRControllerScreen.class,72, 38, 36, 26, MSR);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // Register recipe transfer handlers for all processor types
        for (String name : getRecipeTypes().keySet()) {
            if (!Processors.all().containsKey(name)) continue;
            registration.addRecipeTransferHandler(new ProcessorRecipeTransferHandler<>(getRecipeType(name)), getRecipeType(name));
        }
        registration.addRecipeTransferHandler(new EngineersCrafterRecipeTransferHandler(registration.getTransferHelper()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeTransferHandler(new EngineersEncoderRecipeTransferHandler(), mezz.jei.api.constants.RecipeTypes.CRAFTING);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        for(String  name: getRecipeTypes().keySet()) {
            if(!CATALYSTS.containsKey(name)) continue;
            for(ItemStack stack: CATALYSTS.get(name)) {
                registry.addRecipeCatalyst(stack, getRecipeType(name));
            }
        }

        if(CATALYSTS.containsKey(MSRControllerBE.NAME)) {
            registry.addRecipeCatalyst(CATALYSTS.get(MSRControllerBE.NAME).get(0), MSR);
        }
        if(CATALYSTS.containsKey(FissionControllerBE.NAME)) {
            registry.addRecipeCatalyst(CATALYSTS.get(FissionControllerBE.NAME).get(0), FISSION);
        }
        if(CATALYSTS.containsKey(HeatExchangerControllerBE.NAME)) {
            registry.addRecipeCatalyst(CATALYSTS.get(HeatExchangerControllerBE.NAME).get(0), HEAT_EXCHANGER);
        }

        registry.addRecipeCatalyst(new ItemStack(igentuman.nc.setup.registration.NCCrafter.ENGINEERS_CRAFTING_TABLE_ITEM.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);

        registry.addRecipeCatalyst(ACCELERATOR_BLOCKS.get("linear_accelerator_controller").get(), ACCELERATOR_COOLANT);
        registry.addRecipeCatalyst(ACCELERATOR_BLOCKS.get("ring_accelerator_controller").get(), ACCELERATOR_COOLANT);
        if(CATALYSTS.containsKey("nc_ore_veins")) {
            registry.addRecipeCatalyst(CATALYSTS.get("nc_ore_veins").get(0), ORE_VEINS);
        }
       // registry.addRecipeCatalyst(new ItemStack(NC_FOOD.get("smore").get()));
    }
}
