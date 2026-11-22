package igentuman.nc.particle;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Stores particle channels with rollback and commit-only change notifications. */
public class ParticleStorage implements IParticleHandler, ValueIOSerializable {

    private record SavedChannel(int channel, long capacity, ParticleStack stack) {
        private static final Codec<SavedChannel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("Channel").forGetter(SavedChannel::channel),
                Codec.LONG.validate(capacity -> capacity >= 0 ? DataResult.success(capacity)
                        : DataResult.error(() -> "Negative particle capacity")).fieldOf("Capacity").forGetter(SavedChannel::capacity),
                ParticleStack.CODEC.fieldOf("Particle").forGetter(SavedChannel::stack)
        ).apply(instance, SavedChannel::new));
    }

    // ValueInput can accept partial results; a damaged list must never erase valid channels.
    private static final Codec<List<SavedChannel>> CHANNELS_CODEC = SavedChannel.CODEC.listOf().mapResult(new Codec.ResultFunction<>() {
        private <T> DataResult<T> complete(DataResult<T> result) {
            return result.error().<DataResult<T>>map(error -> DataResult.error(error::message)).orElse(result);
        }
        @Override public <T> DataResult<Pair<List<SavedChannel>, T>> apply(DynamicOps<T> ops, T input,
                                                                         DataResult<Pair<List<SavedChannel>, T>> result) {
            return complete(result);
        }
        @Override public <T> DataResult<T> coApply(DynamicOps<T> ops, List<SavedChannel> input, DataResult<T> result) {
            return complete(result);
        }
    });

    protected int channels;
    protected ParticleStack[] stacks;
    protected long[] capacities;
    private final ChannelJournal[] journals;

    public ParticleStorage(int channels, long[] capacities) {
        if (channels < 0 || capacities.length != channels || Arrays.stream(capacities).anyMatch(capacity -> capacity < 0)) {
            throw new IllegalArgumentException("Invalid particle channel capacities");
        }
        this.channels = channels;
        this.stacks = new ParticleStack[channels];
        this.capacities = capacities.clone();
        this.journals = new ChannelJournal[channels];
        for (int i = 0; i < channels; i++) {
            this.stacks[i] = ParticleStack.EMPTY;
            this.journals[i] = new ChannelJournal(i);
        }
    }

    public ParticleStorage(int channels, long defaultCapacity) {
        this(channels, new long[channels]);
        if (defaultCapacity < 0) throw new IllegalArgumentException("Negative particle capacity");
        Arrays.fill(this.capacities, defaultCapacity);
    }

    @Override
    public int channels() {
        return channels;
    }

    @Override
    public ParticleStack snapshot(int channel) {
        validateChannelIndex(channel);
        return stacks[channel];
    }

    @Override
    public long capacity(int channel) {
        validateChannelIndex(channel);
        return capacities[channel];
    }

    @Override
    public ParticleStack insert(int channel, ParticleStack stack, ParticleAction action) {
        try (var transaction = Transaction.openRoot()) {
            ParticleStack remainder = insert(channel, stack, transaction);
            if (action == ParticleAction.EXECUTE) transaction.commit();
            return remainder;
        }
    }

    /** Inserts particles in the same transaction as an item or fluid conversion. */
    public ParticleStack insert(int channel, ParticleStack stack, TransactionContext transaction) {
        validateChannelIndex(channel);
        if (stack.isEmpty()) {
            return ParticleStack.EMPTY;
        }

        ParticleStack existing = stacks[channel];
        long capacity = capacities[channel];

        if (existing.isEmpty()) {
            long toInsert = Math.min(stack.amount(), capacity);
            if (toInsert <= 0) {
                return stack;
            }
            journals[channel].updateSnapshots(transaction);
            stacks[channel] = new ParticleStack(stack.particleId(), toInsert, stack.meanEnergyKeV(), stack.focus());
            long remainderAmount = stack.amount() - toInsert;
            return remainderAmount > 0
                    ? new ParticleStack(stack.particleId(), remainderAmount, stack.meanEnergyKeV(), stack.focus())
                    : ParticleStack.EMPTY;
        }

        if (!existing.particleId().equals(stack.particleId())
                || existing.meanEnergyKeV() != stack.meanEnergyKeV()
                || existing.focus() != stack.focus()) {
            return stack;
        }

        long space = capacity - existing.amount();
        if (space <= 0) {
            return stack;
        }
        long toInsert = Math.min(stack.amount(), space);
        journals[channel].updateSnapshots(transaction);
        stacks[channel] = ParticleStack.merge(existing, new ParticleStack(stack.particleId(), toInsert, stack.meanEnergyKeV(), stack.focus()));
        long remainderAmount = stack.amount() - toInsert;
        return remainderAmount > 0
                ? new ParticleStack(stack.particleId(), remainderAmount, stack.meanEnergyKeV(), stack.focus())
                : ParticleStack.EMPTY;
    }

    @Override
    public ParticleStack extract(int channel, long maximumAmount, ParticleAction action) {
        try (var transaction = Transaction.openRoot()) {
            ParticleStack extracted = extract(channel, maximumAmount, transaction);
            if (action == ParticleAction.EXECUTE) transaction.commit();
            return extracted;
        }
    }

    /** Extracts particles with nested-transaction rollback support. */
    public ParticleStack extract(int channel, long maximumAmount, TransactionContext transaction) {
        validateChannelIndex(channel);
        if (maximumAmount <= 0) {
            return ParticleStack.EMPTY;
        }

        ParticleStack existing = stacks[channel];
        if (existing.isEmpty()) {
            return ParticleStack.EMPTY;
        }

        long toExtract = Math.min(maximumAmount, existing.amount());
        ParticleStack extracted = new ParticleStack(existing.particleId(), toExtract, existing.meanEnergyKeV(), existing.focus());

        journals[channel].updateSnapshots(transaction);
        long remaining = existing.amount() - toExtract;
        stacks[channel] = remaining > 0
                ? new ParticleStack(existing.particleId(), remaining, existing.meanEnergyKeV(), existing.focus())
                : ParticleStack.EMPTY;
        return extracted;
    }

    @Override
    public void serialize(ValueOutput output) {
        List<SavedChannel> saved = new ArrayList<>(channels);
        for (int channel = 0; channel < channels; channel++) {
            saved.add(new SavedChannel(channel, capacities[channel], stacks[channel]));
        }
        output.putInt("Size", channels);
        output.store("Channels", CHANNELS_CODEC, saved);
    }

    @Override
    public void deserialize(ValueInput input) {
        if (Arrays.stream(journals).anyMatch(SnapshotJournal::isInTransaction)) {
            throw new IllegalStateException("Cannot replace particle storage during a transaction");
        }
        if (input.getIntOr("Size", -1) != channels) throw new IllegalArgumentException("Configured particle channel count changed");
        List<SavedChannel> saved = input.read("Channels", CHANNELS_CODEC)
                .orElseThrow(() -> new IllegalArgumentException("Invalid current-format particle storage"));
        if (saved.size() != channels) throw new IllegalArgumentException("Missing particle channels");
        ParticleStack[] loaded = new ParticleStack[channels];
        long[] loadedCapacities = new long[channels];
        for (SavedChannel channel : saved) {
            if (channel.channel() >= channels || loaded[channel.channel()] != null
                    || channel.stack().amount() > channel.capacity()) {
                throw new IllegalArgumentException("Invalid or duplicate particle channel: " + channel.channel());
            }
            loaded[channel.channel()] = channel.stack();
            loadedCapacities[channel.channel()] = channel.capacity();
        }
        stacks = loaded;
        capacities = loadedCapacities;
    }

    protected void validateChannelIndex(int channel) {
        if (channel < 0 || channel >= channels) {
            throw new IndexOutOfBoundsException("Channel " + channel + " not in valid range - [0," + channels + ")");
        }
    }

    private class ChannelJournal extends SnapshotJournal<ParticleStack> {
        private final int channel;

        private ChannelJournal(int channel) { this.channel = channel; }
        @Override protected ParticleStack createSnapshot() { return stacks[channel]; }
        @Override protected void revertToSnapshot(ParticleStack snapshot) { stacks[channel] = snapshot; }
        @Override protected void onRootCommit(ParticleStack original) {
            if (!original.equals(stacks[channel])) onContentsChanged(channel);
        }
    }

    protected void onContentsChanged(int channel) {
    }
}
