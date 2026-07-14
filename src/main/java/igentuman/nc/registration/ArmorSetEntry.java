package igentuman.nc.registration;

import net.minecraft.core.Holder;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;
import org.apache.commons.lang3.function.TriFunction;

import static igentuman.nc.setup.Registers.ITEMS;

/** Registers and holds the four armor pieces (helmet, chestplate, leggings, boots) for one armor material. */
public class ArmorSetEntry {

    public static final TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> DEFAULT_FACTORY = ArmorItem::new;

    public final String name;
    private DeferredItem<Item> helmet;
    private DeferredItem<Item> chestplate;
    private DeferredItem<Item> leggings;
    private DeferredItem<Item> boots;

    private ArmorSetEntry(String name) {
        this.name = name;
    }

    public static ArmorSetEntry build(String name, Holder<ArmorMaterial> material) {
        return build(name, material, 15);
    }

    public static ArmorSetEntry build(String name, ArmorMaterialEntry material) {
        return build(name, material.holder(), material.durabilityMultiplier());
    }

    public static ArmorSetEntry build(String name, Holder<ArmorMaterial> material, int durabilityMultiplier) {
        return build(name, material, durabilityMultiplier, DEFAULT_FACTORY, DEFAULT_FACTORY, DEFAULT_FACTORY, DEFAULT_FACTORY);
    }

    public static ArmorSetEntry build(String name, ArmorMaterialEntry material,
                                      TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> helmetFactory,
                                      TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> chestplateFactory,
                                      TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> leggingsFactory,
                                      TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> bootsFactory) {
        return build(name, material.holder(), material.durabilityMultiplier(),
                helmetFactory, chestplateFactory, leggingsFactory, bootsFactory);
    }

    @SuppressWarnings("unchecked")
    public static ArmorSetEntry build(String name, Holder<ArmorMaterial> material, int durabilityMultiplier,
                                      TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> helmetFactory,
                                      TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> chestplateFactory,
                                      TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> leggingsFactory,
                                      TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> bootsFactory) {
        ArmorSetEntry e = new ArmorSetEntry(name);
        TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> hf = helmetFactory     != null ? helmetFactory     : DEFAULT_FACTORY;
        TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> cf = chestplateFactory != null ? chestplateFactory : DEFAULT_FACTORY;
        TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> lf = leggingsFactory   != null ? leggingsFactory   : DEFAULT_FACTORY;
        TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> bf = bootsFactory      != null ? bootsFactory      : DEFAULT_FACTORY;
        e.helmet     = (DeferredItem<Item>) ITEMS.register(name + "_helmet",     () -> hf.apply(material, ArmorItem.Type.HELMET,     props(ArmorItem.Type.HELMET,     durabilityMultiplier)));
        e.chestplate = (DeferredItem<Item>) ITEMS.register(name + "_chestplate", () -> cf.apply(material, ArmorItem.Type.CHESTPLATE, props(ArmorItem.Type.CHESTPLATE, durabilityMultiplier)));
        e.leggings   = (DeferredItem<Item>) ITEMS.register(name + "_leggings",   () -> lf.apply(material, ArmorItem.Type.LEGGINGS,   props(ArmorItem.Type.LEGGINGS,   durabilityMultiplier)));
        e.boots      = (DeferredItem<Item>) ITEMS.register(name + "_boots",      () -> bf.apply(material, ArmorItem.Type.BOOTS,      props(ArmorItem.Type.BOOTS,      durabilityMultiplier)));
        return e;
    }

    private static Item.Properties props(ArmorItem.Type type, int durabilityMultiplier) {
        return new Item.Properties().durability(type.getDurability(durabilityMultiplier));
    }

    public DeferredItem<Item> helmet()     { return helmet; }
    public DeferredItem<Item> chestplate() { return chestplate; }
    public DeferredItem<Item> leggings()   { return leggings; }
    public DeferredItem<Item> boots()      { return boots; }
}
