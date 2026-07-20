package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketBombDetonationStart(int id, BlockPos epicenter, float yield)
        implements CustomPacketPayload {

    public static final Type<PacketBombDetonationStart> TYPE =
            new Type<>(NuclearCraft.rl("bomb_detonation_start"));

    public static final StreamCodec<FriendlyByteBuf, PacketBombDetonationStart> STREAM_CODEC =
            StreamCodec.of(PacketBombDetonationStart::encode, PacketBombDetonationStart::decode);

    private static void encode(FriendlyByteBuf buf, PacketBombDetonationStart pkt) {
        buf.writeVarInt(pkt.id);
        BlockPos.STREAM_CODEC.encode(buf, pkt.epicenter);
        buf.writeFloat(pkt.yield);
    }

    private static PacketBombDetonationStart decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        BlockPos epicenter = BlockPos.STREAM_CODEC.decode(buf);
        float yield = buf.readFloat();
        return new PacketBombDetonationStart(id, epicenter, yield);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketBombDetonationStart packet, IPayloadContext context) {
        context.enqueueWork(() -> igentuman.nc.client.bomb.BombFxManager.onDetonationStart(
                packet.id(), packet.epicenter(), packet.yield()));
    }
}
