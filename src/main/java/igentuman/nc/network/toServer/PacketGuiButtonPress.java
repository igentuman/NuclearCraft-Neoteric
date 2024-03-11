package igentuman.nc.network.toServer;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.fission.FissionPortBE;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.client.gui.element.button.Button.ReactorComparatorModeButton;
import igentuman.nc.client.gui.element.button.Button.ReactorMode;
import igentuman.nc.client.gui.element.button.Button.RedstoneConfig;
import igentuman.nc.network.INcPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
        ServerPlayerEntity player = context.getSender();
        if (player == null) {
            return;
        }

        TileEntity be = player.level.getBlockEntity(tilePosition);
        switch (buttonId) {
            case RedstoneConfig.BTN_ID:
                if (!(be instanceof NCProcessorBE<?>)) {
                    return;
                }
                ((NCProcessorBE<?>) be).toggleRedstoneMode();
                break;
            case ReactorMode.BTN_ID:
                if (!(be instanceof FissionControllerBE<?>)) {
                    return;
                }
                ((FissionControllerBE<?>) be).toggleMode();
                break;
            case ReactorComparatorModeButton.BTN_ID:
                if (!(be instanceof FissionPortBE)) {
                    return;
                }
                ((FissionPortBE) be).toggleComparatorMode();
                break;
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeInt(buttonId);
    }

    public static PacketGuiButtonPress decode(PacketBuffer buffer) {
         PacketGuiButtonPress packet = new PacketGuiButtonPress();
          packet.tilePosition = buffer.readBlockPos();
          packet.buttonId = buffer.readInt();
          return packet;
    }



}