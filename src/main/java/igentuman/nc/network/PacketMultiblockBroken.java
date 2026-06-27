package igentuman.nc.network;

import igentuman.nc.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketMultiblockBroken(BlockPos controllerPos) implements CustomPacketPayload {

    public static final Type<PacketMultiblockBroken> TYPE =
            new Type<>(Main.rl("multiblock_broken"));

    public static final StreamCodec<FriendlyByteBuf, PacketMultiblockBroken> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketMultiblockBroken::controllerPos,
                    PacketMultiblockBroken::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketMultiblockBroken packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Client-side hook: clear any per-controller visual state.
        });
    }
}
