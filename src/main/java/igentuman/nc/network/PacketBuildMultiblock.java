package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.MultiblockBuilderBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;

public record PacketBuildMultiblock(BlockPos pos, CompoundTag blockMapTag) implements CustomPacketPayload {

    public static final Type<PacketBuildMultiblock> TYPE =
            new Type<>(NuclearCraft.rl("build_multiblock"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketBuildMultiblock> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketBuildMultiblock::pos,
                    ByteBufCodecs.COMPOUND_TAG, PacketBuildMultiblock::blockMapTag,
                    PacketBuildMultiblock::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static CompoundTag writeBlockMap(HashMap<BlockPos, Block> blockMap) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (var entry : blockMap.entrySet()) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(entry.getValue());
            if (id == null) continue;
            CompoundTag cell = new CompoundTag();
            cell.putLong("pos", entry.getKey().asLong());
            cell.putString("block", id.toString());
            list.add(cell);
        }
        tag.put("blocks", list);
        return tag;
    }

    public static HashMap<BlockPos, Block> readBlockMap(CompoundTag tag) {
        HashMap<BlockPos, Block> map = new HashMap<>();
        ListTag list = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag cell = list.getCompound(i);
            Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(cell.getString("block")));
            if (block != null) {
                map.put(BlockPos.of(cell.getLong("pos")), block);
            }
        }
        return map;
    }

    public static void handle(PacketBuildMultiblock packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            BlockEntity be = player.serverLevel().getBlockEntity(packet.pos());
            if (be instanceof MultiblockBuilderBE builder) {
                builder.build(readBlockMap(packet.blockMapTag()), player);
            }
        });
    }
}
