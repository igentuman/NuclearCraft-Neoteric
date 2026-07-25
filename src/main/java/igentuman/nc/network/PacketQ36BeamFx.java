package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.handler.event.client.Q36BeamRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketQ36BeamFx(Vec3 start, Vec3 end) implements CustomPacketPayload {

    public static final Type<PacketQ36BeamFx> TYPE = new Type<>(NuclearCraft.rl("q36_beam_fx"));

    public static final StreamCodec<FriendlyByteBuf, PacketQ36BeamFx> STREAM_CODEC =
            StreamCodec.of(PacketQ36BeamFx::encode, PacketQ36BeamFx::decode);

    private static void encode(FriendlyByteBuf buf, PacketQ36BeamFx pkt) {
        buf.writeDouble(pkt.start.x); buf.writeDouble(pkt.start.y); buf.writeDouble(pkt.start.z);
        buf.writeDouble(pkt.end.x);   buf.writeDouble(pkt.end.y);   buf.writeDouble(pkt.end.z);
    }

    private static PacketQ36BeamFx decode(FriendlyByteBuf buf) {
        Vec3 s = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        Vec3 e = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new PacketQ36BeamFx(s, e);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketQ36BeamFx pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> Q36BeamRenderer.add(pkt.start(), pkt.end()));
    }
}
