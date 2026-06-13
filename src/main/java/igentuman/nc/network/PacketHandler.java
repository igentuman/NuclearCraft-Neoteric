package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.network.toClient.PacketBombDetonationStart;
import igentuman.nc.network.toClient.PacketPlayerRadiationData;
import igentuman.nc.network.toClient.PacketQ36BeamFx;
import igentuman.nc.network.toClient.PacketWorldRadiationData;
import igentuman.nc.network.toServer.*;
import igentuman.nc.util.ModUtil;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler extends BasePacketHandler {

    private final SimpleChannel netHandler = createChannel(NuclearCraft.rl(NuclearCraft.MODID));

    @Override
    protected SimpleChannel getChannel() {
        return netHandler;
    }

    @Override
    public void initialize() {
        //Client to server messages
        registerClientToServer(PacketSliderChanged.class, PacketSliderChanged::decode);
        registerClientToServer(PacketGuiButtonPress.class, PacketGuiButtonPress::decode);
        registerClientToServer(PacketSideConfigToggle.class, PacketSideConfigToggle::decode);
        registerClientToServer(PacketFlushSlotContent.class, PacketFlushSlotContent::decode);
        registerClientToServer(PacketHandleFluidSlotClick.class, PacketHandleFluidSlotClick::decode);
        registerClientToServer(PacketBuildMultiblock.class, PacketBuildMultiblock::decode);
        registerClientToServer(PacketRecipeTransfer.class, PacketRecipeTransfer::decode);
        registerClientToServer(PacketCreativeParticleSource.class, PacketCreativeParticleSource::decode);
        registerClientToServer(PacketQ36Fire.class, PacketQ36Fire::decode);
        if(ModUtil.isAE2Loaded()) {
            registerClientToServer(PacketAE2PatternTransfer.class, PacketAE2PatternTransfer::decode);
        }

        //Server to client messages
        registerServerToClient(PacketWorldRadiationData.class, PacketWorldRadiationData::decode);
        registerServerToClient(PacketPlayerRadiationData.class, PacketPlayerRadiationData::decode);
        registerServerToClient(PacketQ36BeamFx.class, PacketQ36BeamFx::decode);
        registerServerToClient(PacketBombDetonationStart.class, PacketBombDetonationStart::decode);
    }
}