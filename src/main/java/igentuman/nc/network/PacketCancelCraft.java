package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.container.EngineersCrafterContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketCancelCraft(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PacketCancelCraft> TYPE = new Type<>(NuclearCraft.rl("crafter_cancel"));

    public static final StreamCodec<FriendlyByteBuf, PacketCancelCraft> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketCancelCraft::pos,
                    PacketCancelCraft::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketCancelCraft packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof EngineersCrafterContainer menu)) return;
            if (!menu.getBlockPos().equals(packet.pos())) return;
            if (!menu.stillValid(player)) return;
            menu.blockEntity.cancelJob();
        });
    }
}
