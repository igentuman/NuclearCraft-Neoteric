package igentuman.nc.compat.mbtool;

import igentuman.mbtool.Mbtool;
import igentuman.mbtool.item.MultibuilderItem;
import igentuman.mbtool.util.MultiblockStructure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.NuclearCraft.MODID;

public class MbtoolHelper {

    public static ItemStack toolIcon() {
        return new ItemStack(Mbtool.MBTOOL.get());
    }

    public static boolean loadDesign(ServerPlayer player, CompoundTag structureNbt) {
        MultiblockStructure structure = new MultiblockStructure(
                ResourceLocation.fromNamespaceAndPath(MODID, "runtime_reactor"), structureNbt, "runtime_reactor");

        for (ItemStack stack : player.getInventory().items) {
            if (setRuntimeStructure(stack, structure)) return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (setRuntimeStructure(stack, structure)) return true;
        }
        return false;
    }

    private static boolean setRuntimeStructure(ItemStack stack, MultiblockStructure structure) {
        if (stack.getItem() instanceof MultibuilderItem item) {
            item.setRuntimeStructure(stack, structure);
            return true;
        }
        return false;
    }
}
