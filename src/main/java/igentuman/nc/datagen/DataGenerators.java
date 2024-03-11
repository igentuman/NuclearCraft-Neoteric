package igentuman.nc.datagen;

import igentuman.nc.datagen.blockstates.NCBlockStates;
import igentuman.nc.datagen.blockstates.NCFluidBlockStates;
import igentuman.nc.datagen.models.NCItemModels;
import igentuman.nc.datagen.recipes.NCRecipes;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IDataProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        NcIngredient.ping();
        generator.addProvider(new NCRecipes(generator));
        generator.addProvider(new NCLootTables(generator));
        NCBlockTags blockTags = new NCBlockTags(generator, event.getExistingFileHelper());
        generator.addProvider(blockTags);
        generator.addProvider(new NCItemTags(generator, blockTags, event.getExistingFileHelper()));
        generator.addProvider(new FluidTags(generator, event.getExistingFileHelper()));
        generator.addProvider(new NCBlockStates(generator, event.getExistingFileHelper()));
        generator.addProvider(new NCFluidBlockStates(generator, event.getExistingFileHelper()));
        generator.addProvider(new NCItemModels(generator, event.getExistingFileHelper()));
        generator.addProvider(new NCLanguageProvider(generator, "en_us"));
    }
}