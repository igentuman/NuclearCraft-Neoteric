package igentuman.nc.compat.emi;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import igentuman.nc.block.accelerator.entity.LinearAcceleratorControllerBE;
import igentuman.nc.block.collision_chamber.entity.CollisionChamberControllerBE;
import igentuman.nc.block.decay_chamber.entity.DecayChamberControllerBE;
import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.block.fission.entity.MSRControllerBE;
import igentuman.nc.block.fusion.entity.FusionCoreBE;
import igentuman.nc.block.kugelblitz.entity.ChamberTerminalBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.block.turbine.entity.TurbineControllerBE;
import igentuman.nc.client.NcClient;
import igentuman.nc.client.gui.processor.NCProcessorScreen;
import igentuman.nc.compat.ae2.ProcessorEmiRecipeHandlerAE2;
import igentuman.nc.compat.emi.ingredient.ParticleEmiStack;
import igentuman.nc.compat.refined_storage.ProcessorEmiRecipeHandlerRS;
import igentuman.nc.handler.config.ClientConfig;
import igentuman.nc.compat.jei.FuelInfoRecipe;
import igentuman.nc.compat.jei.IsotopeInfoRecipe;
import igentuman.nc.compat.jei.ParticleRecipe;
import igentuman.nc.compat.jei.ParticleSourceRecipe;
import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.particles.ParticleSources;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.Particles;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.multiblock.accelerator.AcceleratorRegistration;
import igentuman.nc.multiblock.accelerator.CoolerDef;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.multiblock.fission.HeatSinkDef;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.type.NuclearBlastRecipe;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.OreVeinRecipe;
import igentuman.nc.setup.registration.FissionFuel;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.ModUtil;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_BLOCKS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.ION_SOURCES;
import static igentuman.nc.setup.registration.NCItems.UNKNOWN_INGREDIENT;

import static net.minecraft.world.item.Items.BARRIER;

@EmiEntrypoint
public class EMIPlugin implements EmiPlugin {
    
    private static final Map<String, EmiRecipeCategory> CATEGORIES = new HashMap<>();

    public static void displayRecipes(AbstractContainerScreen<?> screen) {
        EmiRecipeCategory cat = getRecipeCategory(screen);
        if(cat != null) {
            EmiApi.displayRecipeCategory(cat);
        }
    }

    private static EmiRecipeCategory getRecipeCategory(AbstractContainerScreen<?> screen) {
        if(screen instanceof NCProcessorScreen processorScreen) {
            if(CATEGORIES.containsKey(processorScreen.getRecipeTypeName())) {
                return CATEGORIES.get(processorScreen.getRecipeTypeName());
            }
        }

        return null;
    }

    @Override
    public void register(EmiRegistry registry) {
        // Register ParticleStack renderer
        registerParticleStackRenderer(registry);
        
        // Register processor categories
        registerProcessorCategories(registry);
        
        // Register special multiblock categories
        registerMultiblockCategories(registry);
        
        // Register particle source category
        registerParticleSourceCategory(registry);
        
        // Register particle info category
        registerParticleInfoCategory(registry);
        
        // Register kugelblitz info category
        registerKugelblitzInfoCategory(registry);
        
        // Register heat sink placement category
        registerHeatSinkPlacementCategory(registry);
        
        // Register cooler placement category
        registerCoolerPlacementCategory(registry);

        // Register fuel info category
        registerFuelInfoCategory(registry);

        // Register isotope info category
        registerIsotopeInfoCategory(registry);

        // Register workstations
        registerWorkstations(registry);

        // Register recipe handlers
        registerRecipeHandlers(registry);

        // Hide fuel and isotope variant items/fluids from EMI index
        hideFuelAndIsotopeVariants(registry);
    }
    
    private void registerProcessorCategories(EmiRegistry registry) {
        for (String name : RECIPE_CLASSES.keySet()) {
            if(name.equals(Processors.NUCLEAR_FURNACE)) continue;
            if (Processors.all().containsKey(name) && !Processors.all().get(name).isRegistered()) {
                continue;
            }
            
            // Skip special categories that have their own implementations
            if (name.equals("fusion_core") || name.equals("fusion_coolant") ||
                name.equals("fission_reactor_controller") || name.equals("fission_boiling") ||
                name.equals("kugelblitz_chamber") || name.equals("target_chamber") ||
                name.equals("collision_chamber") || name.equals("decay_chamber") ||
                name.equals("turbine_controller") || name.equals("accelerator_coolant") ||
                name.equals("msr_controller") || name.equals("nc_ore_veins") ||
                name.equals("nuclear_blast")) {
                continue;
            }
            
            EmiRecipeCategory category = ProcessorEmiCategory.createCategory(name);
            CATEGORIES.put(name, category);
            registry.addCategory(category);
            
            // Register recipes for this category
            var recipes = NcRecipeType.ALL_RECIPES.get(name).getRecipes(NcClient.tryGetClientWorld());
            for (var recipe : recipes) {
                registry.addRecipe(new ProcessorEmiCategory(category, (NcRecipe) recipe, name));
            }
        }
    }
    
    private void registerMultiblockCategories(EmiRegistry registry) {
        // Register Kugelblitz category
        registry.addCategory(KugelblitzEmiCategory.CATEGORY);
        CATEGORIES.put("kugelblitz_chamber", KugelblitzEmiCategory.CATEGORY);
        
        var kugelblitzRecipes = NcRecipeType.ALL_RECIPES.get("kugelblitz_chamber").getRecipes(NcClient.tryGetClientWorld());
        for (var recipe : kugelblitzRecipes) {
            if (recipe instanceof ChamberTerminalBE.Recipe chamberRecipe) {
                registry.addRecipe(new KugelblitzEmiCategory(chamberRecipe));
            }
        }

            // Register Target Chamber category
        registry.addCategory(TargetChamberEmiCategory.CATEGORY);
        CATEGORIES.put("target_chamber", TargetChamberEmiCategory.CATEGORY);

        var targetChamberRecipes = NcRecipeType.ALL_RECIPES.get("target_chamber").getRecipes(NcClient.tryGetClientWorld());
        for (var recipe : targetChamberRecipes) {
            if (recipe instanceof TargetChamberControllerBE.Recipe targetRecipe) {
                registry.addRecipe(new TargetChamberEmiCategory(targetRecipe));
            }
        }

        // Register Collision Chamber category
        registry.addCategory(CollisionChamberEmiCategory.CATEGORY);
        CATEGORIES.put("collision_chamber", CollisionChamberEmiCategory.CATEGORY);

        var collisionChamberRecipes = NcRecipeType.ALL_RECIPES.get("collision_chamber").getRecipes(NcClient.tryGetClientWorld());
        for (var recipe : collisionChamberRecipes) {
            if (recipe instanceof CollisionChamberControllerBE.Recipe collisionRecipe) {
                registry.addRecipe(new CollisionChamberEmiCategory(collisionRecipe));
            }
        }

        // Register Decay Chamber category
        registry.addCategory(DecayChamberEmiCategory.CATEGORY);
        CATEGORIES.put("decay_chamber", DecayChamberEmiCategory.CATEGORY);

        var decayChamberRecipes = NcRecipeType.ALL_RECIPES.get("decay_chamber").getRecipes(NcClient.tryGetClientWorld());
        for (var recipe : decayChamberRecipes) {
            if (recipe instanceof DecayChamberControllerBE.Recipe decayRecipe) {
                registry.addRecipe(new DecayChamberEmiCategory(decayRecipe));
            }
        }

        // Register Fusion category
        registry.addCategory(FusionEmiCategory.CATEGORY);
        CATEGORIES.put("fusion_core", FusionEmiCategory.CATEGORY);
        
        var fusionRecipes = NcRecipeType.ALL_RECIPES.get("fusion_core").getRecipes(NcClient.tryGetClientWorld());
        for (var recipe : fusionRecipes) {
            if (recipe instanceof FusionCoreBE.Recipe fusionRecipe) {
                registry.addRecipe(new FusionEmiCategory(fusionRecipe));
            }
        }
        
        // Register Fusion Coolant category
        registry.addCategory(FusionCoolantEmiCategory.CATEGORY);
        CATEGORIES.put("fusion_coolant", FusionCoolantEmiCategory.CATEGORY);
        
        var fusionCoolantRecipes = NcRecipeType.ALL_RECIPES.get("fusion_coolant").getRecipes(NcClient.tryGetClientWorld());
        for (var recipe : fusionCoolantRecipes) {
            if (recipe instanceof FusionCoreBE.FusionCoolantRecipe coolantRecipe) {
                registry.addRecipe(new FusionCoolantEmiCategory(coolantRecipe));
            }
        }
        
        // Register Accelerator Coolant category
        registry.addCategory(AcceleratorCoolantEmiCategory.CATEGORY);
        CATEGORIES.put("accelerator_coolant", AcceleratorCoolantEmiCategory.CATEGORY);
        
        var acceleratorCoolantRecipes = NcRecipeType.ALL_RECIPES.get("accelerator_coolant").getRecipes(NcClient.tryGetClientWorld());
        for (var recipe : acceleratorCoolantRecipes) {
            if (recipe instanceof LinearAcceleratorControllerBE.CoolantRecipe coolantRecipe) {
                registry.addRecipe(new AcceleratorCoolantEmiCategory(coolantRecipe));
            }
        }
        
        // Register Fission category
        registry.addCategory(FissionEmiCategory.CATEGORY);
        CATEGORIES.put("fission_reactor_controller", FissionEmiCategory.CATEGORY);
        
        var fissionRecipes = NcRecipeType.ALL_RECIPES.get("fission_reactor_controller").getRecipes(NcClient.tryGetClientWorld());
        for (var recipe : fissionRecipes) {
            if (recipe instanceof FissionControllerBE.Recipe fissionRecipe) {
                registry.addRecipe(new FissionEmiCategory(fissionRecipe));
            }
        }

        // Register MSR category
        registry.addCategory(MSREmiCategory.CATEGORY);
        CATEGORIES.put("msr_controller", MSREmiCategory.CATEGORY);

        var msrRecipes = NcRecipeType.ALL_RECIPES.get("msr_controller").getRecipes(NcClient.tryGetClientWorld());
        for (var recipe : msrRecipes) {
            if (recipe instanceof MSRControllerBE.Recipe msrRecipe) {
                registry.addRecipe(new MSREmiCategory(msrRecipe));
            }
        }
        
        // Register Fission Boiling category
        registry.addCategory(FissionBoilingEmiCategory.CATEGORY);
        CATEGORIES.put("fission_boiling", FissionBoilingEmiCategory.CATEGORY);
        
        var fissionBoilingRecipes = NcRecipeType.ALL_RECIPES.get("fission_boiling").getRecipes(NcClient.tryGetClientWorld());
        for (var recipe : fissionBoilingRecipes) {
            if (recipe instanceof FissionControllerBE.FissionBoilingRecipe boilingRecipe) {
                registry.addRecipe(new FissionBoilingEmiCategory(boilingRecipe));
            }
        }
        
        // Register Turbine Controller category
        registry.addCategory(TurbineControllerEmiCategory.CATEGORY);
        CATEGORIES.put("turbine_controller", TurbineControllerEmiCategory.CATEGORY);
        
        var turbineRecipes = NcRecipeType.ALL_RECIPES.get("turbine_controller").getRecipes(NcClient.tryGetClientWorld());
        for (var recipe : turbineRecipes) {
            if (recipe instanceof TurbineControllerBE.Recipe turbineRecipe) {
                registry.addRecipe(new TurbineControllerEmiCategory(turbineRecipe));
            }
        }
        
        // Register Ore Veins category
        registry.addCategory(OreVeinEmiCategory.CATEGORY);
        CATEGORIES.put("nc_ore_veins", OreVeinEmiCategory.CATEGORY);
        
        var oreVeinRecipes = NcRecipeType.ALL_RECIPES.get("nc_ore_veins").getRecipes(NcClient.tryGetClientWorld());
        for (var recipe : oreVeinRecipes) {
            if (recipe instanceof OreVeinRecipe oreVeinRecipe) {
                registry.addRecipe(new OreVeinEmiCategory(oreVeinRecipe));
            }
        }

        // Register Nuclear Blast category
        registry.addCategory(NuclearBlastEmiCategory.CATEGORY);
        CATEGORIES.put("nuclear_blast", NuclearBlastEmiCategory.CATEGORY);

        var nuclearBlastRecipes = NcRecipeType.ALL_RECIPES.get("nuclear_blast").getRecipes(NcClient.tryGetClientWorld());
        for (var recipe : nuclearBlastRecipes) {
            if (recipe instanceof NuclearBlastRecipe blastRecipe) {
                registry.addRecipe(new NuclearBlastEmiCategory(blastRecipe));
            }
        }
    }
    
    private void registerWorkstations(EmiRegistry registry) {
        // Register workstations for all categories
        for (Map.Entry<String, EmiRecipeCategory> entry : CATEGORIES.entrySet()) {
            String name = entry.getKey();
            
            // Skip info categories that don't need workstations
            if (name.equals("particle_info") || name.equals("kugelblitz_info")
                    || name.equals("fuel_info") || name.equals("isotope_info")) {
                continue;
            }
            
            EmiRecipeCategory category = entry.getValue();
            
            EmiStack workstation = getWorkstationForCategory(name);
            if (workstation != null) {
                registry.addWorkstation(category, workstation);
            }
        }
    }
    
    private EmiStack getWorkstationForCategory(String categoryName) {
        if (CATALYSTS.containsKey(categoryName)) {
            return EmiStack.of(CATALYSTS.get(categoryName).get(0));
        }
        
        // Special cases for multiblocks
        switch (categoryName) {
            case "kugelblitz_chamber":
                return EmiStack.of(new ItemStack(KUGELBLITZ_BLOCKS.get("chamber_terminal").get()));
            case "target_chamber":
                return EmiStack.of(new ItemStack(ACCELERATOR_BLOCKS.get("target_chamber_controller").get()));
            case "collision_chamber":
                return EmiStack.of(new ItemStack(PARTICLE_CHAMBER_BLOCKS.get("collision_chamber_controller").get()));
            case "decay_chamber":
                return EmiStack.of(new ItemStack(PARTICLE_CHAMBER_BLOCKS.get("decay_chamber_controller").get()));
            case "fusion_core":
            case "fusion_coolant":
                return EmiStack.of(new ItemStack(FUSION_BLOCKS.get("fusion_core").get()));
            case "accelerator_coolant":
                return EmiStack.of(new ItemStack(ACCELERATOR_BLOCKS.get("linear_accelerator_controller").get()));
            case "fission_reactor_controller":
            case "msr_controller":
                return EmiStack.of(new ItemStack(FISSION_BLOCKS.get("msr_controller").get()));
            case "fission_boiling":
                return EmiStack.of(new ItemStack(FISSION_BLOCKS.get("fission_reactor_controller").get()));
            case "turbine_controller":
                return EmiStack.of(new ItemStack(TURBINE_BLOCKS.get("turbine_controller").get()));
            case "nc_ore_veins":
                return EmiStack.of(new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE));
            case "particle_source_info":
                return EmiStack.of(new ItemStack(ACCELERATOR_BLOCKS.get("linear_accelerator_controller").get()));
            case "heat_sink_placement":
                return EmiStack.of(new ItemStack(FISSION_BLOCKS.get("empty_heat_sink").get()));
            case "cooler_placement":
                return EmiStack.of(new ItemStack(ACCELERATOR_BLOCKS.get("empty_cooler").get()));
            default:
                return EmiStack.of(new ItemStack(BARRIER));
        }
    }
    
    private void registerParticleSourceCategory(EmiRegistry registry) {
        // Register Particle Source category
        registry.addCategory(ParticleSourceEmiCategory.CATEGORY);
        CATEGORIES.put("particle_source_info", ParticleSourceEmiCategory.CATEGORY);
        
        // Generate and register particle source recipes
        List<ParticleSourceRecipe> particleSourceRecipes = particleSourceRecipes();
        for (ParticleSourceRecipe recipe : particleSourceRecipes) {
            registry.addRecipe(new ParticleSourceEmiCategory(recipe));
        }
    }
    
    private List<ParticleSourceRecipe> particleSourceRecipes() {
        List<ParticleSourceRecipe> recipes = new ArrayList<>();
        
        // Add item-based particle sources
        for (String item : ParticleSources.sources.keySet()) {
            if (ParticleSources.sources.get(item).getParticle() == null) {
                continue;
            }
            recipes.add(new ParticleSourceRecipe(
                rl(item), 
                new ItemStack(ION_SOURCES.get(item).get()), 
                null, 
                ParticleSources.sources.get(item).getParticle()
            ));
        }

        // Add fluid-based particle sources
        for (String fluid : ParticleSources.fluidSources.keySet()) {
            if (NCFluids.NC_GASES.containsKey(fluid)) {
                recipes.add(new ParticleSourceRecipe(
                    rl(fluid), 
                    null, 
                    new FluidStack(NCFluids.NC_GASES.get(fluid).still().get(), 1000), 
                    ParticleSources.fluidSources.get(fluid).getParticle()
                ));
            }
        }
        
        return recipes;
    }
    
    private void registerParticleInfoCategory(EmiRegistry registry) {
        // Register Particle Info category
        registry.addCategory(ParticleInfoEmiCategory.CATEGORY);
        CATEGORIES.put("particle_info", ParticleInfoEmiCategory.CATEGORY);
        
        // Generate and register particle info recipes
        List<ParticleRecipe> particleInfoRecipes = particleInfoRecipes();
        for (ParticleRecipe recipe : particleInfoRecipes) {
            registry.addRecipe(new ParticleInfoEmiCategory(recipe));
        }
    }
    
    private void registerKugelblitzInfoCategory(EmiRegistry registry) {
        // Register Kugelblitz Info category
        registry.addCategory(KugelblitzInfoEmiCategory.CATEGORY);
        CATEGORIES.put("kugelblitz_info", KugelblitzInfoEmiCategory.CATEGORY);
        
        // Add info recipes for chamber terminal and unknown ingredient
        registry.addRecipe(new KugelblitzInfoEmiCategory(
            rl("/kugelblitz_chamber_terminal_info"),
            new ItemStack(KUGELBLITZ_BLOCKS.get("chamber_terminal").get())
        ));
        
        registry.addRecipe(new KugelblitzInfoEmiCategory(
            rl("/unknown_ingredient_info"),
            new ItemStack(UNKNOWN_INGREDIENT.get())
        ));
    }
    
    private void registerHeatSinkPlacementCategory(EmiRegistry registry) {
        // Register Heat Sink Placement category
        registry.addCategory(HeatSinkPlacementEmiCategory.CATEGORY);
        CATEGORIES.put("heat_sink_placement", HeatSinkPlacementEmiCategory.CATEGORY);
        
        // Generate and register heat sink placement recipes
        List<HeatSinkPlacementEmiRecipe> heatSinkPlacementRecipes = heatSinkPlacementRecipes();
        for (HeatSinkPlacementEmiRecipe recipe : heatSinkPlacementRecipes) {
            registry.addRecipe(new HeatSinkPlacementEmiCategory(recipe));
        }
    }
    
    private List<HeatSinkPlacementEmiRecipe> heatSinkPlacementRecipes() {
        List<HeatSinkPlacementEmiRecipe> recipes = new ArrayList<>();
        
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
                recipes.add(new HeatSinkPlacementEmiRecipe(rl("/"+blockKey), heatSinkDef, heatSinkItem));
            }
        }
        
        return recipes;
    }
    
    private void registerCoolerPlacementCategory(EmiRegistry registry) {
        // Register Cooler Placement category
        registry.addCategory(CoolerPlacementEmiCategory.CATEGORY);
        CATEGORIES.put("cooler_placement", CoolerPlacementEmiCategory.CATEGORY);
        
        // Generate and register cooler placement recipes
        List<CoolerPlacementEmiRecipe> coolerPlacementRecipes = coolerPlacementRecipes();
        for (CoolerPlacementEmiRecipe recipe : coolerPlacementRecipes) {
            registry.addRecipe(new CoolerPlacementEmiCategory(recipe));
        }
    }
    
    private List<CoolerPlacementEmiRecipe> coolerPlacementRecipes() {
        List<CoolerPlacementEmiRecipe> recipes = new ArrayList<>();
        
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
                recipes.add(new CoolerPlacementEmiRecipe(rl("/"+blockKey), coolerDef, coolerItem));
            }
        }
        
        return recipes;
    }
    
    private void registerFuelInfoCategory(EmiRegistry registry) {
        registry.addCategory(FuelInfoEmiCategory.CATEGORY);
        CATEGORIES.put("fuel_info", FuelInfoEmiCategory.CATEGORY);

        for (String group : FuelManager.all().keySet()) {
            for (String name : FuelManager.all().get(group).keySet()) {
                FuelInfoRecipe r = new FuelInfoRecipe(group, name);
                if (!r.getVariants().isEmpty()) {
                    registry.addRecipe(new FuelInfoEmiCategory(r));
                }
            }
        }
    }

    private void registerIsotopeInfoCategory(EmiRegistry registry) {
        registry.addCategory(IsotopeInfoEmiCategory.CATEGORY);
        CATEGORIES.put("isotope_info", IsotopeInfoEmiCategory.CATEGORY);

        for (String name : Materials.isotopes()) {
            IsotopeInfoRecipe r = new IsotopeInfoRecipe(name);
            if (!r.getVariants().isEmpty()) {
                registry.addRecipe(new IsotopeInfoEmiCategory(r));
            }
        }
    }

    private static final String[] FUEL_VARIANT_SUFFIXES = {"_ox", "_ni", "_za", "_tr"};
    private static final String[] ISOTOPE_VARIANT_SUFFIXES = {"_ox", "_ni", "_za"};

    private void hideFuelAndIsotopeVariants(EmiRegistry registry) {
        Set<Object> hideKeys = new HashSet<>();

        for (Map.Entry<List<String>, net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.Item>> e : FissionFuel.NC_FUEL.entrySet()) {
            String variant = e.getKey().get(3);
            if (!variant.isEmpty()) hideKeys.add(e.getValue().get());
        }
        for (Map.Entry<List<String>, net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.Item>> e : FissionFuel.NC_DEPLETED_FUEL.entrySet()) {
            String variant = e.getKey().get(3);
            if (!variant.isEmpty()) hideKeys.add(e.getValue().get());
        }
        for (Map.Entry<String, net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.Item>> e : FissionFuel.NC_ISOTOPES.entrySet()) {
            String key = e.getKey();
            for (String suf : ISOTOPE_VARIANT_SUFFIXES) {
                if (key.endsWith(suf)) {
                    hideKeys.add(e.getValue().get());
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
            hideKeys.add(e.getValue().getStill());
            net.minecraft.world.item.BucketItem bucket = e.getValue().getBucket();
            if (bucket != null) hideKeys.add(bucket);
        }

        if (!hideKeys.isEmpty()) {
            registry.removeEmiStacks(s -> hideKeys.contains(s.getKey()));
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

    private void registerRecipeHandlers(EmiRegistry registry) {
        for(String name: NCProcessors.PROCESSORS_CONTAINERS.keySet()) {
            var menuType = NCProcessors.PROCESSORS_CONTAINERS.get(name).get();
            registry.addRecipeHandler((MenuType) menuType, new ProcessorEmiRecipeHandler<>());
        }
        if(ModUtil.isRefinedStorageLoaded()) {
            registry.addRecipeHandler(com.refinedmods.refinedstorage.RSContainerMenus.GRID.get(), new ProcessorEmiRecipeHandlerRS<>());
        }
        if(ModUtil.isAE2Loaded()) {
            registry.addRecipeHandler(appeng.menu.me.items.PatternEncodingTermMenu.TYPE, new ProcessorEmiRecipeHandlerAE2<>());
            registry.addRecipeHandler(appeng.menu.me.items.PatternEncodingTermMenu.WIRELESS_TYPE, new ProcessorEmiRecipeHandlerAE2<>());
        }
    }
    
    private List<ParticleRecipe> particleInfoRecipes() {
        List<ParticleRecipe> recipes = new ArrayList<>();
        
        // Add all particles as info recipes
        for (var particle : Particles.particles.values()) {
            recipes.add(new ParticleRecipe(rl("/"+particle.getName()), particle));
        }
        
        return recipes;
    }
    
    private void registerParticleStackRenderer(EmiRegistry registry) {
        if (!ClientConfig.MISC_CONFIG.HIDE_PARTICLES.get()) {
            for (var particle : Particles.particles.values()) {
                ParticleStack particleStack = new ParticleStack(particle, 1);
                registry.addEmiStack(new ParticleEmiStack(particleStack));
            }
        }
    }
}