package igentuman.nc.registration;

import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.TextureUtil;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static igentuman.nc.setup.Registers.ITEMS;

/**
 * Registration unit for one fission fuel (a {@code group/name} pair such as {@code uranium/heu-233}).
 * Parallels {@link IsotopeEntry}: it lives in {@link ModEntries#FUELS} rather than the universal
 * {@code ENTRIES} map and owns the family of registered objects derived from a single {@link FuelDef}:
 *
 * <ul>
 *   <li>fuel items ({@link ItemFuel}) and depleted items, one per item variant
 *       ({@code "", _ox, _ni, _za, _tr});</li>
 *   <li>molten + depleted-molten fluids, one per fluid variant ({@code "", _za, _ox, _ni} — triso
 *       has no fluid), each tinted by the average color of the corresponding pellet texture.</li>
 * </ul>
 *
 * <p>Built-in fuels and KubeJS-registered fuels both enter through {@link #register(FuelDef)},
 * so the native and scripted surfaces share one registration path. Registration is idempotent
 * per {@code group/name}; this is the single point where fuel content reaches the deferred
 * registers, so it must run during mod construction (before registry freeze).</p>
 */
public class FuelEntry {

    public final String group;
    public final String name;
    public final String key;
    /** Registry-id stem: {@code fuel_<group>_<name-with-hyphens-as-underscores>}. */
    public final String idStem;

    private FuelDef base;
    private final Map<String, FuelDef> variantDefs = new LinkedHashMap<>();

    private final Map<String, DeferredItem<Item>> fuelItems = new LinkedHashMap<>();
    private final Map<String, DeferredItem<Item>> depletedItems = new LinkedHashMap<>();
    private final List<MaterialEntry> fluids = new ArrayList<>();
    private final Map<String, MaterialEntry> fuelFluidByVariant = new LinkedHashMap<>();
    private final Map<String, MaterialEntry> depletedFluidByVariant = new LinkedHashMap<>();

    private boolean enabled = true;

    private FuelEntry(FuelDef base) {
        this.base = base;
        this.group = base.group;
        this.name = base.name;
        this.key = base.group + "/" + base.name;
        this.idStem = "fuel_" + base.group + "_" + base.name.replace("-", "_");
        recomputeVariants();
    }

    /**
     * Registers a fuel and all its item/fluid forms, recording it in {@link ModEntries#FUELS}.
     * Idempotent: a second call for the same {@code group/name} returns the existing entry without
     * re-registering (so it is safe even if construction runs more than once).
     */
    public static FuelEntry register(FuelDef base) {
        String k = base.group + "/" + base.name;
        FuelEntry existing = ModEntries.FUELS.get(k);
        if (existing != null) {
            return existing;
        }
        FuelEntry entry = new FuelEntry(base);
        ModEntries.FUELS.put(k, entry);
        entry.registerContent();
        return entry;
    }

    private void recomputeVariants() {
        variantDefs.clear();
        variantDefs.putAll(base.variants());
    }

    private void registerContent() {
        String[] itemVariants = base.isSpecial() ? new String[]{""} : FuelDef.ITEM_VARIANTS;
        for (String v : itemVariants) {
            String fuelId = idStem + v;
            fuelItems.put(v, ITEMS.register(fuelId, () -> new ItemFuel(new Item.Properties(), this, v)));
            depletedItems.put(v, ITEMS.register("depleted_" + fuelId, () -> new Item(new Item.Properties())));
        }

        String nm = name.replace("-", "_");
        String[] fluidVariants = base.isSpecial() ? new String[]{""} : FuelDef.FLUID_VARIANTS;
        for (String v : fluidVariants) {
            int activeColor = TextureUtil.getAverageColor("textures/item/fuel/" + group + "/" + nm + v + ".png");
            int depletedColor = TextureUtil.getAverageColor("textures/item/fuel/" + group + "/depleted/" + nm + v + ".png");
            MaterialEntry active = makeFuelFluid(idStem + v, activeColor);
            MaterialEntry depleted = makeFuelFluid("depleted_" + idStem + v, depletedColor);
            fluids.add(active);
            fluids.add(depleted);
            fuelFluidByVariant.put(v, active);
            depletedFluidByVariant.put(v, depleted);
        }
    }

    private static MaterialEntry makeFuelFluid(String fluidBaseName, int color) {
        MaterialEntry mat = new MaterialEntry(fluidBaseName, color);
        mat.setFluidDefinition(FluidDefinition.metal());
        mat.build();
        return mat;
    }

    /** Resolves the live parameter set for an item variant ({@code "", _ox, _ni, _za, _tr}). */
    public FuelDef variantDef(String variant) {
        return variantDefs.getOrDefault(variant, base);
    }

    public FuelDef base() {
        return base;
    }

    /** Mutates the base parameters in place and re-derives all variants (KubeJS override). */
    public void override(Consumer<FuelDef> mutator) {
        mutator.accept(base);
        recomputeVariants();
    }

    /** Marks the fuel disabled. Registry objects cannot be removed once registered, so a disabled
     *  fuel is simply hidden from the creative menu (and, once recipes exist, skipped there). */
    public void disable() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Map<String, DeferredItem<Item>> fuelItems() {
        return Collections.unmodifiableMap(fuelItems);
    }

    public Map<String, DeferredItem<Item>> depletedItems() {
        return Collections.unmodifiableMap(depletedItems);
    }

    public List<MaterialEntry> fluids() {
        return Collections.unmodifiableList(fluids);
    }

    /** Molten fuel source {@link net.minecraft.world.level.material.Fluid} for a fluid variant ({@code "", _za, _ox, _ni}), or {@code null}. */
    public net.minecraft.world.level.material.Fluid fuelFluid(String variant) {
        MaterialEntry mat = fuelFluidByVariant.get(variant);
        return mat == null ? null : mat.materialFluid().source().get();
    }

    /** Molten depleted-fuel source {@link net.minecraft.world.level.material.Fluid} for a fluid variant, or {@code null}. */
    public net.minecraft.world.level.material.Fluid depletedFluid(String variant) {
        MaterialEntry mat = depletedFluidByVariant.get(variant);
        return mat == null ? null : mat.materialFluid().source().get();
    }
}
