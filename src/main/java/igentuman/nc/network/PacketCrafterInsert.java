package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import igentuman.nc.container.EngineersCrafterContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.CarriedSlotWrapper;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

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

            var cursor = CarriedSlotWrapper.of(menu);
            var resource = ItemResource.of(carried);
            int amount = packet.single() ? 1 : carried.getCount();
            try (var transaction = Transaction.openRoot()) {
                int moved = ResourceHandlerUtil.move(cursor, be.getItemHandler(null),
                        resource::equals, amount, transaction);
                if (moved == 0) return;
                transaction.commit();
            }
            be.markUpdated();
            menu.broadcastChanges();
        });
    }
}
