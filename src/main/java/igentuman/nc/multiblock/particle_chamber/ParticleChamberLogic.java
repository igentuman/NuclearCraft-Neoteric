package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.block_entity.particle.ParticleChamberControllerBE;
import igentuman.nc.api.multiblock.AbstractMultiblockLogic;
import igentuman.nc.particle.ParticleStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public abstract class ParticleChamberLogic extends AbstractMultiblockLogic<ParticleChamberCache> {

    protected List<ParticleStorage> inputs = List.of();
    protected List<ParticleStorage> pendingOutputs = List.of();
    @Nullable
    protected ResourceLocation activeReactionId;
    protected double fractionalWork;
    protected List<Double> fractionalYields = List.of();
    protected boolean enabled;
    private boolean changed;

    protected abstract void process(ServerLevel level, ParticleChamberControllerBE controller,
                                    ParticleChamberCache cache);

    public List<ParticleStorage> inputs() {
        return inputs;
    }

    public List<ParticleStorage> pendingOutputs() {
        return pendingOutputs;
    }

    @Override
    public final void tickServer(ServerLevel level, BlockPos controllerPos, ParticleChamberCache cache) {
        if (!(level.getBlockEntity(controllerPos) instanceof ParticleChamberControllerBE controller)) return;
        ensureChannels(cache.beamInputs.size(), cache.beamOutputs.size());
        boolean signal = redstoneEnabled(level, controllerPos, cache);
        if (signal != enabled) {
            enabled = signal;
            changed = true;
        }
        controller.updateParticleDisplay(inputs, pendingOutputs);
        if (enabled) process(level, controller, cache);
        if (changed) {
            changed = false;
            controller.setChanged();
        }
    }

    public final void ensureChannels(int inputCount, int outputCount) {
        if (inputs.size() == inputCount && pendingOutputs.size() == outputCount) return;
        inputs = resize(inputs, inputCount);
        pendingOutputs = resize(pendingOutputs, outputCount);
        changed = true;
    }

    protected final void finishCycle(@Nullable ResourceLocation reaction, double work, List<Double> yields) {
        activeReactionId = reaction;
        fractionalWork = Math.max(0, work);
        fractionalYields = List.copyOf(yields);
        changed = true;
    }

    protected final ParticleStack[] outputCandidates(List<ParticleStack> templates, long[] amounts, long energyKeV,
                                                     @Nullable Double focus, int outputPortCount) {
        ParticleStack[] candidates = new ParticleStack[amounts.length];
        for (int i = 0; i < amounts.length; i++) {
            if (amounts[i] <= 0 || i >= outputPortCount || i >= pendingOutputs.size()) continue;
            ParticleStack template = templates.get(i);
            candidates[i] = new ParticleStack(template.particleId(), amounts[i], energyKeV,
                    focus == null ? template.focus() : focus);
        }
        return candidates;
    }

    protected final boolean outputsFit(ParticleStack[] candidates) {
        for (int i = 0; i < candidates.length; i++) {
            if (candidates[i] != null
                    && !pendingOutputs.get(i).insert(0, candidates[i], ParticleAction.SIMULATE).isEmpty()) return false;
        }
        return true;
    }

    protected final void commitOutputs(ParticleStack[] candidates) {
        for (int i = 0; i < candidates.length; i++) {
            if (candidates[i] != null) pendingOutputs.get(i).insert(0, candidates[i], ParticleAction.EXECUTE);
        }
    }

    protected static <I extends RecipeInput, R extends Recipe<I>> RecipeHolder<R> bestRecipe(
            ServerLevel level, RecipeType<R> type, Predicate<R> matches, ToIntFunction<R> priority) {
        RecipeHolder<R> best = null;
        for (RecipeHolder<R> holder : level.getRecipeManager().getAllRecipesFor(type)) {
            R recipe = holder.value();
            if (!matches.test(recipe)) continue;
            if (best == null || priority.applyAsInt(recipe) > priority.applyAsInt(best.value())
                    || (priority.applyAsInt(recipe) == priority.applyAsInt(best.value())
                    && holder.id().compareTo(best.id()) < 0)) {
                best = holder;
            }
        }
        return best;
    }

    private static boolean redstoneEnabled(ServerLevel level, BlockPos controllerPos, ParticleChamberCache cache) {
        if (level.hasNeighborSignal(controllerPos)) return true;
        for (BlockPos servicePort : cache.servicePorts) {
            if (level.hasChunkAt(servicePort) && level.getBlockEntity(servicePort) instanceof MultiblockPortBE port
                    && controllerPos.equals(port.getControllerPos()) && port.controlSignalSample() > 0) return true;
        }
        return false;
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
                changed = true;
            }
        };
    }

    @Override
    public void saveRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inputs", saveStorages(inputs, registries));
        tag.put("pendingOutputs", saveStorages(pendingOutputs, registries));
        tag.putDouble("fractionalWork", fractionalWork);
        tag.putBoolean("enabled", enabled);
        if (activeReactionId != null) tag.putString("activeReaction", activeReactionId.toString());
        ListTag yields = new ListTag();
        for (double yield : fractionalYields) yields.add(DoubleTag.valueOf(yield));
        tag.put("fractionalYields", yields);
    }

    @Override
    public void loadRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        inputs = loadStorages(tag.getList("inputs", Tag.TAG_COMPOUND), registries);
        pendingOutputs = loadStorages(tag.getList("pendingOutputs", Tag.TAG_COMPOUND), registries);
        double work = tag.getDouble("fractionalWork");
        fractionalWork = Double.isFinite(work) && work >= 0 ? work : 0D;
        enabled = tag.getBoolean("enabled");
        activeReactionId = tag.contains("activeReaction")
                ? ResourceLocation.tryParse(tag.getString("activeReaction")) : null;
        List<Double> yields = new ArrayList<>();
        ListTag yieldsTag = tag.getList("fractionalYields", Tag.TAG_DOUBLE);
        for (int i = 0; i < yieldsTag.size(); i++) yields.add(yieldsTag.getDouble(i));
        fractionalYields = List.copyOf(yields);
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
