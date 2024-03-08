package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.network.toClient.PacketRadiationData;
import igentuman.nc.network.toServer.*;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

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
        registerClientToServer(BatterySideConfig.class, BatterySideConfig::decode);
        registerClientToServer(StorageSideConfig.class, StorageSideConfig::decode);
        registerClientToServer(PacketFlushSlotContent.class, PacketFlushSlotContent::decode);


        //Server to client messages

        registerServerToClient(PacketRadiationData.class, PacketRadiationData::decode);
    }
}