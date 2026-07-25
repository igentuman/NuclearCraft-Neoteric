package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.item.Q36Item;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketQ36Fire() implements CustomPacketPayload {

    public static final Type<PacketQ36Fire> TYPE = new Type<>(NuclearCraft.rl("q36_fire"));

    public static final StreamCodec<FriendlyByteBuf, PacketQ36Fire> STREAM_CODEC =
            StreamCodec.unit(new PacketQ36Fire());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketQ36Fire pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            Q36Item.serverFire(sp);
        });
    }
}
