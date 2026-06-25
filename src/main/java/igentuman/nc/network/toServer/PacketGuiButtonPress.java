package igentuman.nc.network.toServer;

import igentuman.nc.block.accelerator.entity.AcceleratorPortBE;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.entity.ParticleChamberPortBE;
import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.block.fission.entity.FissionPortBE;
import igentuman.nc.block.fission.entity.MSRControllerBE;
import igentuman.nc.block.fission.entity.MSRPortBE;
import igentuman.nc.block.fusion.entity.FusionCoreBE;
import igentuman.nc.block.fusion.entity.FusionCoreProxyBE;
import igentuman.nc.block.heat_exchanger.entity.HeatExchangerControllerBE;
import igentuman.nc.block.heat_exchanger.entity.HeatExchangerColdCoolantPortBE;
import igentuman.nc.block.heat_exchanger.entity.HeatExchangerHotCoolantPortBE;
import igentuman.nc.block.kugelblitz.entity.ChamberPortBE;
import igentuman.nc.block.kugelblitz.entity.EXPLBE;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.block.turbine.entity.TurbinePortBE;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.button.Button.ReactorPortRedstoneModeButton;
import igentuman.nc.client.gui.element.button.Button.ReactorMode;
import igentuman.nc.client.gui.element.button.Button.RedstoneConfig;
import igentuman.nc.item.ContainerBlockItem;
import igentuman.nc.network.INcPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class PacketGuiButtonPress implements INcPacket {

    private BlockPos tilePosition;
    private UUID playerUUID;
    private int buttonId;

    public PacketGuiButtonPress(Player player, int bId) {
        this.playerUUID = player.getUUID();
        this.tilePosition = BlockPos.ZERO;
        buttonId = bId;
    }

    public PacketGuiButtonPress(BlockPos position, int bId) {
        this.tilePosition = (BlockPos) position;
        this.playerUUID = UUID.randomUUID();
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
        if(!tilePosition.equals(BlockPos.ZERO)) {
            toggleBlockEntity(player, tilePosition);
        }
        if(playerUUID.equals(player.getUUID())) {
            if(buttonId == Button.Magnet.BTN_ID) {
                ItemStack stack = player.getItemBySlot(EquipmentSlot.MAINHAND);
                if(stack.getItem() instanceof ContainerBlockItem containerBlockItem) {
                    containerBlockItem.toggleMagnetMode(stack);
                }
            }
        }
    }

    private void toggleBlockEntity(ServerPlayer player, BlockPos tilePosition) {
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
            case Button.MSRPortRedstoneModeButton.BTN_ID:
                if (be instanceof MSRPortBE msrPort) {
                    msrPort.toggleRedstoneMode();
                }
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
            case Button.AcceleratorPortRedstoneModeButton.BTN_ID:
                if (be instanceof AcceleratorPortBE port) {
                    port.toggleComparatorMode();
                }
                break;
            case Button.TargetChamberPortRedstoneModeButton.BTN_ID:
                if (be instanceof ParticleChamberPortBE port) {
                    port.toggleRedstoneMode();
                }
                break;
            case Button.RadiatorToggle.BTN_ID:
                if (be instanceof HeatExchangerControllerBE hx) {
                    hx.toggleRadiators();
                }
                break;
            case Button.HeatExchangerPortRedstoneModeButton.BTN_ID:
                if (be instanceof HeatExchangerHotCoolantPortBE hotPort) {
                    hotPort.toggleRedstoneMode();
                } else if (be instanceof HeatExchangerColdCoolantPortBE coldPort) {
                    coldPort.toggleRedstoneMode();
                }
                break;
            case 77:
                if (be instanceof EXPLBE expl) {
                    expl.activated = true;
                }
                break;
            case Button.VoidPebbles.BTN_ID:
                if (be instanceof MSRControllerBE msr) {
                    msr.voidPebbles();
                }
                break;
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(tilePosition);
        buffer.writeUUID(playerUUID);
        buffer.writeInt(buttonId);
    }

    public static PacketGuiButtonPress decode(FriendlyByteBuf buffer) {
         PacketGuiButtonPress packet = new PacketGuiButtonPress();
          packet.tilePosition = buffer.readBlockPos();
          packet.playerUUID = buffer.readUUID();
          packet.buttonId = buffer.readInt();
          return packet;
    }



}
