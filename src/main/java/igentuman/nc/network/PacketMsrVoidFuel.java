package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.fission.MsrControllerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketMsrVoidFuel(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PacketMsrVoidFuel> TYPE =
            new Type<>(NuclearCraft.rl("msr_void_fuel"));

    public static final StreamCodec<FriendlyByteBuf, PacketMsrVoidFuel> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketMsrVoidFuel::pos,
                    PacketMsrVoidFuel::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketMsrVoidFuel packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            BlockEntity be = serverPlayer.serverLevel().getBlockEntity(packet.pos());
            if (be instanceof MsrControllerBE controller) {
                controller.voidFuel();
            }
        });
    }
}
