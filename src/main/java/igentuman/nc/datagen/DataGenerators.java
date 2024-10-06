package igentuman.nc.datagen;

import igentuman.nc.datagen.blockstates.NCBlockStates;
import igentuman.nc.datagen.blockstates.NCFluidBlockStates;
import igentuman.nc.datagen.models.NCItemModels;
import igentuman.nc.datagen.recipes.NCRecipes;
import igentuman.nc.datagen.tags.*;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        NcIngredient.ping();
        generator.addProvider(event.includeServer(), new NCRecipes(generator));
        generator.addProvider(event.includeServer(), new LootTableProvider(generator.getPackOutput(), Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(NCLootTables::new, LootContextParamSets.BLOCK))));

        NCBlockTags blockTags = new NCBlockTags(generator, event);

        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new NCItemTags(generator, blockTags, event));
        generator.addProvider(event.includeServer(), new FluidTags(generator, event));
        generator.addProvider(event.includeServer(), new NCBiomeTags(generator, event));
        generator.addProvider(event.includeServer(), new NCStructureSetTags(generator, event));
        generator.addProvider(event.includeClient(), new NCBlockStates(generator, event));
        generator.addProvider(event.includeClient(), new NCFluidBlockStates(generator, event));
        generator.addProvider(event.includeClient(), new NCItemModels(generator, event));
        generator.addProvider(event.includeClient(), new NCLanguageProvider(generator, "en_us"));
        generator.addProvider(event.includeServer(), new NCWorldGenProvider(generator, event));
    }
}