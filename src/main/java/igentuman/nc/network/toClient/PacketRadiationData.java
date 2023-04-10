package igentuman.nc.network.toClient;

import igentuman.nc.network.INcPacket;
import igentuman.nc.handler.radiation.RadiationManager;
import igentuman.nc.capability.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketRadiationData implements INcPacket {

    private final RadiationPacketType type;
    private final double radiation;
    private final double maxMagnitude;

    private PacketRadiationData(RadiationPacketType type, double radiation, double maxMagnitude) {
        this.type = type;
        this.radiation = radiation;
        this.maxMagnitude = maxMagnitude;
    }

    public static PacketRadiationData createEnvironmental(RadiationManager.LevelAndMaxMagnitude levelAndMaxMagnitude) {
        return new PacketRadiationData(RadiationPacketType.ENVIRONMENTAL, levelAndMaxMagnitude.level(), levelAndMaxMagnitude.maxMagnitude());
    }

    public static PacketRadiationData createPlayer(double radiation) {
        return new PacketRadiationData(RadiationPacketType.PLAYER, radiation, 0);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if (type == RadiationPacketType.ENVIRONMENTAL) {
            RadiationManager.INSTANCE.setClientEnvironmentalRadiation(radiation, maxMagnitude);
        } else if (type == RadiationPacketType.PLAYER) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(Capabilities.RADIATION_ENTITY).ifPresent(c -> c.set(radiation));
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(type);
        buffer.writeDouble(radiation);
        if (type.tracksMaxMagnitude) {
            buffer.writeDouble(maxMagnitude);
        }
    }

    public static PacketRadiationData decode(FriendlyByteBuf buffer) {
        RadiationPacketType type = buffer.readEnum(RadiationPacketType.class);
        return new PacketRadiationData(type, buffer.readDouble(), type.tracksMaxMagnitude ? buffer.readDouble() : 0);
    }

    public enum RadiationPacketType {
        ENVIRONMENTAL(true),
        PLAYER(false);

        private final boolean tracksMaxMagnitude;

        RadiationPacketType(boolean tracksMaxMagnitude) {
            this.tracksMaxMagnitude = tracksMaxMagnitude;
        }
    }
}
