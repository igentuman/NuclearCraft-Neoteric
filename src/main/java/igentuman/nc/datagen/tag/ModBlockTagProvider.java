package igentuman.nc.datagen.tag;

import igentuman.nc.multiblock.fission.FissionTags;
import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

import static igentuman.nc.Main.MODID;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MODID, existingFileHelper);
    }

    private static final TagKey<Block> HEAT_SINKS_TAG = TagKey.create(
            net.minecraft.core.registries.Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(MODID, "heat_sinks"));

    @Override
    public void addTags(HolderLookup.Provider provider) {
        for (HeatSinkEntry entry : ModEntries.HEAT_SINKS.values()) {
            if (!entry.name.equals("empty")) {
                tag(BlockTags.MINEABLE_WITH_PICKAXE).add(entry.block().get());
                tag(BlockTags.NEEDS_IRON_TOOL).add(entry.block().get());
                if (!entry.def().isActive()) {
                    tag(HEAT_SINKS_TAG).add(entry.block().get());
                }
            }
        }

        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.hasBlock()) {
                tag(BlockTags.MINEABLE_WITH_PICKAXE).add(entry.block().get());
                tag(BlockTags.NEEDS_IRON_TOOL).add(entry.block().get());
            }

            MaterialEntry material = entry.materialEntry();
            if (material == null) continue;

            String name = material.name;

            if (material.hasOre()) {
                tag(BlockTags.MINEABLE_WITH_PICKAXE).add(material.oreBlock().get());
                tag(BlockTags.NEEDS_IRON_TOOL).add(material.oreBlock().get());
                tag(Tags.Blocks.ORES).add(material.oreBlock().get());
                tag(blockTag("ores/" + name)).add(material.oreBlock().get());
            }

            if (material.hasBlock()) {
                tag(BlockTags.MINEABLE_WITH_PICKAXE).add(material.storageBlock().get());
                tag(BlockTags.NEEDS_IRON_TOOL).add(material.storageBlock().get());
                tag(Tags.Blocks.STORAGE_BLOCKS).add(material.storageBlock().get());
                tag(blockTag("storage_blocks/" + name)).add(material.storageBlock().get());
            }
        }

        addFissionStructureTags();
    }

    private void addFissionStructureTags() {
        tag(FissionTags.CASING).add(
                b("fission_reactor_controller"), b("fission_reactor_casing"),
                b("fission_reactor_glass"), b("fission_reactor_port"));

        tag(FissionTags.MODERATORS).add(storage("graphite"), storage("beryllium"));

        var inner = tag(FissionTags.REACTOR_INNER);
        inner.add(storage("graphite"), storage("beryllium"));
        inner.add(
                b("fission_reactor_solid_fuel_cell"),
                b("fission_reactor_irradiation_chamber"),
                b("fission_reactor_pile-driver_irradiation_chamber"));
        for (HeatSinkEntry hs : ModEntries.HEAT_SINKS.values()) {
            if (!hs.name.equals("empty")) inner.add(hs.block().get());
        }
    }

    private static Block b(String name) {
        return ModEntries.get(name).block().get();
    }

    private static Block storage(String material) {
        return ModEntries.get(material).materialEntry().storageBlock().get();
    }

    private static TagKey<Block> blockTag(String path) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
