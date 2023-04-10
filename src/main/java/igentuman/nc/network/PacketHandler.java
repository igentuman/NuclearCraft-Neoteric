package igentuman.nc.network;

import igentuman.nc.NuclearCraft;
import igentuman.nc.network.toClient.PacketRadiationData;
import igentuman.nc.network.toServer.PacketGuiButtonPress;
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

        registerClientToServer(PacketGuiButtonPress.class, PacketGuiButtonPress::decode);


        //Server to client messages

        registerServerToClient(PacketRadiationData.class, PacketRadiationData::decode);
       // registerServerToClient(PacketPipeUpdate.class, PacketPipeUpdate::decode);
       // registerServerToClient(PacketUpdateContainer.class, PacketUpdateContainer::decode);
       // registerServerToClient(PacketUpdateTile.class, PacketUpdateTile::decode);
    }
}