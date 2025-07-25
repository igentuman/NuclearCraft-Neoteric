package igentuman.nc.compat.mekanism;

import mekanism.api.IConfigurable;
import mekanism.api.RelativeSide;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public class MekInteractions {

    public static boolean handleMultitoolInteractionWithMek(BlockEntity be, Player player, Direction side) {
        if (be instanceof ISideConfiguration config) {
            for(TransmissionType transmissionType: TransmissionType.values()) {
                if (!config.getConfig().supports(transmissionType)) {
                    continue;
                }
                ConfigInfo info = config.getConfig().getConfig(transmissionType);
                if (info != null) {
                    RelativeSide relativeSide = RelativeSide.fromDirections(config.getDirection(), side);
                    DataType dataType = info.getDataType(relativeSide);
                    if (!player.isShiftKeyDown()) {
                        player.displayClientMessage(MekanismLang.CONFIGURATOR_VIEW_MODE.translateColored(EnumColor.GRAY, transmissionType, dataType.getColor(),
                                dataType, dataType.getColor().getColoredName()), true);
                    } else if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, be)) {
                        return false;
                    } else {
                        DataType old = dataType;
                        dataType = info.incrementDataType(relativeSide);
                        if (dataType != old) {
                            player.displayClientMessage(MekanismLang.CONFIGURATOR_TOGGLE_MODE.translateColored(EnumColor.GRAY, transmissionType, dataType.getColor(),
                                    dataType, dataType.getColor().getColoredName()), true);
                            config.getConfig().sideChanged(transmissionType, relativeSide);
                        }
                    }
                }
                return true;
            }
        }
        Optional<IConfigurable> capability = CapabilityUtils.getCapability(be, Capabilities.CONFIGURABLE, side).resolve();
        if (capability.isPresent()) {
            IConfigurable config = capability.get();
            if (player.isShiftKeyDown()) {
                return config.onSneakRightClick(player) == InteractionResult.SUCCESS;
            }
            return config.onRightClick(player) == InteractionResult.SUCCESS;
        }
        return false;
    }

}
