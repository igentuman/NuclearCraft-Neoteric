package igentuman.nc.network.toServer;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.crafter.entity.EngineersCrafterBE;
import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.handler.crafter.AggregatedInventory.ItemKey;
import igentuman.nc.handler.crafter.AutoCraftSolver;
import igentuman.nc.network.INcPacket;
import igentuman.nc.network.toClient.PacketCraftDenied;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Requests an autocraft of {@code qty} of the item identified by {@code target}. The server plans
 * the craft against the current container stock and encoded patterns. On failure it replies with a
 * {@link PacketCraftDenied} shortage report; a feasible plan is validated but not yet executed
 * (execution is the crafting-job phase). The target only selects <em>which</em> item to plan, so a
 * spoofed packet cannot conjure materials.
 */
public class PacketTerminalCraft implements INcPacket {

    public static final int MAX_QTY = 100_000;

    private BlockPos pos;
    private ItemStack target;
    private int qty;

    public PacketTerminalCraft() {
    }

    public PacketTerminalCraft(BlockPos pos, ItemStack target, int qty) {
        this.pos = pos;
        this.target = target;
        this.qty = qty;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        if (!(player.containerMenu instanceof EngineersCrafterContainer menu)) return;
        if (!menu.getBlockPos().equals(pos)) return;
        if (!menu.stillValid(player)) return;
        if (target == null || target.isEmpty()) return;

        int amount = Math.max(1, Math.min(qty, MAX_QTY));
        EngineersCrafterBE be = menu.blockEntity;
        AutoCraftSolver.Result<ItemKey> result = be.planCraft(target, amount);
        if (result.feasible()) {
            // Hand the plan to the crafting-job state machine (rejected silently if one already runs).
            be.startJob(target, amount, result.plan());
            return;
        }

        List<ItemStack> items = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();
        for (Map.Entry<ItemKey, Integer> e : result.shortages().entrySet()) {
            ItemStack sample = new ItemStack(e.getKey().item());
            if (e.getKey().tag() != null) sample.setTag(e.getKey().tag().copy());
            items.add(sample);
            amounts.add(e.getValue());
        }
        NuclearCraft.packetHandler().sendTo(new PacketCraftDenied(items, amounts, result.tooComplex()), player);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeItem(target);
        buffer.writeVarInt(qty);
    }

    public static PacketTerminalCraft decode(FriendlyByteBuf buffer) {
        PacketTerminalCraft packet = new PacketTerminalCraft();
        packet.pos = buffer.readBlockPos();
        packet.target = buffer.readItem();
        packet.qty = buffer.readVarInt();
        return packet;
    }
}
