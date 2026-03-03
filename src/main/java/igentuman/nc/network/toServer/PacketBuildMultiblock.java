package igentuman.nc.network.toServer;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockBuilderBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;

public class PacketBuildMultiblock implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketBuildMultiblock> TYPE =
        new CustomPacketPayload.Type<>(NuclearCraft.rl("build_multiblock"));

    public static final StreamCodec<FriendlyByteBuf, PacketBuildMultiblock> STREAM_CODEC =
        StreamCodec.of((buf, pkt) -> pkt.encode(buf), PacketBuildMultiblock::decode);

    private BlockPos tilePosition;
    private HashMap<BlockPos, Block> blockMap = new HashMap<>();

    public PacketBuildMultiblock(Object position, HashMap<BlockPos, Block> blockMap) {
        this.tilePosition = (BlockPos) position;
        this.blockMap = blockMap;
    }

    public PacketBuildMultiblock() {

    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketBuildMultiblock packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.handlePacket(context));
    }

    private void handlePacket(IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player == null) {
            return;
        }
        BlockEntity be = player.level().getBlockEntity(tilePosition);
        if((be instanceof MultiblockBuilderBE ncBe)) {
            ncBe.build(blockMap);
        }
    }

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
