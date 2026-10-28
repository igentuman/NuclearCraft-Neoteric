package igentuman.nc.client.crafter;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class PendingCraftDenied {

    public static List<ItemStack> items = List.of();
    public static List<Integer> amounts = List.of();
    public static List<FluidStack> fluids = List.of();
    public static List<Integer> fluidAmounts = List.of();
    public static boolean tooComplex = false;
    public static boolean pending = false;

    public static void set(List<ItemStack> items, List<Integer> amounts,
                           List<FluidStack> fluids, List<Integer> fluidAmounts, boolean tooComplex) {
        PendingCraftDenied.items = items;
        PendingCraftDenied.amounts = amounts;
        PendingCraftDenied.fluids = fluids;
        PendingCraftDenied.fluidAmounts = fluidAmounts;
        PendingCraftDenied.tooComplex = tooComplex;
        PendingCraftDenied.pending = true;
    }

    public static void clear() {
        items = List.of();
        amounts = List.of();
        fluids = List.of();
        fluidAmounts = List.of();
        tooComplex = false;
        pending = false;
    }
}
