package igentuman.nc.network.toServer;

import igentuman.nc.compat.mbtool.MbtoolHelper;
import igentuman.nc.network.INcPacket;
import igentuman.nc.util.ModUtil;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import static igentuman.nc.util.TextUtils.__;

public class PacketLoadDesignIntoMultitool implements INcPacket {

    private CompoundTag structureNbt;

    public PacketLoadDesignIntoMultitool() {
    }

    public PacketLoadDesignIntoMultitool(CompoundTag structureNbt) {
        this.structureNbt = structureNbt;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || !ModUtil.isMbtoolLoaded()) {
            return;
        }
        boolean loaded = MbtoolHelper.loadDesign(player, structureNbt);
        if (!loaded) {
            player.sendSystemMessage(TextUtils.applyFormat(
                    __("nc.fission_designer.no_multitool"), ChatFormatting.RED));
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeNbt(structureNbt);
    }

    public static PacketLoadDesignIntoMultitool decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return new PacketLoadDesignIntoMultitool(tag != null ? tag : new CompoundTag());
    }
}
