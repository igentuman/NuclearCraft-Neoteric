package igentuman.nc.network;

import igentuman.nc.Main;
import igentuman.nc.block_entity.MultiblockPortBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketRedstoneModeCycle(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PacketRedstoneModeCycle> TYPE =
            new Type<>(Main.rl("redstone_mode_cycle"));

    public static final StreamCodec<FriendlyByteBuf, PacketRedstoneModeCycle> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketRedstoneModeCycle::pos,
                    PacketRedstoneModeCycle::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketRedstoneModeCycle packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            ServerLevel level = serverPlayer.serverLevel();
            BlockEntity be = level.getBlockEntity(packet.pos());
            if (be instanceof MultiblockPortBE port) {
                port.cycleRedstoneMode();
            }
        });
    }
}
