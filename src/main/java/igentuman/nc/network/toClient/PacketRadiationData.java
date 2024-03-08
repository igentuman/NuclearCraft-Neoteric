package igentuman.nc.network.toClient;

import igentuman.nc.network.INcPacket;
import igentuman.nc.radiation.client.ClientRadiationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;

public class PacketRadiationData implements INcPacket {

    private final Map<Long, Long> radiation;
    private final int playerRadiation;

    public PacketRadiationData(Map<Long, Long> radiation) {
        this(radiation, 0);
    }

    public PacketRadiationData(long id, Long aLong, int playerRadiation) {
        radiation = new HashMap<>();
        radiation.put(id, aLong);
        this.playerRadiation = playerRadiation;
    }

    public PacketRadiationData(Map<Long, Long> radiation, int playerRadiation) {
        this.radiation = radiation;
        this.playerRadiation = playerRadiation;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ClientRadiationData.setWorldRadiation(radiation);
            ClientRadiationData.setPlayerRadiation(playerRadiation);
        });
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(radiation.size());
        for(Map.Entry<Long, Long> entry : radiation.entrySet()) {
            buffer.writeLong(entry.getKey());
            buffer.writeLong(entry.getValue());
        }
        buffer.writeInt(playerRadiation);
    }

    public static PacketRadiationData decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        Map<Long, Long> radiation = new HashMap<>();
        for(int i = 0; i < size; i++) {
            radiation.put(buffer.readLong(), buffer.readLong());
        }
        int playerRadiation = buffer.readInt();
        return new PacketRadiationData(radiation, playerRadiation);
    }
}
