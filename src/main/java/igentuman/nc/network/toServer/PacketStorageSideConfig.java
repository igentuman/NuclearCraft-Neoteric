package igentuman.nc.network.toServer;

import igentuman.nc.block.ISizeToggable;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class PacketStorageSideConfig implements INcPacket {

    private BlockPos tilePosition;
    private int direction;

    public PacketStorageSideConfig(Object position, int direction) {
        this.tilePosition = (BlockPos) position;
        this.direction = direction;
    }

    public PacketStorageSideConfig() {

    }


    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        BlockEntity be = player.level.getBlockEntity(tilePosition);
        if(!(be instanceof ISizeToggable storage)) {
            return;
        }
        ISizeToggable.SideMode mode = storage.toggleSideConfig(direction);
        player.sendSystemMessage(Component.translatable("message.nc.barrel.side_config", mode.name()));
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(direction);
    }

    public static PacketStorageSideConfig decode(FriendlyByteBuf buffer) {
         PacketStorageSideConfig packet = new PacketStorageSideConfig();
          packet.tilePosition = buffer.readBlockPos();
          packet.direction = buffer.readInt();
          return packet;
    }

}