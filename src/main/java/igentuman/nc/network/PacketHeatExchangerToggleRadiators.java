package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.heat_exchanger.HeatExchangerControllerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketHeatExchangerToggleRadiators(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PacketHeatExchangerToggleRadiators> TYPE =
            new Type<>(NuclearCraft.rl("heat_exchanger_toggle_radiators"));

    public static final StreamCodec<FriendlyByteBuf, PacketHeatExchangerToggleRadiators> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketHeatExchangerToggleRadiators::pos,
                    PacketHeatExchangerToggleRadiators::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketHeatExchangerToggleRadiators packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            BlockEntity be = serverPlayer.serverLevel().getBlockEntity(packet.pos());
            if (be instanceof HeatExchangerControllerBE controller) {
                controller.toggleRadiators();
            }
        });
    }
}
