package igentuman.nc.network.particle;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.particle.CreativeParticleSourceBE;
import igentuman.nc.container.CreativeParticleSourceContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CreativeParticleSourceSettingsPayload(
        BlockPos sourcePos,
        ResourceLocation particleId,
        double focus,
        double energyValue,
        int energyScale
) implements CustomPacketPayload {

    public static final Type<CreativeParticleSourceSettingsPayload> TYPE =
            new Type<>(NuclearCraft.rl("creative_particle_source_settings"));

    public static final StreamCodec<FriendlyByteBuf, CreativeParticleSourceSettingsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, CreativeParticleSourceSettingsPayload::sourcePos,
                    ResourceLocation.STREAM_CODEC, CreativeParticleSourceSettingsPayload::particleId,
                    ByteBufCodecs.DOUBLE, CreativeParticleSourceSettingsPayload::focus,
                    ByteBufCodecs.DOUBLE, CreativeParticleSourceSettingsPayload::energyValue,
                    ByteBufCodecs.VAR_INT, CreativeParticleSourceSettingsPayload::energyScale,
                    CreativeParticleSourceSettingsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CreativeParticleSourceSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.containerMenu instanceof CreativeParticleSourceContainer menu)
                    || !menu.getPosition().equals(payload.sourcePos()) || !menu.stillValid(player)
                    || player.distanceToSqr(payload.sourcePos().getCenter()) > 64D) return;
            BlockEntity blockEntity = player.serverLevel().getBlockEntity(payload.sourcePos());
            if (blockEntity instanceof CreativeParticleSourceBE source && menu.getBlockEntity() == source) {
                source.updateSettings(payload.particleId(), payload.focus(),
                        payload.energyValue(), payload.energyScale());
            }
        });
    }
}
