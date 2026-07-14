package igentuman.nc.registration;

import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.TextureUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static igentuman.nc.setup.Registers.ITEMS;

/** Registration unit for an isotope: registers its item-form variants plus molten fluids and carries decay metadata. */
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
    private final LinkedHashMap<String, MaterialEntry> fluidByVariant = new LinkedHashMap<>();
    private final List<MaterialEntry> fluids = new ArrayList<>();

    private IsotopeEntry(String name, double radiation) {
        this.name = name;
        this.itemId = name.replace("/", "_");
        this.radiation = radiation;
    }

    /**
     * Registers an isotope and its item-form variants, then records it in {@link entries#ISOTOPES}.
     */
    public static IsotopeEntry register(String name, double radiation) {
        IsotopeEntry entry = new IsotopeEntry(name, radiation);
        for (String type : VARIANT_SUFFIXES) {
            String id = entry.itemId + type;
            entry.variants.put(type, ITEMS.register(id, () -> new Item(new Item.Properties())));
            if (name.matches(SPECIAL_FAMILY)) break;
        }
        for (String type : entry.variants.keySet()) {
            int color = TextureUtil.getAverageColor("textures/item/material/isotope/" + name + type + ".png");
            MaterialEntry fluid = makeMoltenFluid(entry.itemId + type, color);
            entry.fluidByVariant.put(type, fluid);
            entry.fluids.add(fluid);
        }
        ModEntries.ISOTOPES.put(name, entry);
        return entry;
    }

    private static MaterialEntry makeMoltenFluid(String fluidBaseName, int color) {
        MaterialEntry mat = new MaterialEntry(fluidBaseName, color);
        mat.setFluidDefinition(FluidDefinition.metal());
        mat.build();
        return mat;
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

    /** Molten fluid carriers, one per registered variant (mirrors {@link #variants()}). */
    public List<MaterialEntry> fluids() {
        return Collections.unmodifiableList(fluids);
    }

    /** The molten fluid {@link MaterialEntry} for a variant suffix, or {@code null}. */
    public MaterialEntry fluidEntry(String variant) {
        return fluidByVariant.get(variant);
    }

    /** The molten source {@link Fluid} for a variant suffix, or {@code null}. */
    public Fluid fluid(String variant) {
        MaterialEntry mat = fluidByVariant.get(variant);
        return mat == null ? null : mat.materialFluid().source().get();
    }
}
