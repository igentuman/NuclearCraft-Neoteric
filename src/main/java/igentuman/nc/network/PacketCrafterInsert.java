package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.handler.crafter.AggregatedInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketCrafterInsert(BlockPos pos, boolean single) implements CustomPacketPayload {

    public static final Type<PacketCrafterInsert> TYPE = new Type<>(NuclearCraft.rl("crafter_insert"));

    public static final StreamCodec<FriendlyByteBuf, PacketCrafterInsert> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketCrafterInsert::pos,
                    ByteBufCodecs.BOOL, PacketCrafterInsert::single,
                    PacketCrafterInsert::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketCrafterInsert packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof EngineersCrafterContainer menu)) return;
            if (!menu.getBlockPos().equals(packet.pos())) return;
            if (!menu.stillValid(player)) return;

            EngineersCrafterBE be = menu.blockEntity;
            ItemStack carried = menu.getCarried();
            if (carried.isEmpty()) return;

            AggregatedInventory agg = new AggregatedInventory(be.containerSlots);
            boolean changed = false;
            if (packet.single()) {
                ItemStack one = carried.copy();
                one.setCount(1);
                if (agg.insert(one, false).isEmpty()) {
                    carried.shrink(1);
                    menu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
                    changed = true;
                }
            } else {
                int before = carried.getCount();
                ItemStack leftover = agg.insert(carried, false);
                if (leftover.getCount() != before) {
                    menu.setCarried(leftover);
                    changed = true;
                }
            }

            if (changed) {
                be.markUpdated();
                menu.broadcastChanges();
            }
        });
    }
}
