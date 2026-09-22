package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.accelerator.BeamDiverterControllerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketBeamDiverterCycleOutput(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PacketBeamDiverterCycleOutput> TYPE =
            new Type<>(NuclearCraft.rl("beam_diverter_cycle_output"));

    public static final StreamCodec<FriendlyByteBuf, PacketBeamDiverterCycleOutput> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketBeamDiverterCycleOutput::pos,
                    PacketBeamDiverterCycleOutput::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketBeamDiverterCycleOutput packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            BlockEntity be = serverPlayer.serverLevel().getBlockEntity(packet.pos());
            if (be instanceof BeamDiverterControllerBE controller) {
                controller.cycleSelectedOutputChannel();
            }
        });
    }
}
