package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.handler.crafter.AggregatedInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketCrafterExtract(BlockPos pos, ItemStack sample, boolean single, boolean toInventory)
        implements CustomPacketPayload {

    public static final Type<PacketCrafterExtract> TYPE = new Type<>(NuclearCraft.rl("crafter_extract"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketCrafterExtract> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketCrafterExtract::pos,
                    ItemStack.OPTIONAL_STREAM_CODEC, PacketCrafterExtract::sample,
                    ByteBufCodecs.BOOL, PacketCrafterExtract::single,
                    ByteBufCodecs.BOOL, PacketCrafterExtract::toInventory,
                    PacketCrafterExtract::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketCrafterExtract packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof EngineersCrafterContainer menu)) return;
            if (!menu.getBlockPos().equals(packet.pos())) return;
            if (!menu.stillValid(player)) return;
            if (packet.sample() == null || packet.sample().isEmpty()) return;

            EngineersCrafterBE be = menu.blockEntity;
            AggregatedInventory agg = new AggregatedInventory(be.containerSlots);
            int amount = packet.single() ? 1 : packet.sample().getMaxStackSize();
            ItemStack got = agg.extract(packet.sample(), amount, false);
            if (got.isEmpty()) return;

            if (packet.toInventory()) {
                player.getInventory().add(got);
                if (!got.isEmpty()) {
                    ItemStack back = agg.insert(got, false);
                    if (!back.isEmpty()) player.drop(back, false);
                }
            } else {
                ItemStack cur = menu.getCarried();
                if (cur.isEmpty()) {
                    menu.setCarried(got);
                } else if (ItemStack.isSameItemSameComponents(cur, got)
                        && cur.getCount() + got.getCount() <= cur.getMaxStackSize()) {
                    cur.grow(got.getCount());
                    menu.setCarried(cur);
                } else {
                    ItemStack back = agg.insert(got, false);
                    if (!back.isEmpty()) player.drop(back, false);
                }
            }

            be.markUpdated();
            menu.broadcastChanges();
        });
    }
}
