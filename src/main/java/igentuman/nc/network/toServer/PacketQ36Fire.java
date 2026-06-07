package igentuman.nc.network.toServer;

import igentuman.nc.item.Q36Item;
import igentuman.nc.network.INcPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PacketQ36Fire implements INcPacket {

    public PacketQ36Fire() {
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        Q36Item.serverFire(player);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
    }

    public static PacketQ36Fire decode(FriendlyByteBuf buffer) {
        return new PacketQ36Fire();
    }
}
