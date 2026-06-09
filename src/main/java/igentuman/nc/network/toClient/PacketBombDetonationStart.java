package igentuman.nc.network.toClient;

import igentuman.nc.client.bomb.BombFxManager;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class PacketBombDetonationStart implements INcPacket {

    private final int id;
    private final BlockPos epicenter;
    private final float yield;

    public PacketBombDetonationStart(int id, BlockPos epicenter, float yield) {
        this.id = id;
        this.epicenter = epicenter;
        this.yield = yield;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BombFxManager.onDetonationStart(id, epicenter, yield));
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(id);
        buffer.writeBlockPos(epicenter);
        buffer.writeFloat(yield);
    }

    public static PacketBombDetonationStart decode(FriendlyByteBuf buffer) {
        int id = buffer.readInt();
        BlockPos epicenter = buffer.readBlockPos();
        float yield = buffer.readFloat();
        return new PacketBombDetonationStart(id, epicenter, yield);
    }
}
