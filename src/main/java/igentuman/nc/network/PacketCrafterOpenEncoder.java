package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.container.EngineersCrafterContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketCrafterOpenEncoder(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PacketCrafterOpenEncoder> TYPE = new Type<>(NuclearCraft.rl("crafter_open_encoder"));

    public static final StreamCodec<FriendlyByteBuf, PacketCrafterOpenEncoder> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketCrafterOpenEncoder::pos,
                    PacketCrafterOpenEncoder::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketCrafterOpenEncoder packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof EngineersCrafterContainer menu)) return;
            if (!menu.getBlockPos().equals(packet.pos())) return;
            if (!menu.stillValid(player)) return;
            menu.blockEntity.openEncoder(player);
        });
    }
}
