package igentuman.nc.network;

import igentuman.nc.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketMultiblockFormed(BlockPos controllerPos, long[] structurePositions)
        implements CustomPacketPayload {

    public static final Type<PacketMultiblockFormed> TYPE =
            new Type<>(Main.rl("multiblock_formed"));

    public static final StreamCodec<FriendlyByteBuf, PacketMultiblockFormed> STREAM_CODEC =
            StreamCodec.of(PacketMultiblockFormed::encode, PacketMultiblockFormed::decode);

    private static void encode(FriendlyByteBuf buf, PacketMultiblockFormed pkt) {
        BlockPos.STREAM_CODEC.encode(buf, pkt.controllerPos);
        buf.writeVarInt(pkt.structurePositions.length);
        for (long p : pkt.structurePositions) buf.writeLong(p);
    }

    private static PacketMultiblockFormed decode(FriendlyByteBuf buf) {
        BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
        int n = buf.readVarInt();
        long[] arr = new long[n];
        for (int i = 0; i < n; i++) arr[i] = buf.readLong();
        return new PacketMultiblockFormed(pos, arr);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketMultiblockFormed packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Client-side: notify any controller BE at controllerPos that formation occurred.
            // Hook for client visuals - left intentionally minimal; concrete BE/screen can listen
            // via its own state and the index of positions.
        });
    }
}
