package igentuman.nc.network.toServer;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketSideConfigToggle implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSideConfigToggle> TYPE =
        new CustomPacketPayload.Type<>(NuclearCraft.rl("side_config_toggle"));

    public static final StreamCodec<FriendlyByteBuf, PacketSideConfigToggle> STREAM_CODEC =
        StreamCodec.of((buf, pkt) -> pkt.encode(buf), PacketSideConfigToggle::decode);

    private BlockPos tilePosition;
    private int slotId;
    private int direction;

    public PacketSideConfigToggle(Object position, int slotId, int direction) {
        this.tilePosition = (BlockPos) position;
        this.slotId = slotId;
        this.direction = direction;
    }

    public PacketSideConfigToggle() {

    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketSideConfigToggle packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.handlePacket(context));
    }

    private void handlePacket(IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player == null) {
            return;
        }
        BlockEntity be = player.level().getBlockEntity(tilePosition);
        if(!(be instanceof NCProcessorBE)) {
            return;
        }
        NCProcessorBE processor = (NCProcessorBE) be;
        processor.toggleSideConfig(slotId, direction);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(slotId);
        buffer.writeInt(direction);
    }

    public static PacketSideConfigToggle decode(FriendlyByteBuf buffer) {
         PacketSideConfigToggle packet = new PacketSideConfigToggle();
          packet.tilePosition = buffer.readBlockPos();
          packet.slotId = buffer.readInt();
          packet.direction = buffer.readInt();
          return packet;
    }

}
