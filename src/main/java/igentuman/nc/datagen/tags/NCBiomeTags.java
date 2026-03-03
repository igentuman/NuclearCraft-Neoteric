package igentuman.nc.datagen.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.WorldGeneration.*;
import static net.minecraft.tags.BiomeTags.IS_OVERWORLD;

public class NCBiomeTags extends TagsProvider<Biome> {

    public NCBiomeTags(DataGenerator generator, GatherDataEvent event) {
        super(generator.getPackOutput(), net.minecraft.core.registries.Registries.BIOME, event.getLookupProvider(), MODID, event.getExistingFileHelper());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(WASTELAND).addOptional(WASTELAND_BIOME.location());
        tag(Tags.Biomes.IS_WASTELAND).addOptional(WASTELAND_BIOME.location());
        tag(IS_OVERWORLD).addOptional(WASTELAND_BIOME.location());
        tag(IS_OVERWORLD).addOptional(WASTELAND.location());
    }

    @Override
    public String getName() {
        return "NuclearCraft Biome Tags";
    }
}

