package igentuman.nc.network.toServer;

import igentuman.nc.block.crafter.entity.EngineersCrafterBE;
import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.handler.crafter.AggregatedInventory;
import igentuman.nc.network.INcPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/**
 * Pulls an item out of the crafter's aggregated container inventory onto the cursor (or into the
 * player inventory when {@code toInventory}). The sample only selects <em>which</em> item to pull;
 * the amount is capped server-side by max stack size and by what the containers actually hold, so a
 * spoofed packet cannot extract more than exists.
 */
public class PacketCrafterExtract implements INcPacket {

    private BlockPos pos;
    private ItemStack sample;
    private boolean single;
    private boolean toInventory;

    public PacketCrafterExtract() {
    }

    public PacketCrafterExtract(BlockPos pos, ItemStack sample, boolean single, boolean toInventory) {
        this.pos = pos;
        this.sample = sample;
        this.single = single;
        this.toInventory = toInventory;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        if (!(player.containerMenu instanceof EngineersCrafterContainer menu)) return;
        if (!menu.getBlockPos().equals(pos)) return;
        if (!menu.stillValid(player)) return;
        if (sample == null || sample.isEmpty()) return;

        EngineersCrafterBE be = menu.blockEntity;
        AggregatedInventory agg = new AggregatedInventory(be.containerSlots);
        int amount = single ? 1 : sample.getMaxStackSize();
        ItemStack got = agg.extract(sample, amount, false);
        if (got.isEmpty()) return;

        if (toInventory) {
            player.getInventory().add(got);
            if (!got.isEmpty()) {
                ItemStack back = agg.insert(got, false);
                if (!back.isEmpty()) player.drop(back, false);
            }
        } else {
            ItemStack cur = menu.getCarried();
            if (cur.isEmpty()) {
                menu.setCarried(got);
            } else if (ItemStack.isSameItemSameTags(cur, got)
                    && cur.getCount() + got.getCount() <= cur.getMaxStackSize()) {
                cur.grow(got.getCount());
                menu.setCarried(cur);
            } else {
                ItemStack back = agg.insert(got, false);
                if (!back.isEmpty()) player.drop(back, false);
            }
        }

        be.markUpdated();
        menu.broadcastChanges();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeItem(sample);
        buffer.writeBoolean(single);
        buffer.writeBoolean(toInventory);
    }

    public static PacketCrafterExtract decode(FriendlyByteBuf buffer) {
        PacketCrafterExtract packet = new PacketCrafterExtract();
        packet.pos = buffer.readBlockPos();
        packet.sample = buffer.readItem();
        packet.single = buffer.readBoolean();
        packet.toInventory = buffer.readBoolean();
        return packet;
    }
}
