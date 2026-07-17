package igentuman.nc.network.toServer;

import igentuman.nc.block.crafter.entity.EngineersCrafterBE;
import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * Cancels the crafter's running autocraft job. The server returns everything already reserved in the
 * job's internal buffer back to the containers; carries only a position, so a spoofed packet can do
 * nothing beyond cancelling a job the player already has open.
 */
public class PacketCancelCraft implements INcPacket {

    private BlockPos pos;

    public PacketCancelCraft() {
    }

    public PacketCancelCraft(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        if (!(player.containerMenu instanceof EngineersCrafterContainer menu)) return;
        if (!menu.getBlockPos().equals(pos)) return;
        if (!menu.stillValid(player)) return;

        EngineersCrafterBE be = menu.blockEntity;
        be.cancelJob();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    public static PacketCancelCraft decode(FriendlyByteBuf buffer) {
        PacketCancelCraft packet = new PacketCancelCraft();
        packet.pos = buffer.readBlockPos();
        return packet;
    }
}
