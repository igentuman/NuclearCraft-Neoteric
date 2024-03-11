package igentuman.nc.network.toServer;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.network.INcPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSliderChanged implements INcPacket {

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
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player == null) {
            return;
        }

        NuclearCraftBE be = (NuclearCraftBE) player.level.getBlockEntity(tilePosition);
        if(be != null) {
            be.handleSliderUpdate(buttonId, ratio);
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(ratio);
        buffer.writeInt(buttonId);
    }

    public static PacketSliderChanged decode(PacketBuffer buffer) {
         PacketSliderChanged packet = new PacketSliderChanged();
          packet.tilePosition = buffer.readBlockPos();
          packet.ratio = buffer.readInt();
          packet.buttonId = buffer.readInt();
          return packet;
    }
}