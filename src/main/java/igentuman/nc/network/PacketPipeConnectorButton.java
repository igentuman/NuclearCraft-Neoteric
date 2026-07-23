package igentuman.nc.network;

import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import igentuman.nc.container.PipeConnectorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.block_entity.pipe.PipeConnectorBE.CAP_ENERGY;
import static igentuman.nc.block_entity.pipe.PipeConnectorBE.CAP_FLUID;
import static igentuman.nc.block_entity.pipe.PipeConnectorBE.CAP_ITEM;

public record PacketPipeConnectorButton(BlockPos pos, int buttonId) implements CustomPacketPayload {

    public static final Type<PacketPipeConnectorButton> TYPE =
            new Type<>(rl("pipe_connector_button"));

    public static final StreamCodec<FriendlyByteBuf, PacketPipeConnectorButton> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketPipeConnectorButton::pos,
                    ByteBufCodecs.INT, PacketPipeConnectorButton::buttonId,
                    PacketPipeConnectorButton::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketPipeConnectorButton packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            BlockEntity be = serverPlayer.serverLevel().getBlockEntity(packet.pos());
            if (!(be instanceof PipeConnectorBE connector)) return;
            switch (packet.buttonId()) {
                case PipeConnectorContainer.BTN_MODE -> connector.cycleMode();
                case PipeConnectorContainer.BTN_REDSTONE -> connector.cycleRedstoneMode();
                case PipeConnectorContainer.BTN_CAP_ITEM -> connector.toggleCapability(CAP_ITEM);
                case PipeConnectorContainer.BTN_CAP_FLUID -> connector.toggleCapability(CAP_FLUID);
                case PipeConnectorContainer.BTN_CAP_ENERGY -> connector.toggleCapability(CAP_ENERGY);
                default -> { }
            }
        });
    }
}
