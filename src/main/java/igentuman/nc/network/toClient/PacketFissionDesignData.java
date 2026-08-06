package igentuman.nc.network.toClient;

import igentuman.nc.client.gui.MultiblockBuilderScreen;
import igentuman.nc.client.gui.fission.FissionDesignerScreen;
import igentuman.nc.client.gui.fission.designer.DesignGrid;
import igentuman.nc.item.FissionReactorPlanItem;
import igentuman.nc.network.INcPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;

public class PacketFissionDesignData implements INcPacket {

    private CompoundTag designTag;

    public PacketFissionDesignData() {
    }

    public PacketFissionDesignData(CompoundTag designTag) {
        this.designTag = designTag;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if (designTag == null || designTag.isEmpty()) {
                return;
            }
            if (Minecraft.getInstance().screen instanceof FissionDesignerScreen screen) {
                List<String> fuelKey = FissionReactorPlanItem.fuelKeyFromTag(designTag);
                screen.applyLoadedDesign(DesignGrid.fromTag(designTag), fuelKey);
            } else if (Minecraft.getInstance().screen instanceof MultiblockBuilderScreen screen) {
                screen.applyLoadedDesign(DesignGrid.fromTag(designTag));
            }
        });
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeNbt(designTag);
    }

    public static PacketFissionDesignData decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return new PacketFissionDesignData(tag != null ? tag : new CompoundTag());
    }
}
