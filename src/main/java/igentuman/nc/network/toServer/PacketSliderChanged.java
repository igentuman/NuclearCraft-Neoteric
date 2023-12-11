package igentuman.nc.network.toServer;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.block.entity.fission.FissionPortBE;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

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
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }

        NuclearCraftBE be = (NuclearCraftBE) player.level().getBlockEntity(tilePosition);
        if(be != null) {
            be.handleSliderUpdate(buttonId, ratio);
        }
    }

    @Override
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