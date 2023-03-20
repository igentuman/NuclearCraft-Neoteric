package igentuman.nc.setup;

import igentuman.nc.setup.materials.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;

public class NCItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
      public static HashMap<String, RegistryObject<Item>> NC_INGOTS = new HashMap<>();
      public static HashMap<String, RegistryObject<Item>> NC_CHUNKS = new HashMap<>();
      public static HashMap<String, RegistryObject<Item>> NC_NUGGETS = new HashMap<>();
      public static HashMap<String, RegistryObject<Item>> NC_PLATES = new HashMap<>();
      public static HashMap<String, RegistryObject<Item>> NC_DUSTS = new HashMap<>();
    public static TagKey<Item> PLATE_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "plates"));

      public static HashMap<String, TagKey<Item>> INGOTS_TAG = new HashMap<>();
      public static HashMap<String, TagKey<Item>> CHUNKS_TAG = new HashMap<>();
      public static HashMap<String, TagKey<Item>> NUGGETS_TAG = new HashMap<>();
      public static HashMap<String, TagKey<Item>> PLATES_TAG = new HashMap<>();
      public static HashMap<String, TagKey<Item>> DUSTS_TAG = new HashMap<>();

    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(ModSetup.ITEM_GROUP);
    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        registerChunks();
        registerNuggets();
        registerIngots();
        registerPlates();
        registerDusts();
    }

    private static void registerChunks() {
        for(String name: Chunks.registered().keySet()) {
            CHUNKS_TAG.put(name, TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "raw_materials/"+name)));
            NC_CHUNKS.put(name, ITEMS.register(name+"_chunk", () -> new Item(ITEM_PROPERTIES)));
        }
    }

    private static void registerNuggets() {
        for(String name: Nuggets.registered().keySet()) {
            NUGGETS_TAG.put(name, TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "nuggets/"+name)));
            NC_NUGGETS.put(name, ITEMS.register(name+"_nugget", () -> new Item(ITEM_PROPERTIES)));
        }
    }

    private static void registerIngots() {
        for(String name: Ingots.registered().keySet()) {
            INGOTS_TAG.put(name, TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "ingots/"+name)));
            NC_INGOTS.put(name, ITEMS.register(name+"_ingot", () -> new Item(ITEM_PROPERTIES)));
        }
    }

    private static void registerPlates() {
        for(String name: Plates.registered().keySet()) {
            PLATES_TAG.put(name, TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "plates/"+name)));
            NC_PLATES.put(name, ITEMS.register(name+"_plate", () -> new Item(ITEM_PROPERTIES)));
        }
    }

    private static void registerDusts() {
        for(String name: Dusts.registered().keySet()) {
            DUSTS_TAG.put(name, TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "dusts/"+name)));
            NC_DUSTS.put(name, ITEMS.register(name+"_dust", () -> new Item(ITEM_PROPERTIES)));
        }
    }
}
