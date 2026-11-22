package igentuman.nc.registration;

import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.BiFunction;

import static igentuman.nc.setup.Registers.ITEMS;

/** Registers and holds the five tools (sword, pickaxe, axe, shovel, hoe) for one tool tier. */
public class ToolSetEntry {

    // The old default factories supplied no combat attributes; retain zero bonuses.
    public static final BiFunction<ToolMaterial, Item.Properties, ? extends Item> DEFAULT_SWORD   = (material, properties) -> new Item(properties.sword(material, -material.attackDamageBonus(), 0.0F));
    public static final BiFunction<ToolMaterial, Item.Properties, ? extends Item> DEFAULT_PICKAXE = (material, properties) -> new Item(properties.pickaxe(material, -material.attackDamageBonus(), 0.0F));
    public static final BiFunction<ToolMaterial, Item.Properties, ? extends Item> DEFAULT_AXE     = (material, properties) -> new AxeItem(material, -material.attackDamageBonus(), 0.0F, properties);
    public static final BiFunction<ToolMaterial, Item.Properties, ? extends Item> DEFAULT_SHOVEL  = (material, properties) -> new ShovelItem(material, -material.attackDamageBonus(), 0.0F, properties);
    public static final BiFunction<ToolMaterial, Item.Properties, ? extends Item> DEFAULT_HOE     = (material, properties) -> new HoeItem(material, -material.attackDamageBonus(), 0.0F, properties);

    public final String name;
    private DeferredItem<Item> sword;
    private DeferredItem<Item> pickaxe;
    private DeferredItem<Item> axe;
    private DeferredItem<Item> shovel;
    private DeferredItem<Item> hoe;

    private ToolSetEntry(String name) {
        this.name = name;
    }

    public static ToolSetEntry build(String name, ToolMaterial tier) {
        return build(name, tier, DEFAULT_SWORD, DEFAULT_PICKAXE, DEFAULT_AXE, DEFAULT_SHOVEL, DEFAULT_HOE);
    }

    public static ToolSetEntry build(String name, ToolMaterial tier,
                                     BiFunction<ToolMaterial, Item.Properties, ? extends Item> swordFactory,
                                     BiFunction<ToolMaterial, Item.Properties, ? extends Item> pickaxeFactory,
                                     BiFunction<ToolMaterial, Item.Properties, ? extends Item> axeFactory,
                                     BiFunction<ToolMaterial, Item.Properties, ? extends Item> shovelFactory,
                                     BiFunction<ToolMaterial, Item.Properties, ? extends Item> hoeFactory) {
        ToolSetEntry e = new ToolSetEntry(name);
        BiFunction<ToolMaterial, Item.Properties, ? extends Item> sf = swordFactory   != null ? swordFactory   : DEFAULT_SWORD;
        BiFunction<ToolMaterial, Item.Properties, ? extends Item> pf = pickaxeFactory != null ? pickaxeFactory : DEFAULT_PICKAXE;
        BiFunction<ToolMaterial, Item.Properties, ? extends Item> af = axeFactory     != null ? axeFactory     : DEFAULT_AXE;
        BiFunction<ToolMaterial, Item.Properties, ? extends Item> shf = shovelFactory != null ? shovelFactory  : DEFAULT_SHOVEL;
        BiFunction<ToolMaterial, Item.Properties, ? extends Item> hf = hoeFactory     != null ? hoeFactory     : DEFAULT_HOE;
        e.sword   = ITEMS.<Item>registerItem(name + "_sword", props -> sf.apply(tier, props));
        e.pickaxe = ITEMS.<Item>registerItem(name + "_pickaxe", props -> pf.apply(tier, props));
        e.axe     = ITEMS.<Item>registerItem(name + "_axe", props -> af.apply(tier, props));
        e.shovel  = ITEMS.<Item>registerItem(name + "_shovel", props -> shf.apply(tier, props));
        e.hoe     = ITEMS.<Item>registerItem(name + "_hoe", props -> hf.apply(tier, props));
        return e;
    }

    public DeferredItem<Item> sword()   { return sword; }
    public DeferredItem<Item> pickaxe() { return pickaxe; }
    public DeferredItem<Item> axe()     { return axe; }
    public DeferredItem<Item> shovel()  { return shovel; }
    public DeferredItem<Item> hoe()     { return hoe; }
}
