package igentuman.nc.handler.radiation;

import com.google.common.collect.Table;
import igentuman.nc.util.annotation.NothingNullByDefault;
import igentuman.nc.util.functions.Chunk3D;
import igentuman.nc.util.functions.Coord4D;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;


@NothingNullByDefault
public interface IRadiationManager {

    /**
     * Helper to expose the ability to check if Nuclearcraft's radiation system is enabled in the config.
     */
    boolean isRadiationEnabled();

    /**
     * Helper to access Nuclearcraft's internal radiation damage source.
     *
     * @return Damage source used for radiation.
     */
    DamageSource getRadiationDamageSource();

    /**
     * Get the radiation level (in Sv/h) at a certain location.
     *
     * @param coord Location
     *
     * @return radiation level (in Sv).
     */
    double getRadiationLevel(Coord4D coord);

    double getRadiationLevel(Entity entity);

    /**
     * Gets an unmodifiable table of the radiation sources tracked by this manager. This table keeps track of radiation sources on both a chunk and position based level.
     *
     * @return Unmodifiable table of radiation sources.
     */
    Table<Chunk3D, Coord4D, IRadiationSource> getRadiationSources();

    /**
     * Removes all radiation sources in a given chunk.
     *
     * @param chunk Chunk to clear radiation sources of.
     */
    void removeRadiationSources(Chunk3D chunk);

    /**
     * Removes the radiation source at the given location.
     *
     * @param coord Location.
     */
    void removeRadiationSource(Coord4D coord);

    /**
     * Applies a radiation source (Sv) of the given magnitude to a given location.
     *
     * @param coord     Location to release radiation.
     * @param magnitude Amount of radiation to apply (Sv).
     */
    void radiate(Coord4D coord, double magnitude);

    /**
     * Applies an additional magnitude of radiation (Sv) to the given entity after taking into account the radiation resistance provided to the entity by its armor.
     *
     * @param entity    The entity to radiate.
     * @param magnitude Dosage of radiation to apply before radiation resistance (Sv).
     *
     * @implNote This method does not add any radiation to players in creative or spectator.
     */
    void radiate(LivingEntity entity, double magnitude);

}