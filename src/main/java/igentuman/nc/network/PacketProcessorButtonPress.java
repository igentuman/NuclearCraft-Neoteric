package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.UniversalProcessorBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server payload delivering a processor GUI button press, such as the redstone toggle. */
public record PacketProcessorButtonPress(BlockPos pos, int buttonId)
        implements CustomPacketPayload {

    public static final int REDSTONE_BTN_ID = 70;

    public static final Type<PacketProcessorButtonPress> TYPE =
            new Type<>(NuclearCraft.rl("processor_button_press"));

    public static final StreamCodec<FriendlyByteBuf, PacketProcessorButtonPress> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketProcessorButtonPress::pos,
                    net.minecraft.network.codec.ByteBufCodecs.INT, PacketProcessorButtonPress::buttonId,
                    PacketProcessorButtonPress::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketProcessorButtonPress packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            BlockEntity be = serverPlayer.serverLevel().getBlockEntity(packet.pos());
            if (packet.buttonId() == REDSTONE_BTN_ID && be instanceof UniversalProcessorBE processor) {
                processor.toggleRedstoneMode();
                be.setChanged();
                serverPlayer.serverLevel().sendBlockUpdated(packet.pos(), be.getBlockState(), be.getBlockState(), 3);
            }
        });
    }
}
