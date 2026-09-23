package igentuman.nc.block_entity;

import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.multiblock.StructureRole;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.function.Consumer;

public interface IBeamPort {

    BeamPortMode beamPortMode();

    void configureMode(BeamPortMode mode, int channel);

    MultiblockControllerBE controller();

    BlockPos getBlockPos();

    default BeamPortMode cycleBeamPortMode() {
        return cycleBeamPortMode(null);
    }
    default BeamPortMode cycleBeamPortMode(Consumer<String> debug) {
        BeamPortMode[] order = {BeamPortMode.INPUT, BeamPortMode.OUTPUT, BeamPortMode.DISABLED};
        int start = 0;
        for (int i = 0; i < order.length; i++) {
            if (order[i] == beamPortMode()) start = i;
        }
        BeamPortMode next = order[(start + 1) % order.length];

        if (next == BeamPortMode.DISABLED) {
            configureMode(BeamPortMode.DISABLED, -1);
            return BeamPortMode.DISABLED;
        }

        MultiblockControllerBE controller = controller();
        if (debug != null) debug.accept("controller=" + (controller != null ? controller.getBlockPos() : "null"));
        boolean formed = controller != null && controller.structureFormed();
        if (debug != null) {
            debug.accept("structureFormed=" + (controller == null ? "no controller" : formed));
        }

        int channel = 0;
        if (formed) {
            StructureRole role = next == BeamPortMode.INPUT ? StructureRole.BEAM_INPUT : StructureRole.BEAM_OUTPUT;
            List<BlockPos> positions = controller.rolePositions(role);
            int found = positions.indexOf(getBlockPos());
            if (debug != null) {
                debug.accept(next + " role list=" + positions + " thisPort=" + getBlockPos() + " channel=" + found);
            }
            if (found >= 0) channel = found;
        }
        configureMode(next, channel);
        return next;
    }
}
