package igentuman.nc.datagen;

import net.minecraft.core.HolderLookup;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import static igentuman.nc.NuclearCraft.MODID;

public class NCBiomeTags extends TagsProvider<Biome> {

    public NCBiomeTags(DataGenerator generator, GatherDataEvent event) {
        super(generator.getPackOutput(), ForgeRegistries.BIOMES.getRegistryKey(), event.getLookupProvider(), MODID, event.getExistingFileHelper());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        ForgeRegistries.BIOMES.getValues().forEach(biome -> {

        });
    }

    @Override
    public String getName() {
        return "NuclearCraft Tags";
    }
}