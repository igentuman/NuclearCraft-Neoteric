package igentuman.nc.datagen;

import igentuman.nc.datagen.blockstates.NCBlockStates;
import igentuman.nc.datagen.blockstates.NCFluidBlockStates;
import igentuman.nc.datagen.models.NCItemModels;
import igentuman.nc.datagen.recipes.NCRecipes;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.world.ore.Generator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

        /*CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider().thenApply(provider -> {
            return registrySetBuilder().buildPatch(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), provider);
        });*/
    }

/*    private static RegistrySetBuilder registrySetBuilder()
    {
        return new RegistrySetBuilder()
                .add(Registries.NOISE, MSNoiseParametersProvider::register)
                .add(Registries.DENSITY_FUNCTION, MSDensityFunctionProvider::register)
                .add(Registries.CONFIGURED_FEATURE, MSConfiguredFeatureProvider::register)

                .add(Registries.BIOME, MSBiomeProvider::register)
                .add(Registries.STRUCTURE, MSStructureProvider::register)
                .add(Registries.STRUCTURE_SET, MSStructureSetProvider::register)
                .add(Registries.DAMAGE_TYPE, MSDamageTypeProvider::register)
                //.add(Registries.PLACED_FEATURE, Generator::register)
                .add(ForgeRegistries.Keys.BIOME_MODIFIERS, BiomeModifierProvider::register);
    }*/
}