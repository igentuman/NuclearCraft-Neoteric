package igentuman.nc.world.anomaly;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class AnomalySavedData extends SavedData {

    private static final String NAME = "nc_anomalies";

    private final LongOpenHashSet destroyedCells = new LongOpenHashSet();

    public AnomalySavedData() {
    }

    public static AnomalySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(AnomalySavedData::load, AnomalySavedData::new, NAME);
    }

    public boolean isDestroyed(long cellId) {
        return destroyedCells.contains(cellId);
    }

    public void markDestroyed(long cellId) {
        if (destroyedCells.add(cellId)) {
            setDirty();
        }
    }

    public static AnomalySavedData load(CompoundTag tag) {
        AnomalySavedData data = new AnomalySavedData();
        for (long cell : tag.getLongArray("destroyed")) {
            data.destroyedCells.add(cell);
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putLongArray("destroyed", destroyedCells.toLongArray());
        return tag;
    }
}
