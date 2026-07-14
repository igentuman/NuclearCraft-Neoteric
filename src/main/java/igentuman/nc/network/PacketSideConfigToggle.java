package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.handler.SidedContentHandler.RelativeDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server payload cycling the side-config I/O mode for a slot on a block entity's face. */
public record PacketSideConfigToggle(BlockPos pos, int slotId, int direction)
        implements CustomPacketPayload {

    public static final Type<PacketSideConfigToggle> TYPE =
            new Type<>(NuclearCraft.rl("side_config_toggle"));

    public static final StreamCodec<FriendlyByteBuf, PacketSideConfigToggle> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketSideConfigToggle::pos,
                    net.minecraft.network.codec.ByteBufCodecs.INT, PacketSideConfigToggle::slotId,
                    net.minecraft.network.codec.ByteBufCodecs.INT, PacketSideConfigToggle::direction,
                    PacketSideConfigToggle::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSideConfigToggle packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            ServerLevel level = serverPlayer.serverLevel();
            BlockEntity be = level.getBlockEntity(packet.pos());
            if (!(be instanceof GlobalBlockEntity gbe)) return;

            Direction facing;
            try {
                facing = gbe.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            } catch (Exception e) {
                facing = Direction.NORTH;
            }

            RelativeDirection relDir = RelativeDirection.values()[packet.direction()];
            Direction absoluteDir = RelativeDirection.toAbsolute(relDir, facing);

            gbe.contentHandler.toggleSideConfig(packet.slotId(), absoluteDir);
            gbe.setChanged();
            level.sendBlockUpdated(packet.pos(), gbe.getBlockState(), gbe.getBlockState(), 3);
        });
    }
}
