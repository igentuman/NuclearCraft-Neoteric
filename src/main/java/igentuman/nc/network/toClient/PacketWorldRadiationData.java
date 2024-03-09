package igentuman.nc.network.toClient;

import igentuman.nc.network.INcPacket;
import igentuman.nc.radiation.client.ClientRadiationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;

public class PacketWorldRadiationData implements INcPacket {

    private final Map<Long, Long> radiation;

    public PacketWorldRadiationData(long id, Long aLong) {
        radiation = new HashMap<>();
        radiation.put(id, aLong);
    }

    public PacketWorldRadiationData(Map<Long, Long> radiation) {
        this.radiation = radiation;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ClientRadiationData.setWorldRadiation(radiation);
        });
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(radiation.size());
        for(Map.Entry<Long, Long> entry : radiation.entrySet()) {
            buffer.writeLong(entry.getKey());
            buffer.writeLong(entry.getValue());
        }
    }

    public static PacketWorldRadiationData decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        Map<Long, Long> radiation = new HashMap<>();
        for(int i = 0; i < size; i++) {
            radiation.put(buffer.readLong(), buffer.readLong());
        }
        return new PacketWorldRadiationData(radiation);
    }
}
