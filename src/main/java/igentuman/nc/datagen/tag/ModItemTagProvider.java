package igentuman.nc.datagen.tag;

import igentuman.nc.multiblock.fission.FissionTags;
import igentuman.nc.registration.ArmorSetEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.registration.ToolSetEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

import static igentuman.nc.Main.MODID;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, MODID, existingFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        copy(FissionTags.CASING, FissionTags.CASING_ITEM);
        copy(FissionTags.MODERATORS, FissionTags.MODERATORS_ITEM);

        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.toolSetEntry() instanceof ToolSetEntry toolSet) {
                tag(ItemTags.SWORDS).add(toolSet.sword().get());
                tag(ItemTags.PICKAXES).add(toolSet.pickaxe().get());
                tag(ItemTags.AXES).add(toolSet.axe().get());
                tag(ItemTags.SHOVELS).add(toolSet.shovel().get());
                tag(ItemTags.HOES).add(toolSet.hoe().get());
            }
            if (entry.armorSetEntry() instanceof ArmorSetEntry armorSet) {
                tag(ItemTags.HEAD_ARMOR).add(armorSet.helmet().get());
                tag(ItemTags.CHEST_ARMOR).add(armorSet.chestplate().get());
                tag(ItemTags.LEG_ARMOR).add(armorSet.leggings().get());
                tag(ItemTags.FOOT_ARMOR).add(armorSet.boots().get());
            }

            MaterialEntry material = entry.materialEntry();
            if (material == null) continue;

            String name = material.name;

            if (material.hasIngot()) {
                tag(Tags.Items.INGOTS).add(material.ingot().get());
                tag(materialTag("ingots/" + name)).add(material.ingot().get());
            }

            if (material.hasGem()) {
                tag(Tags.Items.GEMS).add(material.gem().get());
                tag(materialTag("gems/" + name)).add(material.gem().get());
            }

            if (material.hasNugget()) {
                tag(Tags.Items.NUGGETS).add(material.nugget().get());
                tag(materialTag("nuggets/" + name)).add(material.nugget().get());
            }

            if (material.hasDust()) {
                tag(Tags.Items.DUSTS).add(material.dust().get());
                tag(materialTag("dusts/" + name)).add(material.dust().get());
            }

            if (material.hasRawOre()) {
                tag(Tags.Items.RAW_MATERIALS).add(material.rawOre().get());
                tag(materialTag("raw_materials/" + name)).add(material.rawOre().get());
            }

            if (material.hasPlate()) {
                tag(materialTag("plates")).add(material.plate().get());
                tag(materialTag("plates/" + name)).add(material.plate().get());
            }

            if (material.hasOre()) {
                tag(Tags.Items.ORES).add(material.oreItem().get());
                tag(materialTag("ores/" + name)).add(material.oreItem().get());
            }

            if (material.hasBlock()) {
                tag(Tags.Items.STORAGE_BLOCKS).add(material.storageItem().get());
                tag(materialTag("storage_blocks/" + name)).add(material.storageItem().get());
            }
        }
        for (IsotopeEntry isotope : ModEntries.ISOTOPES.values()) {
            TagKey<Item> isoTag = materialTag("isotopes/" + isotope.itemId);
            isotope.variants().values().forEach(item -> tag(isoTag).add(item.get()));
        }
    }

    private static TagKey<Item> materialTag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
