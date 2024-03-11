package igentuman.nc.datagen.recipes;

import igentuman.nc.datagen.recipes.recipes.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import java.util.function.Consumer;

public class CustomRecipes extends NCRecipes {
    public CustomRecipes(DataGenerator generatorIn) {
        super(generatorIn);
    }
    public static Consumer<IFinishedRecipe> consumer;

    public static void generate(Consumer<IFinishedRecipe> consumer) {
        CustomRecipes.consumer = consumer;
        FissionRecipes.generate(consumer);
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
    }
}
