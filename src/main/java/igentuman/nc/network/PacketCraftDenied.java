package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.crafter.PendingCraftDenied;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PacketCraftDenied(List<ItemStack> items, List<Integer> amounts,
                                List<FluidStack> fluids, List<Integer> fluidAmounts,
                                boolean tooComplex)
        implements CustomPacketPayload {

    public static final Type<PacketCraftDenied> TYPE = new Type<>(NuclearCraft.rl("craft_denied"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketCraftDenied> STREAM_CODEC =
            StreamCodec.of(PacketCraftDenied::encode, PacketCraftDenied::decode);

    public PacketCraftDenied(List<ItemStack> items, List<Integer> amounts, boolean tooComplex) {
        this(items, amounts, List.of(), List.of(), tooComplex);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(RegistryFriendlyByteBuf buf, PacketCraftDenied pkt) {
        buf.writeBoolean(pkt.tooComplex);
        buf.writeVarInt(pkt.items.size());
        for (int i = 0; i < pkt.items.size(); i++) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, pkt.items.get(i));
            buf.writeVarInt(pkt.amounts.get(i));
        }
        buf.writeVarInt(pkt.fluids.size());
        for (int i = 0; i < pkt.fluids.size(); i++) {
            FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, pkt.fluids.get(i));
            buf.writeVarInt(pkt.fluidAmounts.get(i));
        }
    }

    private static PacketCraftDenied decode(RegistryFriendlyByteBuf buf) {
        boolean tooComplex = buf.readBoolean();
        int n = buf.readVarInt();
        List<ItemStack> items = new ArrayList<>(n);
        List<Integer> amounts = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            amounts.add(buf.readVarInt());
        }
        int f = buf.readVarInt();
        List<FluidStack> fluids = new ArrayList<>(f);
        List<Integer> fluidAmounts = new ArrayList<>(f);
        for (int i = 0; i < f; i++) {
            fluids.add(FluidStack.OPTIONAL_STREAM_CODEC.decode(buf));
            fluidAmounts.add(buf.readVarInt());
        }
        return new PacketCraftDenied(items, amounts, fluids, fluidAmounts, tooComplex);
    }

    public static void handle(PacketCraftDenied packet, IPayloadContext context) {
        context.enqueueWork(() -> PendingCraftDenied.set(packet.items(), packet.amounts(),
                packet.fluids(), packet.fluidAmounts(), packet.tooComplex()));
    }
}
