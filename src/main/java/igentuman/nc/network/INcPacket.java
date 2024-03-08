package igentuman.nc.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public interface INcPacket {

    void handle(NetworkEvent.Context context);

    void encode(FriendlyByteBuf buffer);

    static <PACKET extends INcPacket> void handle(PACKET message, Supplier<NetworkEvent.Context> ctx) {
        if (message != null) {
            //Message should never be null unless something went horribly wrong decoding.
            // In which case we don't want to try enqueuing handling it, or set the packet as handled
            NetworkEvent.Context context = ctx.get();
            context.enqueueWork(() -> message.handle(context));
            context.setPacketHandled(true);
        }
    }
}