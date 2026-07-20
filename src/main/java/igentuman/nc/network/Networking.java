package igentuman.nc.network;

import igentuman.nc.util.ModUtil;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static igentuman.nc.NuclearCraft.MODID;

/** Registers all mod network payloads and their handlers with the payload registrar. */
public class Networking {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID).versioned("1");
        registrar.playToServer(
                PacketSideConfigToggle.TYPE,
                PacketSideConfigToggle.STREAM_CODEC,
                PacketSideConfigToggle::handle
        );
        registrar.playToServer(
                PacketRedstoneModeCycle.TYPE,
                PacketRedstoneModeCycle.STREAM_CODEC,
                PacketRedstoneModeCycle::handle
        );
        registrar.playToServer(
                PacketProcessorButtonPress.TYPE,
                PacketProcessorButtonPress.STREAM_CODEC,
                PacketProcessorButtonPress::handle
        );
        registrar.playToServer(
                PacketFissionToggleMode.TYPE,
                PacketFissionToggleMode.STREAM_CODEC,
                PacketFissionToggleMode::handle
        );
        registrar.playToServer(
                PacketFusionAmplificationAdjust.TYPE,
                PacketFusionAmplificationAdjust.STREAM_CODEC,
                PacketFusionAmplificationAdjust::handle
        );
        registrar.playToServer(
                PacketKugelblitzSliderAdjust.TYPE,
                PacketKugelblitzSliderAdjust.STREAM_CODEC,
                PacketKugelblitzSliderAdjust::handle
        );
        registrar.playToServer(
                PacketExplBurst.TYPE,
                PacketExplBurst.STREAM_CODEC,
                PacketExplBurst::handle
        );
        if (ModUtil.isAe2Loaded()) {
            registrar.playToServer(
                    PacketAE2PatternTransfer.TYPE,
                    PacketAE2PatternTransfer.STREAM_CODEC,
                    PacketAE2PatternTransfer::handle
            );
        }
        registrar.playToClient(
                PacketMultiblockFormed.TYPE,
                PacketMultiblockFormed.STREAM_CODEC,
                PacketMultiblockFormed::handle
        );
        registrar.playToClient(
                PacketMultiblockBroken.TYPE,
                PacketMultiblockBroken.STREAM_CODEC,
                PacketMultiblockBroken::handle
        );
        registrar.playToClient(
                PacketBombDetonationStart.TYPE,
                PacketBombDetonationStart.STREAM_CODEC,
                PacketBombDetonationStart::handle
        );
    }
}
