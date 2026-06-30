package igentuman.nc.network;

import igentuman.nc.Main;
import igentuman.nc.block_entity.fission.FissionReactorControllerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server request to arm the fission reactor's energy/steam mode toggle. */
public record PacketFissionToggleMode(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PacketFissionToggleMode> TYPE =
            new Type<>(Main.rl("fission_toggle_mode"));

    public static final StreamCodec<FriendlyByteBuf, PacketFissionToggleMode> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketFissionToggleMode::pos,
                    PacketFissionToggleMode::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFissionToggleMode packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            BlockEntity be = serverPlayer.serverLevel().getBlockEntity(packet.pos());
            if (be instanceof FissionReactorControllerBE controller) {
                controller.requestToggleMode();
            }
        });
    }
}
