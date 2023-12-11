package igentuman.nc.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

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

    public NCProcessorPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.id = buf.readInt();
        this.val = buf.readByte();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(id);
        buf.writeByte(val);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            ServerPlayer player = context.getSender();
            BlockEntity be = player.level().getBlockEntity(pos);

        });
        return true;
    }
}
