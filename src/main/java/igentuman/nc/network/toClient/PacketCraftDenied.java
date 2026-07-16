package igentuman.nc.network.toClient;

import igentuman.nc.client.gui.crafter.CraftDeniedScreen;
import igentuman.nc.network.INcPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Server reply when an autocraft request cannot be satisfied. Carries the missing base items (with
 * their true shortfall counts, not clamped to a stack) so the terminal can open the missing-items
 * screen. {@code tooComplex} marks a plan that blew past the solver's operation/depth caps.
 */
public class PacketCraftDenied implements INcPacket {

    private List<ItemStack> items;
    private List<Integer> amounts;
    private boolean tooComplex;

    public PacketCraftDenied() {
    }

    public PacketCraftDenied(List<ItemStack> items, List<Integer> amounts, boolean tooComplex) {
        this.items = items;
        this.amounts = amounts;
        this.tooComplex = tooComplex;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CraftDeniedScreen.open(items, amounts, tooComplex)));
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(tooComplex);
        buffer.writeVarInt(items.size());
        for (int i = 0; i < items.size(); i++) {
            buffer.writeItem(items.get(i));
            buffer.writeVarInt(amounts.get(i));
        }
    }

    public static PacketCraftDenied decode(FriendlyByteBuf buffer) {
        PacketCraftDenied packet = new PacketCraftDenied();
        packet.tooComplex = buffer.readBoolean();
        int n = buffer.readVarInt();
        packet.items = new ArrayList<>(n);
        packet.amounts = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            packet.items.add(buffer.readItem());
            packet.amounts.add(buffer.readVarInt());
        }
        return packet;
    }
}
