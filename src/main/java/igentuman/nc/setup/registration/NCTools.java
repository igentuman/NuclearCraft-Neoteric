package igentuman.nc.setup.registration;

import igentuman.nc.setup.ModSetup;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.NuclearCraft.MODID;

public class NCTools {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(ModSetup.ITEM_GROUP);

    public static final RegistryObject<Item> GEIGER_COUNTER = ITEMS.register("geiger_counter", () -> new Item(ITEM_PROPERTIES));
    public static final RegistryObject<Item> MULTITOOL = ITEMS.register("multitool", () -> new Item(ITEM_PROPERTIES));
    public static final RegistryObject<Item> QNP = ITEMS.register("qnp", () -> new Item(ITEM_PROPERTIES));
    public static final RegistryObject<Item> SPAXELHOE_TOUGH = ITEMS.register("spaxelhoe_tough", () -> new Item(ITEM_PROPERTIES));

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
    }
}
