package igentuman.nc.network.toClient;

import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.network.INcPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

public record PacketEncoderSync(int containerId, int mode, @Nullable ResourceLocation recipeId) implements INcPacket {
    @Override
    public void handle(NetworkEvent.Context context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            var player = Minecraft.getInstance().player;
            if (player != null && player.containerMenu instanceof EngineersEncoderContainer menu
                    && menu.containerId == containerId) menu.applySync(mode, recipeId);
        });
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(containerId);
        buffer.writeVarInt(mode);
        buffer.writeBoolean(recipeId != null);
        if (recipeId != null) buffer.writeResourceLocation(recipeId);
    }

    public static PacketEncoderSync decode(FriendlyByteBuf buffer) {
        int containerId = buffer.readVarInt();
        int mode = buffer.readVarInt();
        return new PacketEncoderSync(containerId, mode, buffer.readBoolean() ? buffer.readResourceLocation() : null);
    }
}
