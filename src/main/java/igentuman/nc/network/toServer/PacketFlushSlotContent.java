package igentuman.nc.network.toServer;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.block.fusion.entity.FusionCoreBE;
import igentuman.nc.block.turbine.entity.TurbineControllerBE;
import igentuman.nc.block.turbine.entity.TurbinePortBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketFlushSlotContent implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFlushSlotContent> TYPE =
        new CustomPacketPayload.Type<>(NuclearCraft.rl("flush_slot_content"));

    public static final StreamCodec<FriendlyByteBuf, PacketFlushSlotContent> STREAM_CODEC =
        StreamCodec.of((buf, pkt) -> pkt.encode(buf), PacketFlushSlotContent::decode);

    private BlockPos tilePosition;
    private int slotId;

    public PacketFlushSlotContent(Object position, int slotId) {
        this.tilePosition = (BlockPos) position;
        this.slotId = slotId;
    }

    public PacketFlushSlotContent() {

    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketFlushSlotContent packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.handlePacket(context));
    }

    private void handlePacket(IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player == null) {
            return;
        }
        BlockEntity be = player.level().getBlockEntity(tilePosition);
        if((be instanceof NCProcessorBE ncBe)) {
            ncBe.voidFluidSlot(slotId);
            return;
        }
        if((be instanceof TurbineControllerBE ncBe)) {
            ncBe.voidFluidSlot(slotId);
            return;
        }
        if((be instanceof TurbinePortBE ncBe)) {
            ncBe.voidFluidSlot(slotId);
            return;
        }
        if((be instanceof FusionCoreBE ncBe)) {
            ncBe.voidFluidSlot(slotId);
            return;
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(slotId);
    }

    public static PacketFlushSlotContent decode(FriendlyByteBuf buffer) {
         PacketFlushSlotContent packet = new PacketFlushSlotContent();
          packet.tilePosition = buffer.readBlockPos();
          packet.slotId = buffer.readInt();
          return packet;
    }
}
