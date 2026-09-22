package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.multiblock.MultiblockDebugClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public record PacketMultiblockDebug(
        BlockPos controllerPos,
        ResourceLocation machineId,
        String phase,
        String status,
        String diagnosticKey,
        @Nullable BlockPos minimum,
        @Nullable BlockPos maximum,
        @Nullable BlockPos start,
        @Nullable BlockPos end,
        @Nullable BlockPos failingPosition,
        @Nullable ResourceLocation expected,
        @Nullable ResourceLocation actual,
        long completed,
        long total,
        long elapsedNanos
) implements CustomPacketPayload {

    public static final Type<PacketMultiblockDebug> TYPE =
            new Type<>(NuclearCraft.rl("multiblock_debug"));

    public static final StreamCodec<FriendlyByteBuf, PacketMultiblockDebug> STREAM_CODEC =
            StreamCodec.of(PacketMultiblockDebug::encode, PacketMultiblockDebug::decode);

    private static void encode(FriendlyByteBuf buf, PacketMultiblockDebug packet) {
        BlockPos.STREAM_CODEC.encode(buf, packet.controllerPos);
        buf.writeResourceLocation(packet.machineId);
        buf.writeUtf(packet.phase);
        buf.writeUtf(packet.status);
        buf.writeUtf(packet.diagnosticKey);
        writeNullablePos(buf, packet.minimum);
        writeNullablePos(buf, packet.maximum);
        writeNullablePos(buf, packet.start);
        writeNullablePos(buf, packet.end);
        writeNullablePos(buf, packet.failingPosition);
        writeNullableId(buf, packet.expected);
        writeNullableId(buf, packet.actual);
        buf.writeVarLong(packet.completed);
        buf.writeVarLong(packet.total);
        buf.writeVarLong(packet.elapsedNanos);
    }

    private static PacketMultiblockDebug decode(FriendlyByteBuf buf) {
        return new PacketMultiblockDebug(
                BlockPos.STREAM_CODEC.decode(buf),
                buf.readResourceLocation(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                readNullablePos(buf),
                readNullablePos(buf),
                readNullablePos(buf),
                readNullablePos(buf),
                readNullablePos(buf),
                readNullableId(buf),
                readNullableId(buf),
                buf.readVarLong(),
                buf.readVarLong(),
                buf.readVarLong());
    }

    private static void writeNullablePos(FriendlyByteBuf buf, @Nullable BlockPos pos) {
        buf.writeBoolean(pos != null);
        if (pos != null) BlockPos.STREAM_CODEC.encode(buf, pos);
    }

    @Nullable
    private static BlockPos readNullablePos(FriendlyByteBuf buf) {
        return buf.readBoolean() ? BlockPos.STREAM_CODEC.decode(buf) : null;
    }

    private static void writeNullableId(FriendlyByteBuf buf, @Nullable ResourceLocation id) {
        buf.writeBoolean(id != null);
        if (id != null) buf.writeResourceLocation(id);
    }

    @Nullable
    private static ResourceLocation readNullableId(FriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readResourceLocation() : null;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketMultiblockDebug packet, IPayloadContext context) {
        context.enqueueWork(() -> MultiblockDebugClient.update(packet));
    }
}
