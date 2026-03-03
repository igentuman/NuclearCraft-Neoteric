package igentuman.nc.network.toClient;

import igentuman.nc.NuclearCraft;
import igentuman.nc.radiation.client.ClientRadiationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketPlayerRadiationData implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketPlayerRadiationData> TYPE =
        new CustomPacketPayload.Type<>(NuclearCraft.rl("player_radiation_data"));

    public static final StreamCodec<FriendlyByteBuf, PacketPlayerRadiationData> STREAM_CODEC =
        StreamCodec.of((buf, pkt) -> pkt.encode(buf), PacketPlayerRadiationData::decode);

    private final long playerRadiation;

    public PacketPlayerRadiationData(long playerRadiation) {
        this.playerRadiation = playerRadiation;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketPlayerRadiationData packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientRadiationData.setPlayerRadiation(packet.playerRadiation);
        });
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeLong(playerRadiation);
    }

    public static PacketPlayerRadiationData decode(FriendlyByteBuf buffer) {
        long playerRadiation = buffer.readLong();
        return new PacketPlayerRadiationData(playerRadiation);
    }
}
