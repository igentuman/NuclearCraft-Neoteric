package igentuman.nc.block_entity;

import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.geometry.StructureRole;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

public final class ParticleBeamPortState {

    private BeamPortMode mode = BeamPortMode.DISABLED;
    private int channel = -1;
    private boolean configured;

    public void configure(StructureRecord record, BlockPos position) {
        if (configured) {
            StructureRole role = mode == BeamPortMode.INPUT ? StructureRole.BEAM_INPUT
                    : mode == BeamPortMode.OUTPUT ? StructureRole.BEAM_OUTPUT : null;
            channel = role == null ? -1 : indexOf(record, role, position);
            return;
        }
        int input = indexOf(record, StructureRole.BEAM_INPUT, position);
        if (input >= 0) {
            set(BeamPortMode.INPUT, input);
            return;
        }
        int output = indexOf(record, StructureRole.BEAM_OUTPUT, position);
        if (output >= 0) {
            set(BeamPortMode.OUTPUT, output);
            return;
        }
        set(BeamPortMode.DISABLED, -1);
    }

    public void detach() {
        channel = -1;
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

    private static int indexOf(StructureRecord record, StructureRole role, BlockPos position) {
        List<BlockPos> positions = record.roles().getOrDefault(role, List.of());
        return positions.indexOf(position);
    }
}
