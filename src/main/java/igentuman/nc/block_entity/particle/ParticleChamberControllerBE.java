package igentuman.nc.block_entity.particle;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.multiblock.StructureLifecycleState;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.multiblock.particle_chamber.ParticleChamberRuntimeState;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.particle.ParticlePortView;
import igentuman.nc.particle.ParticleReactionPlan;
import igentuman.nc.particle.ParticleStorage;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.Registers;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ParticleChamberControllerBE extends MultiblockControllerBE {

    public static final int MAX_PARTICLE_CHANNELS = 4;

    protected ParticleChamberRuntimeState runtimeState;

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

    private static int[] emptyChannelIds() {
        int[] ids = new int[MAX_PARTICLE_CHANNELS];
        java.util.Arrays.fill(ids, -1);
        return ids;
    }

    protected ParticleChamberControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
        runtimeState = new ParticleChamberRuntimeState(List.of(), List.of(), null, 0D, List.of(), false);
    }

    public ParticleChamberRuntimeState runtimeState() {
        return runtimeState;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (!formed || !(level instanceof ServerLevel serverLevel)) {
            clearParticleDisplay();
            return;
        }
        scheduledStructure().filter(record -> record.state() == StructureLifecycleState.FORMED)
                .ifPresent(record -> {
                    ensureChannelCounts(record.roles().getOrDefault(StructureRole.BEAM_INPUT, List.of()).size(),
                            record.roles().getOrDefault(StructureRole.BEAM_OUTPUT, List.of()).size());
                    boolean enabled = redstoneEnabled(serverLevel, record);
                    if (enabled != runtimeState.enabled()) {
                        runtimeState = new ParticleChamberRuntimeState(runtimeState.inputs(),
                                runtimeState.pendingOutputs(), runtimeState.activeReaction(),
                                runtimeState.fractionalWork(), runtimeState.fractionalYields(), enabled);
                        setChanged();
                    }
                    updateParticleDisplay();
                });
    }

    private void clearParticleDisplay() {
        writeChannelDisplay(List.of(), inputParticleRegId, inputParticleAmount, inputParticleEnergy, inputParticleFocus);
        writeChannelDisplay(List.of(), outputParticleRegId, outputParticleAmount, outputParticleEnergy, outputParticleFocus);
    }

    private void updateParticleDisplay() {
        writeChannelDisplay(runtimeState.inputs(), inputParticleRegId, inputParticleAmount, inputParticleEnergy, inputParticleFocus);
        writeChannelDisplay(runtimeState.pendingOutputs(), outputParticleRegId, outputParticleAmount, outputParticleEnergy, outputParticleFocus);
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
    @Override
    public IParticleHandler getParticleHandler(BlockPos portPos, BeamPortMode mode, int channel) {
        var record = scheduledStructure().filter(candidate -> candidate.state() == StructureLifecycleState.FORMED)
                .orElse(null);
        if (record == null || channel < 0) return null;
        StructureRole role = mode == BeamPortMode.INPUT ? StructureRole.BEAM_INPUT
                : mode == BeamPortMode.OUTPUT ? StructureRole.BEAM_OUTPUT : null;
        if (role == null) return null;
        List<BlockPos> positions = record.roles().getOrDefault(role, List.of());
        if (channel >= positions.size() || !positions.get(channel).equals(portPos)) return null;
        ensureChannelCounts(record.roles().getOrDefault(StructureRole.BEAM_INPUT, List.of()).size(),
                record.roles().getOrDefault(StructureRole.BEAM_OUTPUT, List.of()).size());
        List<ParticleStorage> stores = mode == BeamPortMode.INPUT
                ? runtimeState.inputs() : runtimeState.pendingOutputs();
        return new ParticlePortView(stores.get(channel), 0,
                mode == BeamPortMode.INPUT ? ParticlePortView.Access.INPUT : ParticlePortView.Access.OUTPUT);
    }

    private void ensureChannelCounts(int inputCount, int outputCount) {
        if (runtimeState.inputs().size() == inputCount && runtimeState.pendingOutputs().size() == outputCount) return;
        List<ParticleStorage> inputs = resize(runtimeState.inputs(), inputCount);
        List<ParticleStorage> outputs = resize(runtimeState.pendingOutputs(), outputCount);
        runtimeState = new ParticleChamberRuntimeState(inputs, outputs, runtimeState.activeReaction(),
                runtimeState.fractionalWork(), runtimeState.fractionalYields(), runtimeState.enabled());
        setChanged();
    }

    private List<ParticleStorage> resize(List<ParticleStorage> current, int count) {
        List<ParticleStorage> resized = new ArrayList<>(count);
        for (int i = 0; i < count; i++) resized.add(i < current.size() ? current.get(i) : storage());
        return List.copyOf(resized);
    }

    private ParticleStorage storage() {
        return new ParticleStorage(1, Integer.MAX_VALUE) {
            @Override
            protected void onContentsChanged(int channel) {
                setChanged();
            }
        };
    }

    private boolean redstoneEnabled(ServerLevel level, StructureRecord record) {
        if (level.hasNeighborSignal(worldPosition)) return true;
        for (BlockPos servicePort : record.roles().getOrDefault(StructureRole.SERVICE_PORT, List.of())) {
            if (level.hasChunkAt(servicePort)
                    && level.getBlockEntity(servicePort) instanceof igentuman.nc.block_entity.MultiblockPortBE port
                    && worldPosition.equals(port.getControllerPos()) && port.controlSignalSample() > 0) return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag runtime = new CompoundTag();
        runtime.put("inputs", saveStorages(runtimeState.inputs(), registries));
        runtime.put("pendingOutputs", saveStorages(runtimeState.pendingOutputs(), registries));
        runtime.putDouble("fractionalWork", runtimeState.fractionalWork());
        runtime.putBoolean("enabled", runtimeState.enabled());
        if (runtimeState.activeReaction() != null) {
            runtime.putString("activeReaction", runtimeState.activeReaction().recipeId().toString());
        }
        ListTag yields = new ListTag();
        for (double yield : runtimeState.fractionalYields()) yields.add(DoubleTag.valueOf(yield));
        runtime.put("fractionalYields", yields);
        tag.put("particleRuntime", runtime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tag.contains("particleRuntime")) return;
        CompoundTag runtime = tag.getCompound("particleRuntime");
        List<ParticleStorage> inputs = loadStorages(runtime.getList("inputs", Tag.TAG_COMPOUND), registries);
        List<ParticleStorage> outputs = loadStorages(runtime.getList("pendingOutputs", Tag.TAG_COMPOUND), registries);
        double work = runtime.getDouble("fractionalWork");
        ParticleReactionPlan activeReaction = null;
        if (runtime.contains("activeReaction")) {
            ResourceLocation recipeId = ResourceLocation.tryParse(runtime.getString("activeReaction"));
            if (recipeId != null) {
                activeReaction = new ParticleReactionPlan(recipeId, List.of(), List.of(), List.of(),
                        List.of(), List.of(), List.of(), 0L, 0D);
            }
        }
        List<Double> yields = new ArrayList<>();
        ListTag yieldsTag = runtime.getList("fractionalYields", Tag.TAG_DOUBLE);
        for (int i = 0; i < yieldsTag.size(); i++) yields.add(((DoubleTag) yieldsTag.get(i)).getAsDouble());
        runtimeState = new ParticleChamberRuntimeState(inputs, outputs, activeReaction,
                Double.isFinite(work) && work >= 0 ? work : 0D, List.copyOf(yields), runtime.getBoolean("enabled"));
    }

    private static ListTag saveStorages(List<ParticleStorage> storages, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (ParticleStorage storage : storages) list.add(storage.serializeNBT(registries));
        return list;
    }

    private List<ParticleStorage> loadStorages(ListTag list, HolderLookup.Provider registries) {
        List<ParticleStorage> storages = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            ParticleStorage storage = storage();
            storage.deserializeNBT(registries, list.getCompound(i));
            storages.add(storage);
        }
        return List.copyOf(storages);
    }
}
