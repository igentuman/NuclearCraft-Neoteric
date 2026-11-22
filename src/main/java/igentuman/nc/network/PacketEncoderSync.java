package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.container.EngineersEncoderContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import igentuman.nc.handler.crafter.EncoderRecipePreview;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/** Server -> client: encoder mode and the processor recipe currently selected for encoding. */
public record PacketEncoderSync(int containerId, int mode, @Nullable Identifier recipeId, @Nullable EncoderRecipePreview preview) implements CustomPacketPayload {

    public static final Type<PacketEncoderSync> TYPE = new Type<>(NuclearCraft.rl("encoder_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketEncoderSync> STREAM_CODEC =
            StreamCodec.of(PacketEncoderSync::encode, PacketEncoderSync::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(RegistryFriendlyByteBuf buf, PacketEncoderSync pkt) {
        buf.writeVarInt(pkt.containerId);
        buf.writeVarInt(pkt.mode);
        buf.writeBoolean(pkt.recipeId != null);
        if (pkt.recipeId != null) buf.writeIdentifier(pkt.recipeId);
        buf.writeBoolean(pkt.preview != null);
        if (pkt.preview != null) EncoderRecipePreview.STREAM_CODEC.encode(buf, pkt.preview);
    }

    private static PacketEncoderSync decode(RegistryFriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        int mode = buf.readVarInt();
        Identifier id = buf.readBoolean() ? buf.readIdentifier() : null;
        EncoderRecipePreview preview = buf.readBoolean() ? EncoderRecipePreview.STREAM_CODEC.decode(buf) : null;
        return new PacketEncoderSync(containerId, mode, id, preview);
    }

    public static void handle(PacketEncoderSync packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof EngineersEncoderContainer menu
                    && menu.containerId == packet.containerId()) {
                menu.applySync(packet.mode(), packet.recipeId(), packet.preview());
            }
        });
    }
}
