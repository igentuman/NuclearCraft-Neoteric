package igentuman.nc.compat.mbtool;

import igentuman.mbtool.Mbtool;
import igentuman.mbtool.item.MultibuilderItem;
import igentuman.mbtool.util.MultiblockStructure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.NuclearCraft.MODID;

/** Copies a Reactor Designer structure to the player's first Multi Builder Tool. */
public class MbtoolHelper {

    public static ItemStack toolIcon() {
        return new ItemStack(Mbtool.MBTOOL.get());
    }

    public static boolean loadDesign(ServerPlayer player, CompoundTag structureNbt) {
        Inventory inventory = player.getInventory();
        for (ItemStack stack : inventory.getNonEquipmentItems()) {
            if (setRuntimeStructure(stack, structureNbt)) {
                inventory.setChanged();
                return true;
            }
        }
        if (setRuntimeStructure(player.getOffhandItem(), structureNbt)) {
            inventory.setChanged();
            return true;
        }
        return false;
    }

    private static boolean setRuntimeStructure(ItemStack stack, CompoundTag structureNbt) {
        if (stack.getItem() instanceof MultibuilderItem item) {
            MultiblockStructure structure = new MultiblockStructure(
                    Identifier.fromNamespaceAndPath(MODID, "runtime_reactor"), structureNbt, "runtime_reactor");
            item.setRuntimeStructure(stack, structure);
            return true;
        }
        return false;
    }
}
