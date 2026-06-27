package igentuman.nc.registration;

import igentuman.nc.setup.ModEntries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static igentuman.nc.setup.Registers.ITEMS;

/**
 * Registration unit for a NuclearCraft isotope. Parallels {@link ModEntry} but lives in its own
 * {@link ModEntries#ISOTOPES} registry rather than the universal {@code ENTRIES} map, because an
 * isotope is not a block/machine/material-product but a family of fuel-cladding item variants
 * carrying decay metadata.
 *
 * <p>Each isotope registers up to four item forms keyed by suffix: base ({@code ""}), zirconium
 * alloy ({@code "_za"}), oxide ({@code "_ox"}) and nitride ({@code "_ni"}). Isotopes in the
 * {@link #SPECIAL_FAMILY special families} register the base form only.</p>
 *
 * <p>The base name may contain a {@code '/'} (e.g. {@code "uranium/238"}); the registry/item id is
 * the slash-to-underscore form ({@code "uranium_238"}).</p>
 */
public class IsotopeEntry {

    /** Item-form suffixes, in registration order. */
    public static final String[] VARIANT_SUFFIXES = {"", "_za", "_ox", "_ni"};

    /** Families that only ever get the base form (no cladding variants). */
    public static final String SPECIAL_FAMILY =
            "xenorium.*|quantite|beryllium.*|calcium.*|iridium.*|magnesium.*|sodium.*|cobalt.*";

    /** Base name, possibly containing a {@code '/'} (e.g. {@code "uranium/238"}). */
    public final String name;
    /** Registry/item id form of {@link #name} ({@code '/'} replaced with {@code '_'}). */
    public final String itemId;
    /** Per-item radiation level (pRads/tick) carried for the radiation system, integrated later. */
    public final double radiation;

    /** Optional half-life metadata (unit-agnostic). {@code null} until decay data is supplied. */
    private Double halfLife;
    /** Optional decay-product pointer (isotope name). {@code null} until decay data is supplied. */
    private String decaysInto;

    private final LinkedHashMap<String, DeferredItem<Item>> variants = new LinkedHashMap<>();

    private IsotopeEntry(String name, double radiation) {
        this.name = name;
        this.itemId = name.replace("/", "_");
        this.radiation = radiation;
    }

    /**
     * Registers an isotope and its item-form variants, then records it in {@link ModEntries#ISOTOPES}.
     */
    public static IsotopeEntry register(String name, double radiation) {
        IsotopeEntry entry = new IsotopeEntry(name, radiation);
        for (String type : VARIANT_SUFFIXES) {
            String id = entry.itemId + type;
            entry.variants.put(type, ITEMS.register(id, () -> new Item(new Item.Properties())));
            if (name.matches(SPECIAL_FAMILY)) break;
        }
        ModEntries.ISOTOPES.put(name, entry);
        return entry;
    }

    public IsotopeEntry halfLife(double halfLife) {
        this.halfLife = halfLife;
        return this;
    }

    public IsotopeEntry decaysInto(String isotopeName) {
        this.decaysInto = isotopeName;
        return this;
    }

    public Double halfLife() {
        return halfLife;
    }

    public String decaysInto() {
        return decaysInto;
    }

    public double radiation() {
        return radiation;
    }

    /** Suffix → registered item, in registration order. */
    public Map<String, DeferredItem<Item>> variants() {
        return Collections.unmodifiableMap(variants);
    }

    /** The base ({@code ""}) item form. */
    public DeferredItem<Item> base() {
        return variants.get("");
    }
}
