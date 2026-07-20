package igentuman.nc.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Identifies wrench-like tools allowed to configure block side modes, via the conventional wrench tag. */
public final class WrenchUtil {

    /** Conventional cross-mod wrench tag; the mod's multitool is datagen'd into it. */
    public static final TagKey<Item> WRENCH =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));

    private WrenchUtil() {}

    public static boolean isWrench(ItemStack stack) {
        return !stack.isEmpty() && stack.is(WRENCH);
    }
}
