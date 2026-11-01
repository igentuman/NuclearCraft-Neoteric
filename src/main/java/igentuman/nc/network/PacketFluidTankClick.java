package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.container.MultiblockPortContainer;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.handler.fluid.ContainerFluidHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketFluidTankClick(int containerId, int tank, boolean flush) implements CustomPacketPayload {

    public static final Type<PacketFluidTankClick> TYPE = new Type<>(NuclearCraft.rl("fluid_tank_click"));
    public static final StreamCodec<FriendlyByteBuf, PacketFluidTankClick> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacketFluidTankClick::containerId,
            ByteBufCodecs.VAR_INT, PacketFluidTankClick::tank,
            ByteBufCodecs.BOOL, PacketFluidTankClick::flush,
            PacketFluidTankClick::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFluidTankClick packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
            var menu = player.containerMenu;
            if (menu.containerId != packet.containerId() || !menu.stillValid(player)) return;

            GlobalBlockEntity be;
            if (menu instanceof UniversalProcessorContainer processor) {
                be = processor.getBlockEntity();
            } else if (menu instanceof MultiblockControllerContainer controller) {
                be = controller.getBlockEntity();
            } else if (menu instanceof MultiblockPortContainer port) {
                if (port.getBlockEntity().isRemoved()) return;
                be = port.getBlockEntity().controller();
            } else {
                return;
            }
            if (be == null || be.isRemoved() || be.getLevel() != player.level()) return;
            var fluids = be.contentHandler.getFluidHandler();
            if (fluids == null || packet.tank() < 0 || packet.tank() >= fluids.getTanks()) return;

            var carried = menu.getCarried();
            if (packet.flush()) {
                if (!carried.isEmpty()) return;
                fluids.getInternalHandler().voidTank(packet.tank());
            } else {
                var tank = new ContainerFluidHandler(fluids, packet.tank());
                var inventory = new PlayerMainInvWrapper(player.getInventory());
                FluidActionResult result = FluidUtil.tryEmptyContainerAndStow(
                        carried, tank, inventory, Integer.MAX_VALUE, player, true);
                if (!result.isSuccess()) {
                    result = FluidUtil.tryFillContainerAndStow(
                            carried, tank, inventory, Integer.MAX_VALUE, player, true);
                }
                if (!result.isSuccess()) return;
                menu.setCarried(result.getResult());
            }
            be.setChanged();
            be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
            menu.broadcastChanges();
        });
    }
}
