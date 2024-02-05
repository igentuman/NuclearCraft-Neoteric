package igentuman.nc.network.toServer;

import igentuman.nc.block.ISizeToggable;
import igentuman.nc.block.entity.energy.BatteryBE;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class BatterySideConfig implements INcPacket {

    private BlockPos tilePosition;
    private int direction;

    public BatterySideConfig(Object position, int direction) {
        this.tilePosition = (BlockPos) position;
        this.direction = direction;
    }

    public BatterySideConfig() {

    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        BlockEntity be = player.level.getBlockEntity(tilePosition);
        if(!(be instanceof BatteryBE battery)) {
            return;
        }
        ISizeToggable.SideMode mode = battery.toggleSideConfig(direction);
        player.sendMessage(new TranslatableComponent("message.nc.battery.side_config", mode.name()), UUID.randomUUID());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(direction);
    }

    public static BatterySideConfig decode(FriendlyByteBuf buffer) {
         BatterySideConfig packet = new BatterySideConfig();
          packet.tilePosition = buffer.readBlockPos();
          packet.direction = buffer.readInt();
          return packet;
    }

}