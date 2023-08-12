package igentuman.nc.network.toClient;

import igentuman.nc.network.INcPacket;
import igentuman.nc.radiation.client.ClientWorldRadiationData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;

public class PacketRadiationData implements INcPacket {

    private final Map<Long, Long> radiation;

    public PacketRadiationData(Map<Long, Long> radiation) {
        this.radiation = radiation;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ClientWorldRadiationData.set(radiation);
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

    public static PacketRadiationData decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        Map<Long, Long> radiation = new HashMap<>();
        for(int i = 0; i < size; i++) {
            radiation.put(buffer.readLong(), buffer.readLong());
        }
        return new PacketRadiationData(radiation);
    }
}
