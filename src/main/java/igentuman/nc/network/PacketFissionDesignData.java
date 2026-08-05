package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.gui.fission.designer.DesignGrid;
import igentuman.nc.item.FissionReactorPlanItem;
import igentuman.nc.screen.FissionDesignerScreen;
import igentuman.nc.screen.MultiblockBuilderScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketFissionDesignData(CompoundTag designTag) implements CustomPacketPayload {

    public static final Type<PacketFissionDesignData> TYPE =
            new Type<>(NuclearCraft.rl("fission_design_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketFissionDesignData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG, PacketFissionDesignData::designTag,
                    PacketFissionDesignData::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFissionDesignData packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CompoundTag design = packet.designTag();
            if (design == null || design.isEmpty()) return;
            if (Minecraft.getInstance().screen instanceof FissionDesignerScreen screen) {
                screen.applyLoadedDesign(DesignGrid.fromTag(design),
                        design.getString(FissionReactorPlanItem.FUEL_KEY),
                        design.getString(FissionReactorPlanItem.VARIANT_KEY));
            } else if (Minecraft.getInstance().screen instanceof MultiblockBuilderScreen screen) {
                screen.applyLoadedDesign(DesignGrid.fromTag(design));
            }
        });
    }
}
