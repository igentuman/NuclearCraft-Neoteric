package igentuman.nc.block_entity.accelerator;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.multiblock.accelerator.BeamDiverterCache;
import igentuman.nc.multiblock.accelerator.BeamDiverterLogic;
import igentuman.nc.multiblock.StructureRole;
import igentuman.nc.particle.ParticlePortView;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BeamDiverterControllerBE extends AbstractAcceleratorControllerBE {

    @NBTField(syncToClient = true)
    public int selectedOutputChannel;
    @NBTField(syncToClient = true)
    public boolean incompatibleTurn;

    public BeamDiverterControllerBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    @Override
    public List<BlockPos> rolePositions(StructureRole role) {
        BeamDiverterCache cache = cache();
        if (cache == null || !formed) return List.of();
        return switch (role) {
            case BEAM_INPUT -> cache.beamInputs;
            case BEAM_OUTPUT -> cache.beamOutputs;
            case SERVICE_PORT -> cache.servicePorts;
            default -> List.of();
        };
    }

    @Nullable
    @Override
    public IParticleHandler getParticleHandler(BlockPos portPos, BeamPortMode mode, int channel) {
        BeamDiverterLogic logic = logic();
        if (logic == null || !formed || channel < 0) return null;
        if (mode == BeamPortMode.OUTPUT && channel != selectedOutputChannel) return null;
        StructureRole role = mode == BeamPortMode.INPUT ? StructureRole.BEAM_INPUT
                : mode == BeamPortMode.OUTPUT ? StructureRole.BEAM_OUTPUT : null;
        if (role == null) return null;
        List<BlockPos> positions = rolePositions(role);
        if (channel >= positions.size() || !positions.get(channel).equals(portPos)) return null;
        return mode == BeamPortMode.INPUT
                ? new ParticlePortView(logic.input(), 0, ParticlePortView.Access.INPUT)
                : new ParticlePortView(logic.pendingOutput(), 0, ParticlePortView.Access.OUTPUT);
    }

    public boolean cycleSelectedOutputChannel() {
        int outputCount = rolePositions(StructureRole.BEAM_OUTPUT).size();
        if (outputCount <= 0) return false;
        return setSelectedOutputChannel((selectedOutputChannel + 1) % outputCount);
    }

    public boolean setSelectedOutputChannel(int channel) {
        List<BlockPos> outputs = rolePositions(StructureRole.BEAM_OUTPUT);
        if (channel < 0 || channel >= outputs.size() || channel == selectedOutputChannel) return false;
        BeamDiverterLogic logic = logic();
        if (logic != null && !logic.pendingOutput().snapshot(0).isEmpty()) return false;
        selectedOutputChannel = channel;
        if (level instanceof ServerLevel serverLevel) synchronizeOutputModes(serverLevel, outputs, channel);
        setChanged();
        return true;
    }

    public int clampSelectedOutputChannel(int outputCount) {
        int selected = Math.clamp(selectedOutputChannel, 0, outputCount - 1);
        if (selected != selectedOutputChannel) {
            selectedOutputChannel = selected;
            setChanged();
        }
        return selected;
    }

    public void synchronizeOutputModes(ServerLevel level, List<BlockPos> outputs, int selected) {
        for (int channel = 0; channel < outputs.size(); channel++) {
            BlockPos pos = outputs.get(channel);
            if (!level.hasChunkAt(pos)) continue;
            if (level.getBlockEntity(pos) instanceof AcceleratorBeamPortBE port) {
                port.configureMode(channel == selected ? BeamPortMode.OUTPUT : BeamPortMode.DISABLED, channel);
            }
        }
    }

    public void updateIncompatibleTurn(boolean value) {
        if (incompatibleTurn == value) return;
        incompatibleTurn = value;
        setChanged();
    }

    @Nullable
    private BeamDiverterCache cache() {
        var instance = instance();
        return instance != null && instance.cache instanceof BeamDiverterCache cache ? cache : null;
    }

    @Nullable
    private BeamDiverterLogic logic() {
        var instance = instance();
        return instance != null && instance.logic instanceof BeamDiverterLogic logic ? logic : null;
    }
}
