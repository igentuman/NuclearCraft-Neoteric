package igentuman.nc.util.persistence;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.nio.file.Files;
import java.nio.file.Path;

/** Native current-format SavedData access that refuses to replace an existing unreadable store. */
public final class CurrentSavedData {
    private CurrentSavedData() {}

    /** SavedDataStorage accepts partial results; current stores must decode completely. */
    public static <T> Codec<T> strict(Codec<T> codec) {
        return new Codec<>() {
            @Override
            public <O> DataResult<Pair<T, O>> decode(DynamicOps<O> ops, O input) {
                DataResult<Pair<T, O>> result = codec.decode(ops, input);
                return result.isError() ? DataResult.error(() -> result.error().orElseThrow().message()) : result;
            }

            @Override
            public <O> DataResult<O> encode(T input, DynamicOps<O> ops, O prefix) {
                return codec.encode(input, ops, prefix);
            }
        };
    }

    public static <T extends SavedData> T get(ServerLevel level, SavedDataType<T> type) {
        Path dimensions = level.getServer().getWorldPath(LevelResource.ROOT).resolve("dimensions");
        Path dataDirectory = level.dimension().identifier().resolveAgainst(dimensions).resolve("data");
        return get(level.getDataStorage(), dataDirectory, type);
    }

    public static <T extends SavedData> T get(SavedDataStorage storage, Path dataDirectory, SavedDataType<T> type) {
        T current = storage.get(type);
        if (current != null) return current;
        Path file = type.id().withSuffix(".dat").resolveAgainst(dataDirectory);
        // Vanilla logs a failed read and returns null for both missing and corrupt data.
        if (!Files.notExists(file)) throw new IllegalStateException("Cannot load current saved data " + file);
        return storage.computeIfAbsent(type);
    }
}
