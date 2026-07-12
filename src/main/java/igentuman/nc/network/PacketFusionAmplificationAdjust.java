package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server request to set the fusion reactor RF amplification adjustment (1-100). */
public record PacketFusionAmplificationAdjust(BlockPos pos, int value) implements CustomPacketPayload {

    public static final Type<PacketFusionAmplificationAdjust> TYPE =
            new Type<>(NuclearCraft.rl("fusion_amplification_adjust"));

    public static final StreamCodec<FriendlyByteBuf, PacketFusionAmplificationAdjust> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketFusionAmplificationAdjust::pos,
                    ByteBufCodecs.VAR_INT, PacketFusionAmplificationAdjust::value,
                    PacketFusionAmplificationAdjust::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFusionAmplificationAdjust packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            BlockEntity be = serverPlayer.serverLevel().getBlockEntity(packet.pos());
            if (be instanceof FusionReactorControllerBE controller) {
                controller.setAmplificationAdjustment(packet.value());
            }
        });
    }
}
