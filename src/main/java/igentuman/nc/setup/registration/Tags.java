package igentuman.nc.setup.registration;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.neoforgeRl;
import static igentuman.nc.NuclearCraft.resourceLoc;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.Registries.BLOCK_REGISTRY;
import static igentuman.nc.setup.registration.Registries.ITEM_REGISTRY;

public class Tags {

    public final static HashMap<String, TagKey<Item>> INGOTS_TAG = new HashMap<>();
    public final static HashMap<String, TagKey<Item>> CHUNKS_TAG = new HashMap<>();
    public final static HashMap<String, TagKey<Item>> GEMS_TAG = new HashMap<>();
    public final static HashMap<String, TagKey<Item>> NUGGETS_TAG = new HashMap<>();
    public final static HashMap<String, TagKey<Item>> PLATES_TAG = new HashMap<>();
    public final static HashMap<String, TagKey<Item>> DUSTS_TAG = new HashMap<>();
    public final static HashMap<String, TagKey<Item>> ION_SOURCE_TAG = new HashMap<>();
    public final static HashMap<String, TagKey<Item>> ORE_ITEM_TAGS = new HashMap<>();
    public final static HashMap<String, TagKey<Item>> BLOCK_ITEM_TAGS = new HashMap<>();
    public final static HashMap<String, TagKey<Block>> ORE_TAGS = new HashMap<>();
    public final static HashMap<String, TagKey<Block>> BLOCK_TAGS = new HashMap<>();
    public final static HashMap<String, TagKey<Fluid>> GASES_TAG = new HashMap<>();
    public final static HashMap<String, TagKey<Fluid>> LIQUIDS_TAG = new HashMap<>();
    public final static TagKey<Item> PLATE_TAG = TagKey.create(ITEM_REGISTRY, resourceLoc("plates"));
    public final static TagKey<Item> PARTS_TAG = itemTag("parts");
    public final static HashMap<String, TagKey<Item>> NC_ISOTOPE_TAG = new HashMap<>();
    public final static HashMap<String, TagKey<Item>> NC_WASTE_TAG = new HashMap<>();
    public final static HashMap<String, TagKey<Item>> REACTOR_FUEL_TAG = new HashMap<>();
    public final static HashMap<String, TagKey<Item>> REACTOR_DEPLETED_FUEL_TAG = new HashMap<>();

    public static TagKey<Block> blockTag(String name) {
        return TagKey.create(BLOCK_REGISTRY, rl(name));
    }

    public static TagKey<Item> itemTag(String name) {
        return TagKey.create(ITEM_REGISTRY, rl(name));
    }

    public static void addIngotTag(String name) {
        if(name.equals("aluminium")) {
            INGOTS_TAG.put("aluminum", forgeIngot("aluminum"));
        }
        INGOTS_TAG.put(name, forgeIngot(name));
    }

    public static void addNuggetTag(String name) {
        if(name.equals("aluminium")) {
            NUGGETS_TAG.put("aluminum", forgeNugget("aluminum"));
        }
        NUGGETS_TAG.put(name, forgeNugget(name));
    }

    public static void addPlateTag(String name) {
        if(name.equals("aluminium")) {
            PLATES_TAG.put("aluminum", forgePlate("aluminum"));
        }
        PLATES_TAG.put(name, forgePlate(name));
    }

    public static void addOreTag(String name) {
        if(name.equals("aluminium")) {
            ORE_ITEM_TAGS.put("aluminum", forgeOre("aluminum"));
        }
        ORE_ITEM_TAGS.put(name, forgeOre(name));
    }

    public static void addDustTag(String name) {
        if(name.equals("aluminium")) {
            DUSTS_TAG.put("aluminum", forgeDust("aluminum"));
        }
        DUSTS_TAG.put(name, forgeDust(name));
    }

    public static void addGemTag(String name) {
        GEMS_TAG.put(name, forgeGem(name));
    }

    public static void addChunkTag(String name) {
        if(name.equals("aluminium")) {
            CHUNKS_TAG.put("aluminum", forgeChunk("aluminum"));
        }
        CHUNKS_TAG.put(name, forgeChunk(name));
    }

    public static TagKey<Item> forgeIngot(String name)
    {
        return TagKey.create(ITEM_REGISTRY, resourceLoc("ingots/"+name));
    }

    public static TagKey<Item> forgeGem(String name)
    {
        return TagKey.create(ITEM_REGISTRY, resourceLoc("gems/"+name));
    }

    public static TagKey<Item> forgeNugget(String name)
    {
        return TagKey.create(ITEM_REGISTRY, resourceLoc("nuggets/"+name));
    }

    public static TagKey<Item> forgeBlock(String name)
    {
        return TagKey.create(ITEM_REGISTRY, resourceLoc("storage_blocks/"+name));
    }

    public static TagKey<Item> forgeOre(String name)
    {
        return TagKey.create(ITEM_REGISTRY, resourceLoc("ores/"+name));
    }

    public static TagKey<Item> forgeBucket(String name)
    {
        return TagKey.create(ITEM_REGISTRY, resourceLoc("buckets/"+name));
    }

    public static TagKey<Item> forgeChunk(String name)
    {
        return TagKey.create(ITEM_REGISTRY, resourceLoc("raw_materials/"+name));
    }

    public static TagKey<Item> forgeIonSource(String name)
    {
        return TagKey.create(ITEM_REGISTRY, resourceLoc("ion_source/"+name));
    }

    public static TagKey<Item> forgeDust(String name)
    {
        return TagKey.create(ITEM_REGISTRY, resourceLoc("dusts/"+name));
    }

    public static TagKey<Item> forgePlate(String name) {
        return TagKey.create(ITEM_REGISTRY, resourceLoc("plates/"+name));
    }

    public static TagKey<Item> forgeDye(String name) {
        return TagKey.create(ITEM_REGISTRY, resourceLoc("dye/"+name));
    }
}
