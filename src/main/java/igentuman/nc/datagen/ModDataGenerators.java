package igentuman.nc.datagen;

import igentuman.nc.datagen.loot.ModBlockLootTableProvider;
import igentuman.nc.datagen.loot.ModEntityLootTableProvider;
import igentuman.nc.datagen.recipe.ModRecipeProvider;
import igentuman.nc.datagen.recipe.CompatProcessorRecipeProvider;
import igentuman.nc.datagen.tag.ModBlockTagProvider;
import igentuman.nc.datagen.tag.ModFluidTagProvider;
import igentuman.nc.datagen.tag.ModItemTagProvider;
import igentuman.nc.datagen.tag.ModPoiTypeTagProvider;
import igentuman.nc.datagen.tag.ModVillagerTradeTagProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static java.util.Collections.emptySet;

/**
 * Registers all data providers (tags, recipes, models, lang, loot) on the datagen event. The
 * {@code data} run uses {@code clientData()}, so the client event emits both assets and data.
 */
@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ModDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createBlockAndItemTags(ModBlockTagProvider::new, ModItemTagProvider::new);
        event.createProvider(ModFluidTagProvider::new);
        event.createProvider(ModPoiTypeTagProvider::new);
        ModDatapackProvider datapackProvider = event.createProvider(ModDatapackProvider::new);
        event.createProvider(output -> new ModVillagerTradeTagProvider(output, datapackProvider.getRegistryProvider()));
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(CompatProcessorRecipeProvider::new);
        event.createProvider(output -> new ModLanguageProvider(output, "en_us"));
        event.createProvider(output -> new ModModelProvider(output, event.getResourceManager(PackType.CLIENT_RESOURCES)));
        event.createProvider((output, lookup) -> new LootTableProvider(
                output,
                emptySet(),
                List.of(
                        new SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK),
                        new SubProviderEntry(ModEntityLootTableProvider::new, LootContextParamSets.ENTITY)
                ),
                lookup
        ));
    }
}
