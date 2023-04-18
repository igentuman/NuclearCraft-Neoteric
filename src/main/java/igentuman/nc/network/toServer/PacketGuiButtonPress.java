package igentuman.nc.network.toServer;

import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

public class PacketGuiButtonPress implements INcPacket {

    private BlockPos tilePosition;
    private int buttonId;

    public PacketGuiButtonPress(Object position, int bId) {
        this.tilePosition = (BlockPos) position;
        buttonId = bId;
    }

    public PacketGuiButtonPress() {

    }


    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {


    }

    public static PacketGuiButtonPress decode(FriendlyByteBuf buffer) {
         PacketGuiButtonPress packet = new PacketGuiButtonPress();
          packet.tilePosition = buffer.readBlockPos();
          packet.buttonId = buffer.readInt();
          return packet;
    }



}