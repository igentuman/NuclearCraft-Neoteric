package igentuman.nc.compat.jei;

import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;

public class IsotopeInfoRecipe {

    public static final String[] VARIANT_SUFFIXES = {"", "_za", "_ox", "_ni"};
    public static final String[] VARIANT_KEYS = {
            "fuel.variant.default", "fuel.variant.zirconium_alloy",
            "fuel.variant.oxide", "fuel.variant.nitride"
    };

    private final ResourceLocation id;
    private final String name;
    private final List<Variant> variants;

    public IsotopeInfoRecipe(String name) {
        this.id = rl("isotope_info_" + name.replace('/', '_'));
        this.name = name;
        this.variants = buildVariants(name);
    }

    private static List<Variant> buildVariants(String name) {
        List<Variant> out = new ArrayList<>();
        IsotopeEntry entry = ModEntries.ISOTOPES.get(name);
        if (entry == null) return out;
        Map<String, DeferredItem<net.minecraft.world.item.Item>> entryVariants = entry.variants();
        for (int i = 0; i < VARIANT_SUFFIXES.length; i++) {
            String suffix = VARIANT_SUFFIXES[i];
            DeferredItem<net.minecraft.world.item.Item> item = entryVariants.get(suffix);
            if (item == null) continue;
            out.add(new Variant(VARIANT_KEYS[i], new ItemStack(item.get())));
        }
        return out;
    }

    public ResourceLocation getId() { return id; }
    public String getName() { return name; }
    public List<Variant> getVariants() { return variants; }
    public ItemStack getBaseItem() { return variants.isEmpty() ? ItemStack.EMPTY : variants.get(0).item; }

    public static final class Variant {
        public final String labelKey;
        public final ItemStack item;

        public Variant(String labelKey, ItemStack item) {
            this.labelKey = labelKey;
            this.item = item;
        }
    }
}
