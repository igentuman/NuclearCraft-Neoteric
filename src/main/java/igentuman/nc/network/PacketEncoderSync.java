package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.container.EngineersEncoderContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/** Server -> client: encoder mode and the processor recipe currently selected for encoding. */
public record PacketEncoderSync(int mode, @Nullable ResourceLocation recipeId) implements CustomPacketPayload {

    public static final Type<PacketEncoderSync> TYPE = new Type<>(NuclearCraft.rl("encoder_sync"));

    public static final StreamCodec<FriendlyByteBuf, PacketEncoderSync> STREAM_CODEC =
            StreamCodec.of(PacketEncoderSync::encode, PacketEncoderSync::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(FriendlyByteBuf buf, PacketEncoderSync pkt) {
        buf.writeVarInt(pkt.mode);
        buf.writeBoolean(pkt.recipeId != null);
        if (pkt.recipeId != null) buf.writeResourceLocation(pkt.recipeId);
    }

    private static PacketEncoderSync decode(FriendlyByteBuf buf) {
        int mode = buf.readVarInt();
        ResourceLocation id = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new PacketEncoderSync(mode, id);
    }

    public static void handle(PacketEncoderSync packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof EngineersEncoderContainer menu) {
                menu.applySync(packet.mode(), packet.recipeId());
            }
        });
    }
}
