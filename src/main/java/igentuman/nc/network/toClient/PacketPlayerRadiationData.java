package igentuman.nc.network.toClient;

import igentuman.nc.network.INcPacket;
import igentuman.nc.radiation.client.ClientRadiationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketPlayerRadiationData implements INcPacket {

    private final long playerRadiation;

    public PacketPlayerRadiationData(long playerRadiation) {
        this.playerRadiation = playerRadiation;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ClientRadiationData.setPlayerRadiation(playerRadiation);
        });
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeLong(playerRadiation);
    }

    public static PacketPlayerRadiationData decode(FriendlyByteBuf buffer) {
        int playerRadiation = buffer.readInt();
        return new PacketPlayerRadiationData(playerRadiation);
    }
}
