
package igentuman.nc.network.toServer;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketFlushSlotContent implements INcPacket {

    private BlockPos tilePosition;
    private int slotId;

    public PacketFlushSlotContent(Object position, int slotId) {
        this.tilePosition = (BlockPos) position;
        this.slotId = slotId;
    }

    public PacketFlushSlotContent() {

    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        BlockEntity be = player.level.getBlockEntity(tilePosition);
        if(!(be instanceof NCProcessorBE<?> ncBe)) {
            return;
        }
        ncBe.voidFluidSlot(slotId);
    }

    @Override
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