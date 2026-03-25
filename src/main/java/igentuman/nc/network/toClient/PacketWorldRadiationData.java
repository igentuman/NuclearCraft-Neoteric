package igentuman.nc.network.toClient;

import igentuman.nc.NuclearCraft;
import igentuman.nc.radiation.client.ClientRadiationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public class PacketWorldRadiationData implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketWorldRadiationData> TYPE =
        new CustomPacketPayload.Type<>(NuclearCraft.rl("world_radiation_data"));

    public static final StreamCodec<FriendlyByteBuf, PacketWorldRadiationData> STREAM_CODEC =
        StreamCodec.of((buf, pkt) -> pkt.encode(buf), PacketWorldRadiationData::decode);

    private final HashMap<Long, Long> radiation;

    public PacketWorldRadiationData(long id, Long aLong) {
        radiation = new HashMap<>();
        radiation.put(id, aLong);
    }

    public PacketWorldRadiationData(HashMap<Long, Long> radiation) {
        this.radiation = new HashMap<>(radiation);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketWorldRadiationData packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientRadiationData.setWorldRadiation(packet.radiation);
        });
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(radiation.size());
        for(Map.Entry<Long, Long> entry : radiation.entrySet()) {
            buffer.writeLong(entry.getKey());
            buffer.writeLong(entry.getValue());
        }
    }

    public static PacketWorldRadiationData decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        HashMap<Long, Long> radiation = new HashMap<>();
        for(int i = 0; i < size; i++) {
            radiation.put(buffer.readLong(), buffer.readLong());
        }
        return new PacketWorldRadiationData(radiation);
    }
}
