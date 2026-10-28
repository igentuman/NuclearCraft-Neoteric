package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.crafter.PendingCraftDenied;
import igentuman.nc.handler.crafter.CraftShortage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PacketCraftDenied(List<CraftShortage<ItemStack>> items,
                                List<CraftShortage<FluidStack>> fluids,
                                boolean tooComplex)
        implements CustomPacketPayload {

    public static final Type<PacketCraftDenied> TYPE = new Type<>(NuclearCraft.rl("craft_denied"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketCraftDenied> STREAM_CODEC =
            StreamCodec.of(PacketCraftDenied::encode, PacketCraftDenied::decode);

    public PacketCraftDenied {
        items = List.copyOf(items);
        fluids = List.copyOf(fluids);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(RegistryFriendlyByteBuf buf, PacketCraftDenied pkt) {
        buf.writeBoolean(pkt.tooComplex);
        buf.writeVarInt(pkt.items.size());
        for (CraftShortage<ItemStack> shortage : pkt.items) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, shortage.stack());
            buf.writeVarInt(shortage.amount());
        }
        buf.writeVarInt(pkt.fluids.size());
        for (CraftShortage<FluidStack> shortage : pkt.fluids) {
            FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, shortage.stack());
            buf.writeVarInt(shortage.amount());
        }
    }

    private static PacketCraftDenied decode(RegistryFriendlyByteBuf buf) {
        boolean tooComplex = buf.readBoolean();
        int n = buf.readVarInt();
        List<CraftShortage<ItemStack>> items = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            items.add(new CraftShortage<>(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), buf.readVarInt()));
        }
        int f = buf.readVarInt();
        List<CraftShortage<FluidStack>> fluids = new ArrayList<>(f);
        for (int i = 0; i < f; i++) {
            fluids.add(new CraftShortage<>(FluidStack.OPTIONAL_STREAM_CODEC.decode(buf), buf.readVarInt()));
        }
        return new PacketCraftDenied(items, fluids, tooComplex);
    }

    public static void handle(PacketCraftDenied packet, IPayloadContext context) {
        context.enqueueWork(() -> PendingCraftDenied.set(packet));
    }
}
