package igentuman.nc.particle;

import igentuman.nc.NuclearCraft;
import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class ParticleStorage implements IParticleHandler, INBTSerializable<CompoundTag> {

    protected int channels;
    protected ParticleStack[] stacks;
    protected long[] capacities;

    public ParticleStorage(int channels, long[] capacities) {
        this.channels = channels;
        this.stacks = new ParticleStack[channels];
        this.capacities = capacities;
        for (int i = 0; i < channels; i++) {
            this.stacks[i] = ParticleStack.EMPTY;
        }
    }

    public ParticleStorage(int channels, long defaultCapacity) {
        this.channels = channels;
        this.stacks = new ParticleStack[channels];
        this.capacities = new long[channels];
        for (int i = 0; i < channels; i++) {
            this.stacks[i] = ParticleStack.EMPTY;
            this.capacities[i] = defaultCapacity;
        }
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
            if (action == ParticleAction.EXECUTE) {
                stacks[channel] = new ParticleStack(stack.particleId(), toInsert, stack.meanEnergyKeV(), stack.focus());
                onContentsChanged(channel);
            }
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
        if (action == ParticleAction.EXECUTE) {
            stacks[channel] = ParticleStack.merge(existing, new ParticleStack(stack.particleId(), toInsert, stack.meanEnergyKeV(), stack.focus()));
            onContentsChanged(channel);
        }
        long remainderAmount = stack.amount() - toInsert;
        return remainderAmount > 0
                ? new ParticleStack(stack.particleId(), remainderAmount, stack.meanEnergyKeV(), stack.focus())
                : ParticleStack.EMPTY;
    }

    @Override
    public ParticleStack extract(int channel, long maximumAmount, ParticleAction action) {
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

        if (action == ParticleAction.EXECUTE) {
            long remaining = existing.amount() - toExtract;
            stacks[channel] = remaining > 0
                    ? new ParticleStack(existing.particleId(), remaining, existing.meanEnergyKeV(), existing.focus())
                    : ParticleStack.EMPTY;
            onContentsChanged(channel);
        }
        return extracted;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ListTag channelList = new ListTag();
        for (int i = 0; i < channels; i++) {
            if (!stacks[i].isEmpty()) {
                int channel = i;
                ParticleStack.CODEC.encodeStart(NbtOps.INSTANCE, stacks[i]).result().ifPresent(particleTag -> {
                    CompoundTag channelTag = new CompoundTag();
                    channelTag.putInt("Channel", channel);
                    channelTag.putLong("Capacity", capacities[channel]);
                    channelTag.put("Particle", particleTag);
                    channelList.add(channelTag);
                });
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Channels", channelList);
        nbt.putInt("Size", channels);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        ListTag channelList = nbt.getList("Channels", Tag.TAG_COMPOUND);
        for (int i = 0; i < channelList.size(); i++) {
            CompoundTag channelTag = channelList.getCompound(i);
            int channel = channelTag.getInt("Channel");
            if (channel < 0 || channel >= channels) {
                continue;
            }
            if (channelTag.contains("Capacity", Tag.TAG_LONG)) {
                capacities[channel] = channelTag.getLong("Capacity");
            }
            ParticleStack.CODEC.parse(NbtOps.INSTANCE, channelTag.get("Particle")).result().ifPresentOrElse(
                    stack -> stacks[channel] = stack,
                    () -> NuclearCraft.LOGGER.error("Particle storage: dropping unreadable or unknown-species entry in channel {}", channel)
            );
        }
    }

    protected void validateChannelIndex(int channel) {
        if (channel < 0 || channel >= channels) {
            throw new IndexOutOfBoundsException("Channel " + channel + " not in valid range - [0," + channels + ")");
        }
    }

    protected void onContentsChanged(int channel) {
    }
}
