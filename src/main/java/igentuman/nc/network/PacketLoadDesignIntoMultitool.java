package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.mbtool.MbtoolHelper;
import igentuman.nc.util.ModUtil;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static igentuman.nc.util.TextUtils.__;

public record PacketLoadDesignIntoMultitool(CompoundTag structureNbt) implements CustomPacketPayload {

    public static final Type<PacketLoadDesignIntoMultitool> TYPE =
            new Type<>(NuclearCraft.rl("load_design_into_multitool"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketLoadDesignIntoMultitool> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG, PacketLoadDesignIntoMultitool::structureNbt,
                    PacketLoadDesignIntoMultitool::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketLoadDesignIntoMultitool packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!ModUtil.isMbtoolLoaded()) return;

            boolean loaded = MbtoolHelper.loadDesign(player, packet.structureNbt());
            if (!loaded) {
                player.sendSystemMessage(TextUtils.applyFormat(
                        __("nc.fission_designer.no_multitool"), ChatFormatting.RED));
            }
        });
    }
}
