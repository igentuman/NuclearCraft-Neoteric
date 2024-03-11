package igentuman.nc.setup.registration;

import igentuman.nc.content.ArmorMaterials;
import igentuman.nc.content.materials.*;
import igentuman.nc.item.*;
import igentuman.nc.item.Tiers;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.util.datafix.fixes.JukeboxRecordItem;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCSounds.SOUND_MAP;
import static igentuman.nc.util.TagUtil.createItemForgeTag;
import static igentuman.nc.util.TagUtil.createItemNCTag;
import static net.minecraft.item.ItemGroup.TAB_FOOD;

public class NCItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static HashMap<String, RegistryObject<Item>> NC_FOOD = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> ALL_NC_ITEMS = new HashMap<>();

    public static HashMap<String, RegistryObject<Item>> NC_RECORDS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> NC_PARTS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> NC_SHIELDING = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> NC_ITEMS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> NC_GEMS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> NC_INGOTS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> NC_CHUNKS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> NC_NUGGETS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> NC_PLATES = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> NC_DUSTS = new HashMap<>();
    public static Tags.IOptionalNamedTag<Item> PLATE_TAG = createItemForgeTag("plates");
    public static Tags.IOptionalNamedTag<Item> PARTS_TAG = createItemNCTag( "parts");
    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.NC_ITEMS);
    public static final Item.Properties ONE_ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.NC_ITEMS).stacksTo(1);
    public static final Item.Properties PAXEL_PROPS = new Item.Properties().tab(CreativeTabs.NC_ITEMS).stacksTo(1).durability(5000);
    public static final Item.Properties HAZMAT_PROPS = new Item.Properties().tab(CreativeTabs.NC_ITEMS).stacksTo(1).durability(250);
    public static final Item.Properties TOUGH_PROPS = new Item.Properties().tab(CreativeTabs.NC_ITEMS).stacksTo(1).durability(2500);
    public static final Item.Properties HEV_PROPS = new Item.Properties().tab(CreativeTabs.NC_ITEMS).stacksTo(1).durability(5500);

    public static final RegistryObject<Item> HAZMAT_MASK =
            ITEMS.register("hazmat_mask", () -> new HazmatItem(ArmorMaterials.HAZMAT, EquipmentSlotType.HEAD, HAZMAT_PROPS));
    public static final RegistryObject<Item> HAZMAT_CHEST =
            ITEMS.register("hazmat_chest", () -> new HazmatItem(ArmorMaterials.HAZMAT, EquipmentSlotType.CHEST, HAZMAT_PROPS));
    public static final RegistryObject<Item> HAZMAT_BOOTS =
            ITEMS.register("hazmat_boots", () -> new HazmatItem(ArmorMaterials.HAZMAT, EquipmentSlotType.FEET, HAZMAT_PROPS));
    public static final RegistryObject<Item> HAZMAT_PANTS =
            ITEMS.register("hazmat_pants", () -> new HazmatItem(ArmorMaterials.HAZMAT, EquipmentSlotType.LEGS, HAZMAT_PROPS));


    public static final RegistryObject<Item> HEV_HELMET =
            ITEMS.register("hev_helmet", () -> new HEVItem(ArmorMaterials.HEV, EquipmentSlotType.HEAD, HEV_PROPS));
    public static final RegistryObject<Item> HEV_CHEST =
            ITEMS.register("hev_chest", () -> new HEVItem(ArmorMaterials.HEV, EquipmentSlotType.CHEST, HEV_PROPS));
    public static final RegistryObject<Item> HEV_BOOTS =
            ITEMS.register("hev_boots", () -> new HEVItem(ArmorMaterials.HEV, EquipmentSlotType.FEET, HEV_PROPS));
    public static final RegistryObject<Item> HEV_PANTS =
            ITEMS.register("hev_pants", () -> new HEVItem(ArmorMaterials.HEV, EquipmentSlotType.LEGS, HEV_PROPS));

    public static final RegistryObject<Item> TOUGH_HELMET =
            ITEMS.register("tough_helmet", () -> new ArmorItem(ArmorMaterials.TOUGH, EquipmentSlotType.HEAD, TOUGH_PROPS));
    public static final RegistryObject<Item> TOUGH_CHEST =
            ITEMS.register("tough_chest", () -> new ArmorItem(ArmorMaterials.TOUGH, EquipmentSlotType.CHEST, TOUGH_PROPS));
    public static final RegistryObject<Item> TOUGH_BOOTS =
            ITEMS.register("tough_boots", () -> new ArmorItem(ArmorMaterials.TOUGH, EquipmentSlotType.FEET, TOUGH_PROPS));
    public static final RegistryObject<Item> TOUGH_PANTS =
            ITEMS.register("tough_pants", () -> new ArmorItem(ArmorMaterials.TOUGH, EquipmentSlotType.LEGS, TOUGH_PROPS));

    public static final RegistryObject<Item> GEIGER_COUNTER = ITEMS.register("geiger_counter", () -> new GeigerCounterItem(ONE_ITEM_PROPERTIES));
    public static final RegistryObject<Item> LITHIUM_ION_CELL = ITEMS.register("lithium_ion_cell", () -> new BatteryItem(ONE_ITEM_PROPERTIES));
    public static final RegistryObject<Item> SPAXELHOE_TOUGH = ITEMS.register("spaxelhoe_tough", () -> new PaxelItem(7, 2, Tiers.TOUGH, PAXEL_PROPS));
    public static final RegistryObject<Item> SPAXELHOE_THORIUM = ITEMS.register("spaxelhoe_thorium", () -> new PaxelItem(3, 1, Tiers.THORIUM, PAXEL_PROPS));
    public static final RegistryObject<Item> QNP = ITEMS.register("qnp", () -> new QNP(Tiers.QNP, 11, 2F, ONE_ITEM_PROPERTIES));
    public static final RegistryObject<Item> MULTITOOL = ITEMS.register("multitool", () -> new MultitoolItem(ONE_ITEM_PROPERTIES));
    public static HashMap<String, Tags.IOptionalNamedTag<Item>> INGOTS_TAG = new HashMap<>();
    public static HashMap<String, Tags.IOptionalNamedTag<Item>> CHUNKS_TAG = new HashMap<>();
    public static HashMap<String, Tags.IOptionalNamedTag<Item>> GEMS_TAG = new HashMap<>();
    public static HashMap<String, Tags.IOptionalNamedTag<Item>> NUGGETS_TAG = new HashMap<>();
    public static HashMap<String, Tags.IOptionalNamedTag<Item>> PLATES_TAG = new HashMap<>();
    public static HashMap<String, Tags.IOptionalNamedTag<Item>> DUSTS_TAG = new HashMap<>();

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        registerChunks();
        registerNuggets();
        registerIngots();
        registerPlates();
        registerDusts();
        registerGems();
        registerParts();
        registerItems();
        registerFood();
        registerRecords();
        registerShielding();
    }



    private static void registerRecords() {
        List<String> items = Arrays.asList(
                "end_of_the_world",
                "hyperspace",
                "money_for_nothing",
                "wanderer"
        );
        for(String name: items) {
          //  NC_RECORDS.put(name, ITEMS.register(name, () -> new Item( SOUND_MAP.get(name), ITEM_PROPERTIES)));
            //ALL_NC_ITEMS.put(name, NC_RECORDS.get(name));
        }
    }

    private static void registerFood() {
        List<String> items = Arrays.asList(
                "rad_x",
                "radaway",
                "radaway_slow",
                "marshmallow",
                "milk_chocolate",
                "dark_chocolate",
                "graham_cracker",
                "smore",
                "moresmore",
                "foursmore",
                "dominos",
                "evenmoresmore"

        );
        int i = -18;
        for(String name: items) {
            i+=4;
            int finalI = Math.max(i, 1);

            NC_FOOD.put(name, ITEMS.register(name, () -> new Item(new Item.Properties().tab(TAB_FOOD).food(
                    new Food.Builder().nutrition(finalI)
                            .saturationMod(finalI).build()

            ))));
            ALL_NC_ITEMS.put(name, NC_FOOD.get(name));
        }
    }

    private static void registerItems() {
        List<String> items = Arrays.asList(
                "cocoa_butter",
                "cocoa_solids",
                "roasted_cocoa_beans",
                "flour",
                "gelatin",
                "ground_cocoa_nibs",
                "salt",
                "sawdust",
                "dosimeter",
                "upgrade_energy",
                "upgrade_speed",
                "water_collector",
                "lava_collector",
                "compact_water_collector",
                "dense_water_collector",
                "helium_collector",
                "compact_helium_collector",
                "dense_helium_collector",
                "nitrogen_collector",
                "compact_nitrogen_collector",
                "dense_nitrogen_collector",
                "unsweetened_chocolate"
        );
        for(String name: items) {
            if(name.equals("dosimeter")) {
                NC_ITEMS.put(name, ITEMS.register(name, () -> new DosimiterItem(ONE_ITEM_PROPERTIES)));
            } else {
                NC_ITEMS.put(name, ITEMS.register(name, () -> new Item(ITEM_PROPERTIES)));
            }
            ALL_NC_ITEMS.put(name, NC_ITEMS.get(name));
        }
    }

    private static void registerShielding() {
        List<String> parts = Arrays.asList(
                "light",
                "medium",
                "heavy",
                "dps"
        );
        int i = 1;
        for(String name: parts) {
            int finalI = i;
            NC_SHIELDING.put(name, ITEMS.register(name, () -> new RadShieldingItem(ITEM_PROPERTIES, finalI)));
            i+=2;
            ALL_NC_ITEMS.put(name, NC_SHIELDING.get(name));
        }
    }


    private static void registerParts() {
        List<String> parts = Arrays.asList(
                "actuator",
                "research_paper",
                "basic_electric_circuit",
                "bioplastic",
                "chassis",
                "empty_frame",
                "empty_sink",
                "motor",
                "plate_advanced",
                "plate_basic",
                "plate_du",
                "plate_elite",
                "plate_extreme",
                "servo",
                "sic_fiber",
                "steel_frame",
                "coil_copper",
                "coil_magnesium_diboride",
                "coil_bscco"
        );
        for(String name: parts) {
            if(name.equals("research_paper")) {
                NC_PARTS.put(name, ITEMS.register(name, () -> new ResearchPaperItem(ONE_ITEM_PROPERTIES)));
            } else {
                NC_PARTS.put(name, ITEMS.register(name, () -> new Item(ITEM_PROPERTIES)));
            }
            ALL_NC_ITEMS.put(name, NC_PARTS.get(name));
        }
    }

    private static void registerGems() {
        for(String name: Gems.registered().keySet()) {
            GEMS_TAG.put(name, createItemForgeTag("gems/"+name));
            NC_GEMS.put(name, ITEMS.register(name+"_gem", () -> new Item(ITEM_PROPERTIES)));
            ALL_NC_ITEMS.put(name+"_gem", NC_GEMS.get(name));
        }
    }

    private static void registerChunks() {
        for(String name: Chunks.registered().keySet()) {
            CHUNKS_TAG.put(name, createItemForgeTag("raw_materials/"+name));
            NC_CHUNKS.put(name, ITEMS.register(name+"_chunk", () -> new Item(ITEM_PROPERTIES)));
            ALL_NC_ITEMS.put(name+"_chunk", NC_CHUNKS.get(name));
        }
    }

    private static void registerNuggets() {
        for(String name: Nuggets.registered().keySet()) {
            NUGGETS_TAG.put(name, createItemForgeTag("nuggets/"+name));
            NC_NUGGETS.put(name, ITEMS.register(name+"_nugget", () -> new Item(ITEM_PROPERTIES)));
            ALL_NC_ITEMS.put(name+"_nugget", NC_NUGGETS.get(name));
        }
    }

    private static void registerIngots() {
        for(String name: Ingots.registered().keySet()) {
            INGOTS_TAG.put(name, createItemForgeTag("ingots/"+name));
            NC_INGOTS.put(name, ITEMS.register(name+"_ingot", () -> new Item(ITEM_PROPERTIES)));
            ALL_NC_ITEMS.put(name+"_ingot", NC_INGOTS.get(name));
        }
    }

    private static void registerPlates() {
        for(String name: Plates.registered().keySet()) {
            PLATES_TAG.put(name,createItemForgeTag("plates/"+name));
            NC_PLATES.put(name, ITEMS.register(name+"_plate", () -> new Item(ITEM_PROPERTIES)));
            ALL_NC_ITEMS.put(name+"_plate", NC_PLATES.get(name));
        }
    }

    private static void registerDusts() {
        for(String name: Dusts.registered().keySet()) {
            DUSTS_TAG.put(name, createItemForgeTag("dusts/"+name));
            NC_DUSTS.put(name, ITEMS.register(name+"_dust", () -> new Item(ITEM_PROPERTIES)));
            ALL_NC_ITEMS.put(name+"_dust", NC_DUSTS.get(name));
        }
    }
}
