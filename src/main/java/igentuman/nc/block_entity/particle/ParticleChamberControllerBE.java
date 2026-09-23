package igentuman.nc.block_entity.particle;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.multiblock.StructureRole;
import igentuman.nc.multiblock.particle_chamber.ParticleChamberCache;
import igentuman.nc.multiblock.particle_chamber.ParticleChamberLogic;
import igentuman.nc.particle.ParticlePortView;
import igentuman.nc.particle.ParticleStorage;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.Registers;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ParticleChamberControllerBE extends MultiblockControllerBE {

    public static final int MAX_PARTICLE_CHANNELS = 4;

    @NBTField(syncToClient = true, syncLength = MAX_PARTICLE_CHANNELS)
    public int[] inputParticleRegId = emptyChannelIds();
    @NBTField(syncToClient = true, syncLength = MAX_PARTICLE_CHANNELS)
    public int[] inputParticleAmount = new int[MAX_PARTICLE_CHANNELS];
    @NBTField(syncToClient = true, syncLength = MAX_PARTICLE_CHANNELS)
    public int[] inputParticleEnergy = new int[MAX_PARTICLE_CHANNELS];
    @NBTField(syncToClient = true, syncLength = MAX_PARTICLE_CHANNELS)
    public int[] inputParticleFocus = new int[MAX_PARTICLE_CHANNELS];

    @NBTField(syncToClient = true, syncLength = MAX_PARTICLE_CHANNELS)
    public int[] outputParticleRegId = emptyChannelIds();
    @NBTField(syncToClient = true, syncLength = MAX_PARTICLE_CHANNELS)
    public int[] outputParticleAmount = new int[MAX_PARTICLE_CHANNELS];
    @NBTField(syncToClient = true, syncLength = MAX_PARTICLE_CHANNELS)
    public int[] outputParticleEnergy = new int[MAX_PARTICLE_CHANNELS];
    @NBTField(syncToClient = true, syncLength = MAX_PARTICLE_CHANNELS)
    public int[] outputParticleFocus = new int[MAX_PARTICLE_CHANNELS];

    public ParticleChamberControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    private static int[] emptyChannelIds() {
        int[] ids = new int[MAX_PARTICLE_CHANNELS];
        Arrays.fill(ids, -1);
        return ids;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (!formed) updateParticleDisplay(List.of(), List.of());
    }

    @Override
    public List<BlockPos> rolePositions(StructureRole role) {
        ParticleChamberCache cache = cache();
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
        ParticleChamberCache cache = cache();
        ParticleChamberLogic logic = logic();
        if (cache == null || logic == null || !formed || channel < 0) return null;
        List<BlockPos> positions = mode == BeamPortMode.INPUT ? cache.beamInputs
                : mode == BeamPortMode.OUTPUT ? cache.beamOutputs : List.of();
        if (channel >= positions.size() || !positions.get(channel).equals(portPos)) return null;
        logic.ensureChannels(cache.beamInputs.size(), cache.beamOutputs.size());
        List<ParticleStorage> stores = mode == BeamPortMode.INPUT ? logic.inputs() : logic.pendingOutputs();
        return new ParticlePortView(stores.get(channel), 0,
                mode == BeamPortMode.INPUT ? ParticlePortView.Access.INPUT : ParticlePortView.Access.OUTPUT);
    }

    public void updateParticleDisplay(List<ParticleStorage> inputs, List<ParticleStorage> outputs) {
        writeChannelDisplay(inputs, inputParticleRegId, inputParticleAmount, inputParticleEnergy, inputParticleFocus);
        writeChannelDisplay(outputs, outputParticleRegId, outputParticleAmount, outputParticleEnergy,
                outputParticleFocus);
    }

    private static void writeChannelDisplay(List<ParticleStorage> storages,
                                            int[] regId, int[] amount, int[] energy, int[] focus) {
        for (int i = 0; i < regId.length; i++) {
            ParticleStack stack = i < storages.size() ? storages.get(i).snapshot(0) : ParticleStack.EMPTY;
            if (stack.isEmpty()) {
                regId[i] = -1;
                amount[i] = 0;
                energy[i] = 0;
                focus[i] = 0;
            } else {
                var definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(stack.particleId());
                regId[i] = definition != null ? Registers.PARTICLE_DEFINITION_REGISTRY.getId(definition) : -1;
                amount[i] = (int) Math.min(Integer.MAX_VALUE, stack.amount());
                energy[i] = (int) Math.min(Integer.MAX_VALUE, stack.meanEnergyKeV());
                focus[i] = (int) Math.min(Integer.MAX_VALUE, Math.round(stack.focus() * 1000D));
            }
        }
    }

    @Nullable
    private ParticleChamberCache cache() {
        var instance = instance();
        return instance != null && instance.cache instanceof ParticleChamberCache cache ? cache : null;
    }

    @Nullable
    private ParticleChamberLogic logic() {
        var instance = instance();
        return instance != null && instance.logic instanceof ParticleChamberLogic logic ? logic : null;
    }
}
