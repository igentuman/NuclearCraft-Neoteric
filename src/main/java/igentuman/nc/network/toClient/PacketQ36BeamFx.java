package igentuman.nc.network.toClient;

import igentuman.nc.handler.event.client.Q36BeamRenderer;
import igentuman.nc.network.INcPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class PacketQ36BeamFx implements INcPacket {

    private final Vec3 start;
    private final Vec3 end;

    public PacketQ36BeamFx(Vec3 start, Vec3 end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Q36BeamRenderer.add(start, end));
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeDouble(start.x); buffer.writeDouble(start.y); buffer.writeDouble(start.z);
        buffer.writeDouble(end.x);   buffer.writeDouble(end.y);   buffer.writeDouble(end.z);
    }

    public static PacketQ36BeamFx decode(FriendlyByteBuf buffer) {
        Vec3 s = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        Vec3 e = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        return new PacketQ36BeamFx(s, e);
    }
}
