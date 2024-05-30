package igentuman.nc.setup.registration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import java.util.HashMap;
import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.Registries.BLOCK_REGISTRY;
import static igentuman.nc.setup.registration.Registries.ITEM_REGISTRY;

public class Tags {

    public static HashMap<String, TagKey<Item>> INGOTS_TAG = new HashMap<>();
    public static HashMap<String, TagKey<Item>> CHUNKS_TAG = new HashMap<>();
    public static HashMap<String, TagKey<Item>> GEMS_TAG = new HashMap<>();
    public static HashMap<String, TagKey<Item>> NUGGETS_TAG = new HashMap<>();
    public static HashMap<String, TagKey<Item>> PLATES_TAG = new HashMap<>();
    public static HashMap<String, TagKey<Item>> DUSTS_TAG = new HashMap<>();
    public static HashMap<String, TagKey<Item>> ORE_ITEM_TAGS = new HashMap<>();
    public static HashMap<String, TagKey<Item>> BLOCK_ITEM_TAGS = new HashMap<>();
    public static HashMap<String, TagKey<Block>> ORE_TAGS = new HashMap<>();
    public static HashMap<String, TagKey<Block>> BLOCK_TAGS = new HashMap<>();
    public static HashMap<String, TagKey<Fluid>> GASES_TAG = new HashMap<>();
    public static HashMap<String, TagKey<Fluid>> LIQUIDS_TAG = new HashMap<>();
    public static TagKey<Item> PLATE_TAG = TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge", "plates"));
    public static TagKey<Item> PARTS_TAG = itemTag("parts");
    public static TagKey<Item> ISOTOPE_TAG = TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge", "isotopes"));
    public static TagKey<Item> NC_ISOTOPE_TAG = itemTag("isotopes");
    public static TagKey<Item> NC_FUEL_TAG = itemTag("reactor_fuel");
    public static TagKey<Item> NC_DEPLETED_FUEL_TAG = itemTag("reactor_fuel");
    public static TagKey<Item> NC_FUELS_TAG = itemTag("reactor_fuel");

    public static TagKey<Block> blockTag(String name) {
        return TagKey.create(BLOCK_REGISTRY, new ResourceLocation(MODID, name));
    }

    public static TagKey<Item> itemTag(String name) {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation(MODID, name));
    }

    public static void addIngotTag(String name) {
        INGOTS_TAG.put(name, forgeIngot(name));
    }

    public static void addNuggetTag(String name) {
        NUGGETS_TAG.put(name, forgeNugget(name));
    }

    public static void addPlateTag(String name) {
        PLATES_TAG.put(name, forgePlate(name));
    }

    public static void addOreTag(String name) {
        ORE_ITEM_TAGS.put(name, forgeOre(name));
    }

    public static void addDustTag(String name) {
        DUSTS_TAG.put(name, forgeDust(name));
    }

    public static void addGemTag(String name) {
        GEMS_TAG.put(name, forgeGem(name));
    }

    public static void addChunkTag(String name) {
        CHUNKS_TAG.put(name, forgeChunk(name));
    }

    public static TagKey<Item> forgeIngot(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:ingots/"+name));
    }

    public static TagKey<Item> forgeGem(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:gems/"+name));
    }

    public static TagKey<Item> forgeNugget(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:nuggets/"+name));
    }

    public static TagKey<Item> forgeBlock(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:storage_blocks/"+name));
    }

    public static TagKey<Item> forgeOre(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:ores/"+name));
    }

    public static TagKey<Item> forgeBucket(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:buckets/"+name));
    }

    public static TagKey<Item> forgeChunk(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:raw_materials/"+name));
    }

    public static TagKey<Item> forgeDust(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:dusts/"+name));
    }

    public static TagKey<Item> forgePlate(String name) {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:plates/"+name));
    }

    public static TagKey<Item> forgeDye(String name) {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:dye/"+name));
    }
}
