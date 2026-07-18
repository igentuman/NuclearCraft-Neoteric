package igentuman.nc.network.toServer;

import igentuman.nc.block.entity.processor.CreativeParticleSourceBE;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PacketCreativeParticleSource implements INcPacket {

    private BlockPos tilePosition;
    private String particle;
    private double focus;
    private double energy;
    private int scale;

    public PacketCreativeParticleSource(BlockPos position, String particle, double focus, double energy, int scale) {
        this.tilePosition = position;
        this.particle = particle;
        this.focus = focus;
        this.energy = energy;
        this.scale = scale;
    }

    public PacketCreativeParticleSource() {
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        if (player.level.getBlockEntity(tilePosition) instanceof CreativeParticleSourceBE be) {
            be.updateBeamSettings(particle, focus, energy, scale);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeUtf(particle);
        buffer.writeDouble(focus);
        buffer.writeDouble(energy);
        buffer.writeInt(scale);
    }

    public static PacketCreativeParticleSource decode(FriendlyByteBuf buffer) {
        PacketCreativeParticleSource packet = new PacketCreativeParticleSource();
        packet.tilePosition = buffer.readBlockPos();
        packet.particle = buffer.readUtf();
        packet.focus = buffer.readDouble();
        packet.energy = buffer.readDouble();
        packet.scale = buffer.readInt();
        return packet;
    }
}
