package igentuman.nc.block_entity;

import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.multiblock.StructureLifecycleState;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.geometry.StructureRole;
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
        StructureRecord record = controller == null ? null : controller.scheduledStructure()
                .filter(candidate -> candidate.state() == StructureLifecycleState.FORMED)
                .orElse(null);
        if (debug != null) {
            debug.accept("scheduledStructure=" + (controller == null ? "no controller"
                    : controller.scheduledStructure().map(r -> r.state().toString()).orElse("absent")));
        }

        int channel = 0;
        if (record != null) {
            StructureRole role = next == BeamPortMode.INPUT ? StructureRole.BEAM_INPUT : StructureRole.BEAM_OUTPUT;
            List<BlockPos> positions = record.roles().getOrDefault(role, List.of());
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
