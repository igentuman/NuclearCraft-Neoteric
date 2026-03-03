package igentuman.api.platform;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Platform wrapper for spawn placement APIs.
 * In 1.21.1: SpawnPlacements.Type enum → SpawnPlacementTypes constants,
 * and registration moved to RegisterSpawnPlacementsEvent (SpawnPlacements.register is private).
 */
public final class NCSpawning {
    private NCSpawning() {}

    public static final SpawnPlacementType NO_RESTRICTIONS = SpawnPlacementTypes.NO_RESTRICTIONS;
    public static final SpawnPlacementType ON_GROUND = SpawnPlacementTypes.ON_GROUND;
    public static final SpawnPlacementType IN_WATER = SpawnPlacementTypes.IN_WATER;

    public static <T extends Entity> void register(RegisterSpawnPlacementsEvent event,
                                                    EntityType<T> entityType, SpawnPlacementType type,
                                                    Heightmap.Types heightmap,
                                                    SpawnPlacements.SpawnPredicate<T> predicate) {
        event.register(entityType, type, heightmap, predicate, RegisterSpawnPlacementsEvent.Operation.OR);
    }
}
