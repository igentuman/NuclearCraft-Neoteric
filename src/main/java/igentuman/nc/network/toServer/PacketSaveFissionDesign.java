package igentuman.nc.network.toServer;

import igentuman.nc.item.FissionReactorPlanItem;
import igentuman.nc.network.INcPacket;
import igentuman.nc.setup.registration.NCItems;
import igentuman.nc.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class PacketSaveFissionDesign implements INcPacket {

    private CompoundTag designTag;
    private List<String> fuelKey;

    public PacketSaveFissionDesign() {
    }

    public PacketSaveFissionDesign(CompoundTag designTag, List<String> fuelKey) {
        this.designTag = designTag;
        this.fuelKey = fuelKey;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || designTag == null || designTag.isEmpty()) {
            return;
        }
        if (fuelKey != null && !fuelKey.isEmpty()) {
            designTag.put(FissionReactorPlanItem.FUEL_KEY, FissionReactorPlanItem.fuelKeyToTag(fuelKey));
        }
        ItemStack plan = new ItemStack(NCItems.FISSION_REACTOR_PLAN.get());
        ItemDataUtils.setCompound(plan, FissionReactorPlanItem.DESIGN_KEY, designTag);
        if (!player.getInventory().add(plan)) {
            player.drop(plan, false);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeNbt(designTag);
        if (fuelKey == null) {
            buffer.writeVarInt(0);
        } else {
            buffer.writeVarInt(fuelKey.size());
            for (String s : fuelKey) {
                buffer.writeUtf(s);
            }
        }
    }

    public static PacketSaveFissionDesign decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        int n = buffer.readVarInt();
        List<String> key = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            key.add(buffer.readUtf());
        }
        return new PacketSaveFissionDesign(tag != null ? tag : new CompoundTag(), key);
    }
}
