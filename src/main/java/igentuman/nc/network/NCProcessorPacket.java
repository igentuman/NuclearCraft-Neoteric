package igentuman.nc.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class NCProcessorPacket {

    private BlockPos pos;
    private int id;
    private byte val;

    public NCProcessorPacket(BlockPos pos, int id, byte val)
    {
        this.pos = pos;
        this.id = id;
        this.val = val;
    }

    public NCProcessorPacket(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.id = buf.readInt();
        this.val = buf.readByte();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(id);
        buf.writeByte(val);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            ServerPlayerEntity player = context.getSender();
            TileEntity be = player.level.getBlockEntity(pos);

        });
        return true;
    }
}
