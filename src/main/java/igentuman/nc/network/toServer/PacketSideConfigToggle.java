package igentuman.nc.network.toServer;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.network.INcPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSideConfigToggle implements INcPacket {

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
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player == null) {
            return;
        }
        TileEntity be = player.level.getBlockEntity(tilePosition);
        if(!(be instanceof NCProcessorBE)) {
            return;
        }
        NCProcessorBE processor = (NCProcessorBE) be;
        processor.toggleSideConfig(slotId, direction);
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(slotId);
        buffer.writeInt(direction);
    }

    public static PacketSideConfigToggle decode(PacketBuffer buffer) {
         PacketSideConfigToggle packet = new PacketSideConfigToggle();
          packet.tilePosition = buffer.readBlockPos();
          packet.slotId = buffer.readInt();
          packet.direction = buffer.readInt();
          return packet;
    }

}