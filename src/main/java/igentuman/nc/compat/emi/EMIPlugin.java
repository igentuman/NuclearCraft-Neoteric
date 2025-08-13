package igentuman.nc.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import igentuman.nc.block.accelerator.entity.LinearAcceleratorControllerBE;
import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.block.fusion.entity.FusionCoreBE;
import igentuman.nc.block.kugelblitz.entity.ChamberTerminalBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.block.turbine.entity.TurbineControllerBE;
import igentuman.nc.client.NcClient;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.OreVeinRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_BLOCKS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static net.minecraft.world.item.Items.BARRIER;

@EmiEntrypoint
public class EMIPlugin implements EmiPlugin {
    
    private static final Map<String, EmiRecipeCategory> CATEGORIES = new HashMap<>();
    
    @Override
    public void register(EmiRegistry registry) {
        // Register processor categories
        registerProcessorCategories(registry);
        
        // Register special multiblock categories
        registerMultiblockCategories(registry);
        
        // Register workstations
        registerWorkstations(registry);
    }
    
    private void registerProcessorCategories(EmiRegistry registry) {
        for (String name : RECIPE_CLASSES.keySet()) {
            if (Processors.all().containsKey(name) && !Processors.all().get(name).isRegistered()) {
                continue;
            }
            
            // Skip special categories that have their own implementations
            if (name.equals("fusion_core") || name.equals("fusion_coolant") ||
                name.equals("fission_reactor_controller") || name.equals("fission_boiling") ||
                name.equals("kugelblitz_chamber") || name.equals("target_chamber") ||
                name.equals("turbine_controller") || name.equals("accelerator_coolant") ||
                name.equals("nc_ore_veins")) {
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
    }
    
    private void registerWorkstations(EmiRegistry registry) {
        // Register workstations for all categories
        for (Map.Entry<String, EmiRecipeCategory> entry : CATEGORIES.entrySet()) {
            String name = entry.getKey();
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
            case "fusion_core":
            case "fusion_coolant":
                return EmiStack.of(new ItemStack(FUSION_BLOCKS.get("fusion_core").get()));
            case "accelerator_coolant":
                return EmiStack.of(new ItemStack(ACCELERATOR_BLOCKS.get("linear_accelerator_controller").get()));
            case "fission_reactor_controller":
            case "fission_boiling":
                return EmiStack.of(new ItemStack(FISSION_BLOCKS.get("fission_reactor_controller").get()));
            case "turbine_controller":
                return EmiStack.of(new ItemStack(TURBINE_BLOCKS.get("turbine_controller").get()));
            case "nc_ore_veins":
                return EmiStack.of(new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE));
            default:
                return EmiStack.of(new ItemStack(BARRIER));
        }
    }
}