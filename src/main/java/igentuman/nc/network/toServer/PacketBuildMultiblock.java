
package igentuman.nc.network.toServer;

import igentuman.nc.block.entity.MultiblockBuilderBE;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;

public class PacketBuildMultiblock implements INcPacket {

    private BlockPos tilePosition;
    private HashMap<BlockPos, Block> blockMap = new HashMap<>();

    public PacketBuildMultiblock(Object position, HashMap<BlockPos, Block> blockMap) {
        this.tilePosition = (BlockPos) position;
        this.blockMap = blockMap;
    }

    public PacketBuildMultiblock() {

    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        BlockEntity be = player.level.getBlockEntity(tilePosition);
        if((be instanceof MultiblockBuilderBE ncBe)) {
            ncBe.build(blockMap, player);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(blockMap.size());
        for (HashMap.Entry<BlockPos, Block> entry : blockMap.entrySet()) {
            buffer.writeBlockPos(entry.getKey());
            buffer.writeInt(Block.getId(entry.getValue().defaultBlockState()));
        }
    }

    public static PacketBuildMultiblock decode(FriendlyByteBuf buffer) {
         PacketBuildMultiblock packet = new PacketBuildMultiblock();
          packet.tilePosition = buffer.readBlockPos();
          int size = buffer.readInt();
          for(int i = 0; i < size; i++) {
              BlockPos pos = buffer.readBlockPos();
              Block block = Block.stateById(buffer.readInt()).getBlock();
              packet.blockMap.put(pos, block);
          }
          return packet;
    }
}