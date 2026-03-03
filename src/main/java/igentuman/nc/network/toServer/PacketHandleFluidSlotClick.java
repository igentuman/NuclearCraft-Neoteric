package igentuman.nc.network.toServer;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketHandleFluidSlotClick implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketHandleFluidSlotClick> TYPE =
        new CustomPacketPayload.Type<>(NuclearCraft.rl("handle_fluid_slot_click"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketHandleFluidSlotClick> STREAM_CODEC =
        StreamCodec.of((buf, pkt) -> pkt.encode(buf), PacketHandleFluidSlotClick::decode);

    private BlockPos tilePosition;
    private int slotId;
    private ItemStack fluidStackHandler;


    public PacketHandleFluidSlotClick() {

    }

    public PacketHandleFluidSlotClick(BlockPos position, int slotId, ItemStack carried) {
        this.tilePosition = position;
        this.slotId = slotId;
        this.fluidStackHandler = carried;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketHandleFluidSlotClick packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.handlePacket(context));
    }

    private void handlePacket(IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player == null) {
            return;
        }
        BlockEntity be = player.level().getBlockEntity(tilePosition);
        if((be instanceof NCProcessorBE ncBe)) {
            ncBe.handleFluidItemClick(slotId, fluidStackHandler, player);
            return;
        }
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(slotId);
        ItemStack.STREAM_CODEC.encode(buffer, fluidStackHandler);
    }

    public static PacketHandleFluidSlotClick decode(RegistryFriendlyByteBuf buffer) {
        PacketHandleFluidSlotClick packet = new PacketHandleFluidSlotClick();
        packet.tilePosition = buffer.readBlockPos();
        packet.slotId = buffer.readInt();
        packet.fluidStackHandler = ItemStack.STREAM_CODEC.decode(buffer);
        return packet;
    }
}
