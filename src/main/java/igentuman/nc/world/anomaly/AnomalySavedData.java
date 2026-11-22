package igentuman.nc.world.anomaly;

import com.mojang.serialization.Codec;
import igentuman.nc.util.persistence.CurrentSavedData;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.Arrays;

/** Persists destroyed anomaly cells independently in each dimension. */
public class AnomalySavedData extends SavedData {

    private static final String NAME = "nc_anomalies";
    public static final Codec<AnomalySavedData> CODEC = CurrentSavedData.strict(Codec.LONG_STREAM.xmap(cells -> {
        AnomalySavedData data = new AnomalySavedData();
        cells.forEach(data.destroyedCells::add);
        return data;
    }, data -> Arrays.stream(data.destroyedCells.toLongArray())).fieldOf("destroyed").codec());
    public static final SavedDataType<AnomalySavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("nuclearcraft", NAME), AnomalySavedData::new, CODEC);

    private final LongOpenHashSet destroyedCells = new LongOpenHashSet();

    public AnomalySavedData() {
    }

    public static AnomalySavedData get(ServerLevel level) {
        return CurrentSavedData.get(level, TYPE);
    }

    public boolean isDestroyed(long cellId) {
        return destroyedCells.contains(cellId);
    }

    public void markDestroyed(long cellId) {
        if (destroyedCells.add(cellId)) {
            setDirty();
        }
    }
}
