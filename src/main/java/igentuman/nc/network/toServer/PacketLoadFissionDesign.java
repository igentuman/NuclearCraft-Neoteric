package igentuman.nc.network.toServer;

import igentuman.nc.NuclearCraft;
import igentuman.nc.item.FissionReactorPlanItem;
import igentuman.nc.network.INcPacket;
import igentuman.nc.network.toClient.PacketFissionDesignData;
import igentuman.nc.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketLoadFissionDesign implements INcPacket {

    private int paperSlot;

    public PacketLoadFissionDesign() {
    }

    public PacketLoadFissionDesign(int paperSlot) {
        this.paperSlot = paperSlot;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        ItemStack stack = player.getInventory().getItem(paperSlot);
        if (stack.isEmpty() || !(stack.getItem() instanceof FissionReactorPlanItem)) {
            return;
        }
        CompoundTag tag = ItemDataUtils.getCompound(stack, FissionReactorPlanItem.DESIGN_KEY);
        if (tag.isEmpty()) {
            return;
        }
        NuclearCraft.packetHandler().sendTo(new PacketFissionDesignData(tag), player);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(paperSlot);
    }

    public static PacketLoadFissionDesign decode(FriendlyByteBuf buffer) {
        return new PacketLoadFissionDesign(buffer.readVarInt());
    }
}
