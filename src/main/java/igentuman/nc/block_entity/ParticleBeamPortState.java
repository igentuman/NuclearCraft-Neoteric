package igentuman.nc.block_entity;

import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.multiblock.StructureRole;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.function.Function;

public final class ParticleBeamPortState {

    private BeamPortMode mode = BeamPortMode.DISABLED;
    private int channel = -1;
    private boolean configured;

    public boolean configure(Function<StructureRole, List<BlockPos>> roles, BlockPos position) {
        BeamPortMode previousMode = mode;
        int previousChannel = channel;
        boolean previousConfigured = configured;
        if (configured) {
            StructureRole role = mode == BeamPortMode.INPUT ? StructureRole.BEAM_INPUT
                    : mode == BeamPortMode.OUTPUT ? StructureRole.BEAM_OUTPUT : null;
            channel = role == null ? -1 : roles.apply(role).indexOf(position);
        } else {
            int input = roles.apply(StructureRole.BEAM_INPUT).indexOf(position);
            int output = input >= 0 ? -1 : roles.apply(StructureRole.BEAM_OUTPUT).indexOf(position);
            if (input >= 0) set(BeamPortMode.INPUT, input);
            else if (output >= 0) set(BeamPortMode.OUTPUT, output);
            else set(BeamPortMode.DISABLED, -1);
        }
        return mode != previousMode || channel != previousChannel || configured != previousConfigured;
    }

    public boolean detach() {
        boolean changed = channel != -1;
        channel = -1;
        return changed;
    }

    public void clear() {
        mode = BeamPortMode.DISABLED;
        channel = -1;
        configured = false;
    }

    public BeamPortMode mode() {
        return mode;
    }

    public int channel() {
        return channel;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void set(BeamPortMode mode, int channel) {
        this.mode = mode;
        this.channel = channel;
        this.configured = true;
    }

    public void load(String serializedMode, int channel) {
        for (BeamPortMode candidate : BeamPortMode.values()) {
            if (candidate.getSerializedName().equals(serializedMode)) {
                set(candidate, channel);
                return;
            }
        }
        clear();
    }

}
