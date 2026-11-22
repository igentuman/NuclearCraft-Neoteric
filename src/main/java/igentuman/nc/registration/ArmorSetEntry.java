package igentuman.nc.registration;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.registries.DeferredItem;
import org.apache.commons.lang3.function.TriFunction;

import static igentuman.nc.setup.Registers.ITEMS;

/** Registers and holds the four armor pieces (helmet, chestplate, leggings, boots) for one armor material. */
public class ArmorSetEntry {

    public static final TriFunction<ArmorMaterial, ArmorType, Item.Properties, ? extends Item> DEFAULT_FACTORY = (material, type, properties) -> new Item(properties.humanoidArmor(material, type));

    public final String name;
    private DeferredItem<Item> helmet;
    private DeferredItem<Item> chestplate;
    private DeferredItem<Item> leggings;
    private DeferredItem<Item> boots;

    private ArmorSetEntry(String name) {
        this.name = name;
    }

    public static ArmorSetEntry build(String name, ArmorMaterial material) {
        return build(name, material, DEFAULT_FACTORY, DEFAULT_FACTORY, DEFAULT_FACTORY, DEFAULT_FACTORY);
    }

    public static ArmorSetEntry build(String name, ArmorMaterialEntry material) {
        return build(name, material.material());
    }

    public static ArmorSetEntry build(String name, ArmorMaterialEntry material,
                                      TriFunction<ArmorMaterial, ArmorType, Item.Properties, ? extends Item> helmetFactory,
                                      TriFunction<ArmorMaterial, ArmorType, Item.Properties, ? extends Item> chestplateFactory,
                                      TriFunction<ArmorMaterial, ArmorType, Item.Properties, ? extends Item> leggingsFactory,
                                      TriFunction<ArmorMaterial, ArmorType, Item.Properties, ? extends Item> bootsFactory) {
        return build(name, material.material(),
                helmetFactory, chestplateFactory, leggingsFactory, bootsFactory);
    }

    public static ArmorSetEntry build(String name, ArmorMaterial material,
                                      TriFunction<ArmorMaterial, ArmorType, Item.Properties, ? extends Item> helmetFactory,
                                      TriFunction<ArmorMaterial, ArmorType, Item.Properties, ? extends Item> chestplateFactory,
                                      TriFunction<ArmorMaterial, ArmorType, Item.Properties, ? extends Item> leggingsFactory,
                                      TriFunction<ArmorMaterial, ArmorType, Item.Properties, ? extends Item> bootsFactory) {
        ArmorSetEntry e = new ArmorSetEntry(name);
        TriFunction<ArmorMaterial, ArmorType, Item.Properties, ? extends Item> hf = helmetFactory     != null ? helmetFactory     : DEFAULT_FACTORY;
        TriFunction<ArmorMaterial, ArmorType, Item.Properties, ? extends Item> cf = chestplateFactory != null ? chestplateFactory : DEFAULT_FACTORY;
        TriFunction<ArmorMaterial, ArmorType, Item.Properties, ? extends Item> lf = leggingsFactory   != null ? leggingsFactory   : DEFAULT_FACTORY;
        TriFunction<ArmorMaterial, ArmorType, Item.Properties, ? extends Item> bf = bootsFactory      != null ? bootsFactory      : DEFAULT_FACTORY;
        e.helmet     = ITEMS.<Item>registerItem(name + "_helmet", properties -> hf.apply(material, ArmorType.HELMET, properties));
        e.chestplate = ITEMS.<Item>registerItem(name + "_chestplate", properties -> cf.apply(material, ArmorType.CHESTPLATE, properties));
        e.leggings   = ITEMS.<Item>registerItem(name + "_leggings", properties -> lf.apply(material, ArmorType.LEGGINGS, properties));
        e.boots      = ITEMS.<Item>registerItem(name + "_boots", properties -> bf.apply(material, ArmorType.BOOTS, properties));
        return e;
    }

    public DeferredItem<Item> helmet()     { return helmet; }
    public DeferredItem<Item> chestplate() { return chestplate; }
    public DeferredItem<Item> leggings()   { return leggings; }
    public DeferredItem<Item> boots()      { return boots; }
}
