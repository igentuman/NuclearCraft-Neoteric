package igentuman.nc.datagen;

import igentuman.nc.datagen.loot.ModBlockLootTableProvider;
import igentuman.nc.datagen.recipe.ModRecipeProvider;
import igentuman.nc.datagen.tag.ModBlockTagProvider;
import igentuman.nc.datagen.tag.ModFluidTagProvider;
import igentuman.nc.datagen.tag.ModItemTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static igentuman.nc.Main.MODID;
import static java.util.Collections.emptySet;

@EventBusSubscriber(modid = MODID)
public class ModDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        ModBlockTagProvider blockTagProvider = new ModBlockTagProvider(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagProvider);
        generator.addProvider(event.includeServer(), new ModItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new ModFluidTagProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModDatapackProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(generator, "en_us"));
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));

        generator.addProvider(
                event.includeServer(),
                new LootTableProvider(
                        packOutput,
                        emptySet(),
                        List.of(
                                new SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK)
                                //new SubProviderEntry(ModEntityLootTableProvider::new, LootContextParamSets.ENTITY)
                        ),
                        lookupProvider
                )
        );
    }
}
