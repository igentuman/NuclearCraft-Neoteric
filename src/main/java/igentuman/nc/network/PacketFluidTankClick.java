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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

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
                if (carried.isEmpty()) return;
                var tank = new ContainerFluidHandler(fluids, packet.tank());
                ItemAccess access;
                if (player.hasInfiniteMaterials()) {
                    // Let buckets change item type in a temporary slot while retaining the creative cursor.
                    var temporarySlot = new ItemStacksResourceHandler(1);
                    temporarySlot.set(0, ItemResource.of(carried), 1);
                    access = ItemAccess.forHandlerIndexStrict(temporarySlot, 0);
                } else {
                    access = ItemAccess.forPlayerCursor(player, menu).oneByOne();
                }
                var item = access.getCapability(Capabilities.Fluid.ITEM);
                if (item == null) return;
                boolean pickup = false;
                ResourceStack<FluidResource> moved;
                try (var transaction = Transaction.openRoot()) {
                    moved = ResourceHandlerUtil.moveFirst(item, tank, resource -> true, Integer.MAX_VALUE, transaction);
                    if (moved == null) {
                        pickup = true;
                        moved = ResourceHandlerUtil.moveFirst(tank, item, resource -> true, Integer.MAX_VALUE, transaction);
                    }
                    if (moved == null) return;
                    transaction.commit();
                }
                FluidUtil.triggerSoundAndGameEvent(moved.resource(), player.level(),
                        Vec3.atCenterOf(be.getBlockPos()), player, pickup);
            }
            be.setChanged();
            be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
            menu.broadcastChanges();
        });
    }
}
