package igentuman.nc.network.toServer;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

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
        ServerPlayer player = context.getSender();
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

    @Override
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