package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.fission.MsrControllerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketMsrRateAdjust(BlockPos pos, int buttonId, int value) implements CustomPacketPayload {

    public static final Type<PacketMsrRateAdjust> TYPE =
            new Type<>(NuclearCraft.rl("msr_rate_adjust"));

    public static final StreamCodec<FriendlyByteBuf, PacketMsrRateAdjust> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketMsrRateAdjust::pos,
                    ByteBufCodecs.VAR_INT, PacketMsrRateAdjust::buttonId,
                    ByteBufCodecs.VAR_INT, PacketMsrRateAdjust::value,
                    PacketMsrRateAdjust::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketMsrRateAdjust packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            BlockEntity be = serverPlayer.serverLevel().getBlockEntity(packet.pos());
            if (be instanceof MsrControllerBE controller) {
                controller.handleSliderUpdate(packet.buttonId(), packet.value());
            }
        });
    }
}
