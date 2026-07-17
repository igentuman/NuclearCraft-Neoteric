package igentuman.nc.network.toServer;

import igentuman.nc.block.crafter.entity.EngineersCrafterBE;
import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.handler.crafter.AggregatedInventory;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/**
 * Deposits the player's carried (cursor) stack into the crafter's aggregated container inventory.
 * The item to insert is taken from the server-authoritative carried stack, never from client data,
 * so a spoofed packet cannot conjure items the player does not hold.
 */
public class PacketCrafterInsert implements INcPacket {

    private BlockPos pos;
    private boolean single;

    public PacketCrafterInsert() {
    }

    public PacketCrafterInsert(BlockPos pos, boolean single) {
        this.pos = pos;
        this.single = single;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        if (!(player.containerMenu instanceof EngineersCrafterContainer menu)) return;
        if (!menu.getBlockPos().equals(pos)) return;
        if (!menu.stillValid(player)) return;

        EngineersCrafterBE be = menu.blockEntity;
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) return;

        AggregatedInventory agg = new AggregatedInventory(be.containerSlots);
        boolean changed = false;
        if (single) {
            ItemStack one = carried.copy();
            one.setCount(1);
            if (agg.insert(one, false).isEmpty()) {
                carried.shrink(1);
                menu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
                changed = true;
            }
        } else {
            int before = carried.getCount();
            ItemStack leftover = agg.insert(carried, false);
            if (leftover.getCount() != before) {
                menu.setCarried(leftover);
                changed = true;
            }
        }

        if (changed) {
            be.markUpdated();
            menu.broadcastChanges();
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(single);
    }

    public static PacketCrafterInsert decode(FriendlyByteBuf buffer) {
        PacketCrafterInsert packet = new PacketCrafterInsert();
        packet.pos = buffer.readBlockPos();
        packet.single = buffer.readBoolean();
        return packet;
    }
}
