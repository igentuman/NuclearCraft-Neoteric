package igentuman.nc.network.toServer;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketSliderChanged implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSliderChanged> TYPE =
        new CustomPacketPayload.Type<>(NuclearCraft.rl("slider_changed"));

    public static final StreamCodec<FriendlyByteBuf, PacketSliderChanged> STREAM_CODEC =
        StreamCodec.of((buf, pkt) -> pkt.encode(buf), PacketSliderChanged::decode);

    private BlockPos tilePosition;
    private int ratio;
    private int buttonId;

    public PacketSliderChanged(Object position, int ratio, int buttonId) {
        this.tilePosition = (BlockPos) position;
        this.ratio = ratio;
        this.buttonId = buttonId;
    }

    public PacketSliderChanged() {

    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketSliderChanged packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.handlePacket(context));
    }

    private void handlePacket(IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player == null) {
            return;
        }

        NuclearCraftBE be = (NuclearCraftBE) player.level().getBlockEntity(tilePosition);
        if(be != null) {
            be.handleSliderUpdate(buttonId, ratio);
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(ratio);
        buffer.writeInt(buttonId);
    }

    public static PacketSliderChanged decode(FriendlyByteBuf buffer) {
         PacketSliderChanged packet = new PacketSliderChanged();
          packet.tilePosition = buffer.readBlockPos();
          packet.ratio = buffer.readInt();
          packet.buttonId = buffer.readInt();
          return packet;
    }
}
