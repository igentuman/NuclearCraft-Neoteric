package igentuman.nc.compat.jei;

import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;

public class FuelInfoRecipe {

    public static final String[] VARIANT_SUFFIXES = {"", "_ox", "_ni", "_za", "_tr"};
    public static final String[] VARIANT_KEYS = {
            "fuel.variant.default", "fuel.variant.oxide",
            "fuel.variant.nitride", "fuel.variant.zirconium_alloy",
            "fuel.variant.triso"
    };

    private final ResourceLocation id;
    private final String group;
    private final String name;
    private final List<Variant> variants;

    public FuelInfoRecipe(String group, String name) {
        this.id = rl("fuel_info_" + group + "_" + name);
        this.group = group;
        this.name = name;
        this.variants = buildVariants(group, name);
    }

    private static List<Variant> buildVariants(String group, String name) {
        List<Variant> out = new ArrayList<>();
        FissionFuelEntry entry = ModEntries.FISSION_FUEL.get(group + "/" + name);
        if (entry == null) return out;
        Map<String, DeferredItem<net.minecraft.world.item.Item>> fuelItems = entry.fuelItems();
        for (int i = 0; i < VARIANT_SUFFIXES.length; i++) {
            String suffix = VARIANT_SUFFIXES[i];
            DeferredItem<net.minecraft.world.item.Item> item = fuelItems.get(suffix);
            if (item == null) continue;
            FuelDef def = entry.variantDef(suffix);
            out.add(new Variant(VARIANT_KEYS[i], new ItemStack(item.get()), def, suffix.equals("_tr")));
        }
        return out;
    }

    public ResourceLocation getId() { return id; }
    public String getGroup() { return group; }
    public String getName() { return name; }
    public List<Variant> getVariants() { return variants; }
    public ItemStack getBaseItem() { return variants.isEmpty() ? ItemStack.EMPTY : variants.get(0).item; }

    public static final class Variant {
        public final String labelKey;
        public final ItemStack item;
        public final FuelDef def;
        public final boolean triso;

        public Variant(String labelKey, ItemStack item, FuelDef def, boolean triso) {
            this.labelKey = labelKey;
            this.item = item;
            this.def = def;
            this.triso = triso;
        }
    }
}
