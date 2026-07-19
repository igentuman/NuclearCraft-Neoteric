package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.kugelblitz.ChamberTerminalBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server request to set a kugelblitz terminal slider (0 = energy conversion rate, 1 = quantum frequency). */
public record PacketKugelblitzSliderAdjust(BlockPos pos, int buttonId, int value) implements CustomPacketPayload {

    public static final Type<PacketKugelblitzSliderAdjust> TYPE =
            new Type<>(NuclearCraft.rl("kugelblitz_slider_adjust"));

    public static final StreamCodec<FriendlyByteBuf, PacketKugelblitzSliderAdjust> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketKugelblitzSliderAdjust::pos,
                    ByteBufCodecs.VAR_INT, PacketKugelblitzSliderAdjust::buttonId,
                    ByteBufCodecs.VAR_INT, PacketKugelblitzSliderAdjust::value,
                    PacketKugelblitzSliderAdjust::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketKugelblitzSliderAdjust packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            BlockEntity be = serverPlayer.serverLevel().getBlockEntity(packet.pos());
            if (be instanceof ChamberTerminalBE controller) {
                controller.handleSliderUpdate(packet.buttonId(), packet.value());
            }
        });
    }
}
