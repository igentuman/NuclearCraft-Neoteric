package igentuman.nc.util.persistence;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;
import java.util.stream.Stream;

/** Bridges registry-aware raw-tag serializers to Value I/O without adding a save envelope. */
public final class TagDataIO {
    private static final MapCodec<CompoundTag> ROOT = MapCodec.assumeMapUnsafe(CompoundTag.CODEC);
    private static final MapCodec<Writer> WRITER = new MapCodec<>() {
        @Override
        public <T> RecordBuilder<T> encode(Writer writer, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            if (!(ops instanceof RegistryOps<?> registries)
                    || !(registries.lookupProvider instanceof RegistryOps.HolderLookupAdapter lookup)) {
                return prefix.withErrorsFrom(DataResult.error(() -> "NuclearCraft tag data requires registry context"));
            }
            CompoundTag tag = new CompoundTag();
            writer.write(tag, lookup.lookupProvider);
            return ROOT.encode(tag, ops, prefix);
        }

        @Override
        public <T> DataResult<Writer> decode(DynamicOps<T> ops, MapLike<T> input) {
            return DataResult.error(() -> "NuclearCraft tag writer is encode-only");
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.empty();
        }
    };

    private TagDataIO() {
    }

    /**
     * Decodes a copy of the input's tag with the input's own registry context; the counterpart of
     * {@link #write(ValueOutput, Writer)}. Readers may normalize the copy. Reader exceptions propagate,
     * as with a direct raw-tag load.
     */
    @SuppressWarnings("deprecation")
    public static <T> Optional<T> read(ValueInput input, Reader<T> reader) {
        return input.read(readerCodec(reader));
    }

    private static <T> MapCodec<T> readerCodec(Reader<T> reader) {
        return new MapCodec<>() {
            @Override
            public <O> RecordBuilder<O> encode(T value, DynamicOps<O> ops, RecordBuilder<O> prefix) {
                return prefix.withErrorsFrom(DataResult.error(() -> "NuclearCraft tag reader is decode-only"));
            }

            @Override
            public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
                if (!(ops instanceof RegistryOps<?> registries)
                        || !(registries.lookupProvider instanceof RegistryOps.HolderLookupAdapter lookup)) {
                    return DataResult.error(() -> "NuclearCraft tag data requires registry context");
                }
                return ROOT.decode(ops, input).map(tag -> reader.read(tag, lookup.lookupProvider));
            }

            @Override
            public <O> Stream<O> keys(DynamicOps<O> ops) {
                return Stream.empty();
            }
        };
    }

    /** Uses the output's registry context, including when the block entity has no attached level. */
    @SuppressWarnings("deprecation")
    public static void write(ValueOutput output, Writer writer) {
        output.store(WRITER, writer);
    }

    @FunctionalInterface
    public interface Reader<T> {
        T read(CompoundTag tag, HolderLookup.Provider registries);
    }

    @FunctionalInterface
    public interface Writer {
        void write(CompoundTag tag, HolderLookup.Provider registries);
    }
}
