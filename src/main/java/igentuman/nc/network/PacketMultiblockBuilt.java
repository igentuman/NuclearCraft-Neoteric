package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.MultiblockBuilderBE;
import igentuman.nc.screen.MultiblockBuilderScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;

public record PacketMultiblockBuilt(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PacketMultiblockBuilt> TYPE =
            new Type<>(NuclearCraft.rl("multiblock_built"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketMultiblockBuilt> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketMultiblockBuilt::pos,
                    PacketMultiblockBuilt::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketMultiblockBuilt packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            BlockEntity be = Minecraft.getInstance().level != null
                    ? Minecraft.getInstance().level.getBlockEntity(packet.pos())
                    : null;
            if (be instanceof MultiblockBuilderBE builder) {
                builder.blockMap = new HashMap<>();
            }
            if (Minecraft.getInstance().screen instanceof MultiblockBuilderScreen screen
                    && screen.getMenu().getPosition().equals(packet.pos())) {
                screen.blockMap = new HashMap<>();
            }
        });
    }
}
