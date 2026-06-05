package igentuman.nc.compat.jei;

import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.fuel.NCFuel;
import igentuman.nc.setup.registration.FissionFuel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

public class FuelInfoRecipe {

    public static final String[] VARIANT_SUFFIXES = {"", "ox", "ni", "za", "tr"};
    public static final String[] VARIANT_KEYS = {"fuel.variant.default", "fuel.variant.oxide", "fuel.variant.nitride", "fuel.variant.zirconium_alloy", "fuel.variant.triso"};

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
        NCFuel fuel = FuelManager.all().get(group).get(name);
        boolean specialFamily = name.matches("xenorium.*|quantite.*");
        for (int i = 0; i < VARIANT_SUFFIXES.length; i++) {
            String suffix = VARIANT_SUFFIXES[i];
            if (specialFamily && !suffix.isEmpty()) continue;
            RegistryObject<net.minecraft.world.item.Item> item = FissionFuel.NC_FUEL.get(List.of("fuel", group, name, suffix));
            if (item == null) continue;
            FuelDef def = fuel.subType(suffix.isEmpty() ? "" : "_" + suffix);
            out.add(new Variant(VARIANT_KEYS[i], new ItemStack(item.get()), def, suffix.equals("tr")));
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
