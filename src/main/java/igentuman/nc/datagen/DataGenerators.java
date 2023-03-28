package igentuman.nc.datagen;

import igentuman.nc.datagen.blockstates.NCBlockStates;
import igentuman.nc.datagen.blockstates.NCFluidBlockStates;
import igentuman.nc.datagen.models.NCItemModels;
import igentuman.nc.datagen.recipes.NCRecipes;
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
        generator.addProvider(event.includeServer(), new NCRecipes(generator));
        generator.addProvider(event.includeServer(), new NCLootTables(generator));
        NCBlockTags blockTags = new NCBlockTags(generator, event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new NCItemTags(generator, blockTags, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new FluidTags(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new NCBiomeTags(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new NCStructureSetTags(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new NCBlockStates(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new NCFluidBlockStates(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new NCItemModels(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new NCLanguageProvider(generator, "en_us"));
        BiomeModifierProvider.addTo(generator, event.getExistingFileHelper(), d -> generator.addProvider(true, d));
    }
}