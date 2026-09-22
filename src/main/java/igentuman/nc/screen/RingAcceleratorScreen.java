package igentuman.nc.screen;

import igentuman.nc.block_entity.accelerator.RingAcceleratorControllerBE;
import igentuman.nc.container.MultiblockControllerContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatParticleEnergy;

public class RingAcceleratorScreen extends AbstractAcceleratorControllerScreen {

    public RingAcceleratorScreen(MultiblockControllerContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title, true);
    }

    @Override
    protected int coolantInputTank() {
        return RingAcceleratorControllerBE.TANK_COOLANT_IN;
    }

    @Override
    protected List<Component> statLines() {
        List<Component> lines = super.statLines();
        lines.add(__("screen.nuclearcraft.accelerator.dipole_field", synced("dipoleField")));
        long maxEnergyKeV = ((RingAcceleratorControllerBE) menu.getBlockEntity()).maximumEnergyKeV;
        lines.add(__("screen.nuclearcraft.accelerator.max_energy", formatParticleEnergy(maxEnergyKeV)));
        if (synced("incompatibleParticle") != 0) {
            lines.add(__("screen.nuclearcraft.accelerator.incompatible_particle").withStyle(ChatFormatting.RED));
        } else if (synced("inputEnergyTooLow") != 0) {
            lines.add(__("screen.nuclearcraft.accelerator.energy_too_low").withStyle(ChatFormatting.RED));
        } else if (synced("inputEnergyTooHigh") != 0) {
            lines.add(__("screen.nuclearcraft.accelerator.energy_too_high").withStyle(ChatFormatting.RED));
        }
        return lines;
    }

    @Override
    protected List<Component> machineLines() {
        List<Component> lines = super.machineLines();
        if (synced("incompatibleParticle") != 0) {
            lines.add(__("screen.nuclearcraft.accelerator.incompatible_particle").withStyle(ChatFormatting.RED));
        } else if (synced("inputEnergyTooLow") != 0) {
            lines.add(__("screen.nuclearcraft.accelerator.energy_too_low").withStyle(ChatFormatting.RED));
        } else if (synced("inputEnergyTooHigh") != 0) {
            lines.add(__("screen.nuclearcraft.accelerator.energy_too_high").withStyle(ChatFormatting.RED));
        }
        return lines;
    }
}
