package igentuman.nc.datagen;

import igentuman.nc.datagen.blockstates.NCBlockStates;
import igentuman.nc.datagen.blockstates.NCFluidBlockStates;
import igentuman.nc.datagen.models.NCItemModels;
import igentuman.nc.datagen.recipes.NCRecipes;
import igentuman.nc.datagen.tags.*;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        NcIngredient.ping();
        generator.addProvider(event.includeServer(), new NCRecipes(generator));
        generator.addProvider(event.includeServer(), new NCLootTables(generator));
        generator.addProvider(event.includeServer(), new NCEntityLootTableProvider(generator));

        NCBlockTags blockTags = new NCBlockTags(generator, event);

        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new NCItemTags(generator, blockTags, event));
        generator.addProvider(event.includeServer(), new FluidTags(generator, event));
        generator.addProvider(event.includeServer(), new NCStructureSetTags(generator, event));
        generator.addProvider(event.includeClient(), new NCBlockStates(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new NCFluidBlockStates(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new NCItemModels(generator, event));
        generator.addProvider(event.includeClient(), new NCLanguageProvider(generator, "en_us"));
        generator.addProvider(event.includeClient(), new EmiLangProvider(generator, "en_gb"));
        generator.addProvider(event.includeServer(), new NCBiomeTags(generator, event));
        generator.addProvider(event.includeServer(), new NCWorldGenProvider(generator, event));
        generator.addProvider(event.includeServer(), new PoiTypeTags(generator, event.getExistingFileHelper()));
    }
}
