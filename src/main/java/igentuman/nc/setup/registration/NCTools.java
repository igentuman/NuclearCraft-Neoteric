package igentuman.nc.setup.registration;

import igentuman.nc.item.BatteryItem;
import igentuman.nc.item.GeigerCounterItem;
import igentuman.nc.item.QNP;
import igentuman.nc.item.Tiers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.NuclearCraft.MODID;

public class NCTools {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.NC_ITEMS);
    public static final RegistryObject<Item> GEIGER_COUNTER = ITEMS.register("geiger_counter", () -> new GeigerCounterItem(ITEM_PROPERTIES));
    public static final RegistryObject<Item> LITHIUM_ION_CELL = ITEMS.register("lithium_ion_cell", () -> new BatteryItem(ITEM_PROPERTIES));
    public static final RegistryObject<Item> SPAXELHOE_TOUGH = ITEMS.register("spaxelhoe_tough", () -> new PickaxeItem(Tiers.TOUGH, 7, 2F, ITEM_PROPERTIES));
    public static final RegistryObject<Item> QNP = ITEMS.register("qnp", () -> new QNP(Tiers.QNP, 11, 2F, ITEM_PROPERTIES));
    public static final RegistryObject<Item> MULTITOOL = ITEMS.register("multitool", () -> new Item(ITEM_PROPERTIES));

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
    }
}
