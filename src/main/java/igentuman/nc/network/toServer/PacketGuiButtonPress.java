package igentuman.nc.network.toServer;

import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.block.fission.entity.FissionPortBE;
import igentuman.nc.block.fusion.entity.FusionCoreBE;
import igentuman.nc.block.fusion.entity.FusionCoreProxyBE;
import igentuman.nc.block.kugelblitz.entity.ChamberPortBE;
import igentuman.nc.block.kugelblitz.entity.EXPLBE;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.block.turbine.entity.TurbinePortBE;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.button.Button.ReactorPortRedstoneModeButton;
import igentuman.nc.client.gui.element.button.Button.ReactorMode;
import igentuman.nc.client.gui.element.button.Button.RedstoneConfig;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

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

        BlockEntity be = player.level().getBlockEntity(tilePosition);
        switch (buttonId) {
            case RedstoneConfig.BTN_ID:
                if (!(be instanceof NCProcessorBE processor)) {
                    return;
                }
                processor.toggleRedstoneMode();
                break;
            case ReactorMode.BTN_ID:
                if (!(be instanceof FissionControllerBE controller)) {
                    return;
                }
                controller.toggleMode();
                break;
            case Button.MultiblockAnalyze.BTN_ID:
                if (!(be instanceof MultiblockControllerBE controller)) {
                    return;
                }
                controller.runAnalyze();
                break;
            case ReactorPortRedstoneModeButton.BTN_ID:
                if (!(be instanceof FissionPortBE port)) {
                    return;
                }
                port.toggleRedstoneMode();
                break;
            case Button.TurbinePortRedstoneModeButton.BTN_ID:
                if (!(be instanceof TurbinePortBE port)) {
                    return;
                }
                port.toggleRedstoneMode();
                break;
            case Button.FusionReactorRedstoneModeButton.BTN_ID:
                if (be instanceof FusionCoreBE port) {
                    port.toggleRedstoneMode();
                }
                if (be instanceof FusionCoreProxyBE port) {
                    port.toggleRedstoneMode();
                }
                break;

            case Button.Kugelblitz.BTN_ID:
                if (be instanceof ChamberPortBE port) {
                    port.toggleComparatorMode();
                }
                break;
            case 77:
                if (be instanceof EXPLBE expl) {
                    expl.activated = true;
                }
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
