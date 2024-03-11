
package igentuman.nc.network.toServer;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.network.INcPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
        ServerPlayerEntity player = context.getSender();
        if (player == null) {
            return;
        }
        TileEntity be = player.level.getBlockEntity(tilePosition);
        if(!(be instanceof NCProcessorBE<?>)) {
            return;
        }
        ((NCProcessorBE<?>) be).voidFluidSlot(slotId);
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(slotId);
    }

    public static PacketFlushSlotContent decode(PacketBuffer buffer) {
         PacketFlushSlotContent packet = new PacketFlushSlotContent();
          packet.tilePosition = buffer.readBlockPos();
          packet.slotId = buffer.readInt();
          return packet;
    }
}