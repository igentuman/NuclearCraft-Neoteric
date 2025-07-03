
package igentuman.nc.network.toServer;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.block.entity.turbine.TurbinePortBE;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class PacketHandleFluidSlotClick implements INcPacket {

    private BlockPos tilePosition;
    private int slotId;
    private ItemStack fluidStackHandler;


    public PacketHandleFluidSlotClick() {

    }

    public PacketHandleFluidSlotClick(BlockPos position, int slotId, ItemStack carried) {
        this.tilePosition = position;
        this.slotId = slotId;
        this.fluidStackHandler = carried;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        BlockEntity be = player.level().getBlockEntity(tilePosition);
        if((be instanceof NCProcessorBE ncBe)) {
            ncBe.handleFluidItemClick(slotId, fluidStackHandler, player);
            return;
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(slotId);
        buffer.writeItemStack(fluidStackHandler, false);
    }

    public static PacketHandleFluidSlotClick decode(FriendlyByteBuf buffer) {
        PacketHandleFluidSlotClick packet = new PacketHandleFluidSlotClick();
        packet.tilePosition = buffer.readBlockPos();
        packet.slotId = buffer.readInt();
        packet.fluidStackHandler = buffer.readItem();
        return packet;
    }
}