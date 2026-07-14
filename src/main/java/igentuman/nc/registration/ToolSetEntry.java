package igentuman.nc.registration;

import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.BiFunction;

import static igentuman.nc.setup.Registers.ITEMS;

/** Registers and holds the five tools (sword, pickaxe, axe, shovel, hoe) for one tool tier. */
public class ToolSetEntry {

    public static final BiFunction<Tier, Item.Properties, ? extends Item> DEFAULT_SWORD   = SwordItem::new;
    public static final BiFunction<Tier, Item.Properties, ? extends Item> DEFAULT_PICKAXE = PickaxeItem::new;
    public static final BiFunction<Tier, Item.Properties, ? extends Item> DEFAULT_AXE     = AxeItem::new;
    public static final BiFunction<Tier, Item.Properties, ? extends Item> DEFAULT_SHOVEL  = ShovelItem::new;
    public static final BiFunction<Tier, Item.Properties, ? extends Item> DEFAULT_HOE     = HoeItem::new;

    public final String name;
    private DeferredItem<Item> sword;
    private DeferredItem<Item> pickaxe;
    private DeferredItem<Item> axe;
    private DeferredItem<Item> shovel;
    private DeferredItem<Item> hoe;

    private ToolSetEntry(String name) {
        this.name = name;
    }

    public static ToolSetEntry build(String name, Tier tier) {
        return build(name, tier, DEFAULT_SWORD, DEFAULT_PICKAXE, DEFAULT_AXE, DEFAULT_SHOVEL, DEFAULT_HOE);
    }

    @SuppressWarnings("unchecked")
    public static ToolSetEntry build(String name, Tier tier,
                                     BiFunction<Tier, Item.Properties, ? extends Item> swordFactory,
                                     BiFunction<Tier, Item.Properties, ? extends Item> pickaxeFactory,
                                     BiFunction<Tier, Item.Properties, ? extends Item> axeFactory,
                                     BiFunction<Tier, Item.Properties, ? extends Item> shovelFactory,
                                     BiFunction<Tier, Item.Properties, ? extends Item> hoeFactory) {
        ToolSetEntry e = new ToolSetEntry(name);
        BiFunction<Tier, Item.Properties, ? extends Item> sf = swordFactory   != null ? swordFactory   : DEFAULT_SWORD;
        BiFunction<Tier, Item.Properties, ? extends Item> pf = pickaxeFactory != null ? pickaxeFactory : DEFAULT_PICKAXE;
        BiFunction<Tier, Item.Properties, ? extends Item> af = axeFactory     != null ? axeFactory     : DEFAULT_AXE;
        BiFunction<Tier, Item.Properties, ? extends Item> shf = shovelFactory != null ? shovelFactory  : DEFAULT_SHOVEL;
        BiFunction<Tier, Item.Properties, ? extends Item> hf = hoeFactory     != null ? hoeFactory     : DEFAULT_HOE;
        e.sword   = (DeferredItem<Item>) ITEMS.register(name + "_sword",   () -> sf.apply(tier, new Item.Properties()));
        e.pickaxe = (DeferredItem<Item>) ITEMS.register(name + "_pickaxe", () -> pf.apply(tier, new Item.Properties()));
        e.axe     = (DeferredItem<Item>) ITEMS.register(name + "_axe",     () -> af.apply(tier, new Item.Properties()));
        e.shovel  = (DeferredItem<Item>) ITEMS.register(name + "_shovel",  () -> shf.apply(tier, new Item.Properties()));
        e.hoe     = (DeferredItem<Item>) ITEMS.register(name + "_hoe",     () -> hf.apply(tier, new Item.Properties()));
        return e;
    }

    public DeferredItem<Item> sword()   { return sword; }
    public DeferredItem<Item> pickaxe() { return pickaxe; }
    public DeferredItem<Item> axe()     { return axe; }
    public DeferredItem<Item> shovel()  { return shovel; }
    public DeferredItem<Item> hoe()     { return hoe; }
}
