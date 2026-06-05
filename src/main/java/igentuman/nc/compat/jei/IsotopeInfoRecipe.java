package igentuman.nc.compat.jei;

import igentuman.nc.setup.registration.FissionFuel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

public class IsotopeInfoRecipe {

    public static final String[] VARIANT_SUFFIXES = {"", "_za", "_ox", "_ni"};
    public static final String[] VARIANT_KEYS = {"fuel.variant.default", "fuel.variant.zirconium_alloy", "fuel.variant.oxide", "fuel.variant.nitride"};

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
        boolean specialFamily = name.matches("xenorium.*|quantite|beryllium.*|calcium.*|iridium.*|magnesium.*|sodium.*|cobalt.*");
        for (int i = 0; i < VARIANT_SUFFIXES.length; i++) {
            String suffix = VARIANT_SUFFIXES[i];
            if (specialFamily && !suffix.isEmpty()) continue;
            RegistryObject<net.minecraft.world.item.Item> item = FissionFuel.NC_ISOTOPES.get(name + suffix);
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
