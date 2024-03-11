package igentuman.nc.network.toServer;

import igentuman.nc.block.ISizeToggable;
import igentuman.nc.block.entity.energy.BatteryBE;
import igentuman.nc.network.INcPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

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
        ServerPlayerEntity player = context.getSender();
        if (player == null) {
            return;
        }
        TileEntity be = player.level.getBlockEntity(tilePosition);
        if(!(be instanceof BatteryBE)) {
            return;
        }
        ISizeToggable.SideMode mode = ((BatteryBE) be).toggleSideConfig(direction);
        player.sendMessage(new TranslationTextComponent("message.nc.battery.side_config", mode.name()), UUID.randomUUID());
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(direction);
    }

    public static BatterySideConfig decode(PacketBuffer buffer) {
         BatterySideConfig packet = new BatterySideConfig();
          packet.tilePosition = buffer.readBlockPos();
          packet.direction = buffer.readInt();
          return packet;
    }

}