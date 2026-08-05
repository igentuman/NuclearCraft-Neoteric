package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.item.FissionReactorPlanItem;
import igentuman.nc.setup.ModEntries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSaveFissionDesign(CompoundTag designTag, String fuelKey, String variant) implements CustomPacketPayload {

    public static final Type<PacketSaveFissionDesign> TYPE =
            new Type<>(NuclearCraft.rl("save_fission_design"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSaveFissionDesign> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG, PacketSaveFissionDesign::designTag,
                    ByteBufCodecs.STRING_UTF8, PacketSaveFissionDesign::fuelKey,
                    ByteBufCodecs.STRING_UTF8, PacketSaveFissionDesign::variant,
                    PacketSaveFissionDesign::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSaveFissionDesign packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            CompoundTag design = packet.designTag();
            if (design == null || design.isEmpty()) return;
            if (packet.fuelKey() != null && !packet.fuelKey().isEmpty()) {
                design.putString(FissionReactorPlanItem.FUEL_KEY, packet.fuelKey());
                design.putString(FissionReactorPlanItem.VARIANT_KEY, packet.variant() == null ? "" : packet.variant());
            }
            ItemStack plan = new ItemStack(ModEntries.get("fission_reactor_plan").item().get());
            FissionReactorPlanItem.setDesign(plan, design);
            if (!player.getInventory().add(plan)) {
                player.drop(plan, false);
            }
        });
    }
}
