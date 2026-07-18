package igentuman.nc.datagen.tags;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.WorldGeneration.*;
import static net.minecraft.tags.BiomeTags.IS_OVERWORLD;

public class NCBiomeTags extends ForgeRegistryTagsProvider<Biome> {

    public NCBiomeTags(DataGenerator generator, GatherDataEvent event) {
        super(generator, ForgeRegistries.BIOMES, MODID, event.getExistingFileHelper());
    }

    @Override
    protected void addTags() {
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
