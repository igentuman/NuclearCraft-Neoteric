package igentuman.nc.setup.registration;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.NuclearCraft.MODID;

public class NCArmor {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.NC_ITEMS);

    public static final RegistryObject<Item> HAZMAT_MASK =
            ITEMS.register("hazmat_mask", () -> new ArmorItem(ArmorMaterials.GOLD, EquipmentSlot.HEAD, ITEM_PROPERTIES));
    public static final RegistryObject<Item> HAZMAT_CHEST =
            ITEMS.register("hazmat_chest", () -> new ArmorItem(ArmorMaterials.GOLD, EquipmentSlot.CHEST, ITEM_PROPERTIES));
    public static final RegistryObject<Item> HAZMAT_BOOTS =
            ITEMS.register("hazmat_boots", () -> new ArmorItem(ArmorMaterials.GOLD, EquipmentSlot.FEET, ITEM_PROPERTIES));
    public static final RegistryObject<Item> HAZMAT_PANTS =
            ITEMS.register("hazmat_pants", () -> new ArmorItem(ArmorMaterials.GOLD, EquipmentSlot.LEGS, ITEM_PROPERTIES));


    public static final RegistryObject<Item> HEV_HELMET =
            ITEMS.register("hev_helmet", () -> new ArmorItem(ArmorMaterials.NETHERITE, EquipmentSlot.HEAD, ITEM_PROPERTIES));
    public static final RegistryObject<Item> HEV_CHEST =
            ITEMS.register("hev_chest", () -> new ArmorItem(ArmorMaterials.NETHERITE, EquipmentSlot.CHEST, ITEM_PROPERTIES));
    public static final RegistryObject<Item> HEV_BOOTS =
            ITEMS.register("hev_boots", () -> new ArmorItem(ArmorMaterials.NETHERITE, EquipmentSlot.FEET, ITEM_PROPERTIES));
    public static final RegistryObject<Item> HEV_PANTS =
            ITEMS.register("hev_pants", () -> new ArmorItem(ArmorMaterials.NETHERITE, EquipmentSlot.LEGS, ITEM_PROPERTIES));

    public static final RegistryObject<Item> TOUGH_HELMET =
            ITEMS.register("tough_helmet", () -> new ArmorItem(ArmorMaterials.NETHERITE, EquipmentSlot.HEAD, ITEM_PROPERTIES));
    public static final RegistryObject<Item> TOUGH_CHEST =
            ITEMS.register("tough_chest", () -> new ArmorItem(ArmorMaterials.NETHERITE, EquipmentSlot.CHEST, ITEM_PROPERTIES));
    public static final RegistryObject<Item> TOUGH_BOOTS =
            ITEMS.register("tough_boots", () -> new ArmorItem(ArmorMaterials.NETHERITE, EquipmentSlot.FEET, ITEM_PROPERTIES));
    public static final RegistryObject<Item> TOUGH_PANTS =
            ITEMS.register("tough_pants", () -> new ArmorItem(ArmorMaterials.NETHERITE, EquipmentSlot.LEGS, ITEM_PROPERTIES));

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
    }
}
