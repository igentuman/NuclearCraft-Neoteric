package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketScheduledMultiblockFormed(BlockPos controllerPos, CompoundTag geometry)
        implements CustomPacketPayload {

    public static final Type<PacketScheduledMultiblockFormed> TYPE =
            new Type<>(NuclearCraft.rl("scheduled_multiblock_formed"));

    public static final StreamCodec<FriendlyByteBuf, PacketScheduledMultiblockFormed> STREAM_CODEC =
            StreamCodec.of(PacketScheduledMultiblockFormed::encode, PacketScheduledMultiblockFormed::decode);

    private static void encode(FriendlyByteBuf buf, PacketScheduledMultiblockFormed pkt) {
        BlockPos.STREAM_CODEC.encode(buf, pkt.controllerPos);
        buf.writeNbt(pkt.geometry);
    }

    private static PacketScheduledMultiblockFormed decode(FriendlyByteBuf buf) {
        BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
        CompoundTag geometry = buf.readNbt();
        return new PacketScheduledMultiblockFormed(pos, geometry == null ? new CompoundTag() : geometry);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketScheduledMultiblockFormed packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Client-side hook: a controller BE/screen can decode the compact geometry descriptor via
            // MultiblockPersistence.loadFootprint and update its render/UI state from it.
        });
    }
}
