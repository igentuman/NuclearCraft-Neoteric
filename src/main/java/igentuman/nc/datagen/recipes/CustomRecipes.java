package igentuman.nc.datagen.recipes;

import igentuman.nc.datagen.recipes.recipes.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.RecipeOutput;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;

public class CustomRecipes extends NCRecipes {
    public CustomRecipes(DataGenerator generatorIn, CompletableFuture<HolderLookup.Provider> registries) {
        super(generatorIn, registries);
    }
    public static RecipeOutput consumer;

    public static void generate(RecipeOutput consumer) {
        CustomRecipes.consumer = consumer;
        KugelblitzRecipes.generate(consumer);
        FissionRecipes.generate(consumer);
        MSRRecipes.generate(consumer);
        ManufactoryRecipes.generate(consumer);
        DecayHastenerRecipes.generate(consumer);
        PressurizerRecipes.generate(consumer);
        AlloySmelterRecipes.generate(consumer);
        RockCrusherRecipes.generate(consumer);
        IsotopeSeparatorRecipes.generate(consumer);
        MelterRecipes.generate(consumer);
        IngotFormerRecipes.generate(consumer);
        FuelReprocessorRecipes.generate(consumer);
        ElectrolyzerRecipes.generate(consumer);
        ChemicalReactorRecipes.generate(consumer);
        AssemblerRecipes.generate(consumer);
        CentrifugeRecipes.generate(consumer);
        IrradiatorRecipes.generate(consumer);
        CrystalizerRecipes.generate(consumer);
        SteamTurbineRecipes.generate(consumer);
        FluidInfuserRecipes.generate(consumer);
        SupercoolerRecipes.generate(consumer);
        FluidEnricherRecipes.generate(consumer);
        ExtractorRecipes.generate(consumer);
        PumpRecipes.generate(consumer);
        GasScrubberRecipes.generate(consumer);
        AnalyzerRecipes.generate(consumer);
        LeacherRecipes.generate(consumer);
        OreVeinsRecipes.generate(consumer);
        FusionReactorRecipes.generate(consumer);
        FusionCoolantRecipes.generate(consumer);
        FissionBoilingRecipes.generate(consumer);
        TurbineControllerRecipes.generate(consumer);
        SubatomicLiquifierRecipes.generate(consumer);
        TargetChamberRecipes.generate(consumer);
        // TODO: Tinker's Construct compat — disabled, waiting on TiC to port to NeoForge 1.21.1. Re-enable when available.
        // TConstructAlloyingRecipes.generate(consumer);
        // TConstructCastingRecipes.generate(consumer);
        // TConstructMeltingRecipes.generate(consumer);
        AcceleratorCoolantRecipes.generate(consumer);
    }
}
