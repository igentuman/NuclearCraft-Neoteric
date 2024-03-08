package igentuman.nc.network.toServer;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.fission.FissionPortBE;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.client.gui.element.button.Button.ReactorComparatorModeButton;
import igentuman.nc.client.gui.element.button.Button.ReactorMode;
import igentuman.nc.client.gui.element.button.Button.RedstoneConfig;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketGuiButtonPress implements INcPacket {

    private BlockPos tilePosition;
    private int buttonId;

    public PacketGuiButtonPress(Object position, int bId) {
        this.tilePosition = (BlockPos) position;
        buttonId = bId;
    }

    public PacketGuiButtonPress() {

    }


    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }

        BlockEntity be = player.level.getBlockEntity(tilePosition);
        switch (buttonId) {
            case RedstoneConfig.BTN_ID:
                if (!(be instanceof NCProcessorBE<?> processor)) {
                    return;
                }
                processor.toggleRedstoneMode();
                break;
            case ReactorMode.BTN_ID:
                if (!(be instanceof FissionControllerBE<?> port)) {
                    return;
                }
                port.toggleMode();
                break;
            case ReactorComparatorModeButton.BTN_ID:
                if (!(be instanceof FissionPortBE port)) {
                    return;
                }
                port.toggleComparatorMode();
                break;
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(buttonId);
    }

    public static PacketGuiButtonPress decode(FriendlyByteBuf buffer) {
         PacketGuiButtonPress packet = new PacketGuiButtonPress();
          packet.tilePosition = buffer.readBlockPos();
          packet.buttonId = buffer.readInt();
          return packet;
    }



}