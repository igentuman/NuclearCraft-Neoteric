package igentuman.nc.datagen.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.WorldGeneration.*;

public class NCBiomeTags extends TagsProvider<Biome> {

    public NCBiomeTags(DataGenerator generator, GatherDataEvent event) {
        super(generator.getPackOutput(), ForgeRegistries.BIOMES.getRegistryKey(), event.getLookupProvider(), MODID, event.getExistingFileHelper());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Add Wasteland to its custom tag
        tag(WASTELAND).addOptional(WASTELAND_BIOME.location());
        tag(Tags.Biomes.IS_WASTELAND).addOptional(WASTELAND_BIOME.location());
    }

    @Override
    public String getName() {
        return "NuclearCraft Biome Tags";
    }
}

