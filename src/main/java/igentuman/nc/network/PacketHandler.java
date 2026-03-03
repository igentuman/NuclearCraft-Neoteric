package igentuman.nc.network;

import igentuman.nc.network.toClient.PacketPlayerRadiationData;
import igentuman.nc.network.toClient.PacketWorldRadiationData;
import igentuman.nc.network.toServer.*;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static igentuman.nc.NuclearCraft.MODID;

@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
public class PacketHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0");

        // Client to server messages
        registrar.playToServer(PacketSliderChanged.TYPE, PacketSliderChanged.STREAM_CODEC, PacketSliderChanged::handle);
        registrar.playToServer(PacketGuiButtonPress.TYPE, PacketGuiButtonPress.STREAM_CODEC, PacketGuiButtonPress::handle);
        registrar.playToServer(PacketSideConfigToggle.TYPE, PacketSideConfigToggle.STREAM_CODEC, PacketSideConfigToggle::handle);
        registrar.playToServer(PacketFlushSlotContent.TYPE, PacketFlushSlotContent.STREAM_CODEC, PacketFlushSlotContent::handle);
        registrar.playToServer(PacketHandleFluidSlotClick.TYPE, PacketHandleFluidSlotClick.STREAM_CODEC, PacketHandleFluidSlotClick::handle);
        registrar.playToServer(PacketBuildMultiblock.TYPE, PacketBuildMultiblock.STREAM_CODEC, PacketBuildMultiblock::handle);
        registrar.playToServer(PacketRecipeTransfer.TYPE, PacketRecipeTransfer.STREAM_CODEC, PacketRecipeTransfer::handle);
        // TODO: AE2 pattern transfer removed — waiting on AE2 to port to NeoForge 1.21.1. Re-enable PacketAE2PatternTransfer registration when available (packet itself is already migrated).

        // Server to client messages
        registrar.playToClient(PacketWorldRadiationData.TYPE, PacketWorldRadiationData.STREAM_CODEC, PacketWorldRadiationData::handle);
        registrar.playToClient(PacketPlayerRadiationData.TYPE, PacketPlayerRadiationData.STREAM_CODEC, PacketPlayerRadiationData::handle);
    }

    /**
     * Send a message to a specific player.
     */
    public <MSG extends CustomPacketPayload> void sendTo(MSG message, ServerPlayer player) {
        if (!(player instanceof FakePlayer)) {
            PacketDistributor.sendToPlayer(player, message);
        }
    }

    /**
     * Send a message to all connected players.
     */
    public <MSG extends CustomPacketPayload> void sendToAll(MSG message) {
        PacketDistributor.sendToAllPlayers(message);
    }

    /**
     * Send a message to the server.
     */
    public <MSG extends CustomPacketPayload> void sendToServer(MSG message) {
        PacketDistributor.sendToServer(message);
    }

    /**
     * Send a message to all players tracking the given entity.
     */
    public <MSG extends CustomPacketPayload> void sendToAllTracking(MSG message, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, message);
    }

    /**
     * Send a message to all players tracking the given entity, and the entity itself if it's a player.
     */
    public <MSG extends CustomPacketPayload> void sendToAllTrackingAndSelf(MSG message, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, message);
    }
}
