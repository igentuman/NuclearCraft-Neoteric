package igentuman.nc.setup.registration;

import igentuman.nc.setup.ModSetup;
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

    public static final RegistryObject<Item> HAZMAT_MASK = ITEMS.register("hazmat_mask", () -> new Item(ITEM_PROPERTIES));
    public static final RegistryObject<Item> HAZMAT_CHEST = ITEMS.register("hazmat_chest", () -> new Item(ITEM_PROPERTIES));
    public static final RegistryObject<Item> HAZMAT_BOOTS = ITEMS.register("hazmat_boots", () -> new Item(ITEM_PROPERTIES));
    public static final RegistryObject<Item> HAZMAT_PANTS = ITEMS.register("hazmat_pants", () -> new Item(ITEM_PROPERTIES));


    public static final RegistryObject<Item> HEV_HELMET = ITEMS.register("hev_helmet", () -> new Item(ITEM_PROPERTIES));
    public static final RegistryObject<Item> HEV_CHEST = ITEMS.register("hev_chest", () -> new Item(ITEM_PROPERTIES));
    public static final RegistryObject<Item> HEV_BOOTS = ITEMS.register("hev_boots", () -> new Item(ITEM_PROPERTIES));
    public static final RegistryObject<Item> HEV_PANTS = ITEMS.register("hev_pants", () -> new Item(ITEM_PROPERTIES));

    public static final RegistryObject<Item> TOUGH_HELMET = ITEMS.register("tough_helmet", () -> new Item(ITEM_PROPERTIES));
    public static final RegistryObject<Item> TOUGH_CHEST = ITEMS.register("tough_chest", () -> new Item(ITEM_PROPERTIES));
    public static final RegistryObject<Item> TOUGH_BOOTS = ITEMS.register("tough_boots", () -> new Item(ITEM_PROPERTIES));
    public static final RegistryObject<Item> TOUGH_PANTS = ITEMS.register("tough_pants", () -> new Item(ITEM_PROPERTIES));

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
    }
}
