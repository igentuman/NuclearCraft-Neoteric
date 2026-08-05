package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.item.FissionReactorPlanItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketLoadFissionDesign(int paperSlot) implements CustomPacketPayload {

    public static final Type<PacketLoadFissionDesign> TYPE =
            new Type<>(NuclearCraft.rl("load_fission_design"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketLoadFissionDesign> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, PacketLoadFissionDesign::paperSlot,
                    PacketLoadFissionDesign::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketLoadFissionDesign packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ItemStack stack = player.getInventory().getItem(packet.paperSlot());
            if (stack.isEmpty() || !(stack.getItem() instanceof FissionReactorPlanItem)) return;
            CompoundTag design = FissionReactorPlanItem.getDesign(stack);
            if (design.isEmpty()) return;
            PacketDistributor.sendToPlayer(player, new PacketFissionDesignData(design));
        });
    }
}
