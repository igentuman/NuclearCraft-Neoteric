package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.handler.crafter.AggregatedInventory.ItemKey;
import igentuman.nc.handler.crafter.AutoCraftSolver;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record PacketTerminalCraft(BlockPos pos, ItemStack target, int qty) implements CustomPacketPayload {

    public static final int MAX_QTY = 100_000;

    public static final Type<PacketTerminalCraft> TYPE = new Type<>(NuclearCraft.rl("terminal_craft"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketTerminalCraft> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketTerminalCraft::pos,
                    ItemStack.OPTIONAL_STREAM_CODEC, PacketTerminalCraft::target,
                    ByteBufCodecs.VAR_INT, PacketTerminalCraft::qty,
                    PacketTerminalCraft::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketTerminalCraft packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof EngineersCrafterContainer menu)) return;
            if (!menu.getBlockPos().equals(packet.pos())) return;
            if (!menu.stillValid(player)) return;
            if (packet.target() == null || packet.target().isEmpty()) return;

            int amount = Math.max(1, Math.min(packet.qty(), MAX_QTY));
            EngineersCrafterBE be = menu.blockEntity;
            AutoCraftSolver.Result<ItemKey> result = be.planCraft(packet.target(), amount);
            if (result.feasible()) {
                be.startJob(packet.target(), amount, result.plan());
                return;
            }

            List<ItemStack> items = new ArrayList<>();
            List<Integer> amounts = new ArrayList<>();
            for (Map.Entry<ItemKey, Integer> e : result.shortages().entrySet()) {
                items.add(e.getKey().sample());
                amounts.add(e.getValue());
            }
            PacketDistributor.sendToPlayer(player, new PacketCraftDenied(items, amounts, result.tooComplex()));
        });
    }
}
