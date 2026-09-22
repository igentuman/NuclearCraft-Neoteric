package igentuman.nc.block_entity;

import igentuman.nc.config.Common;
import igentuman.nc.setup.ModEntries;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.api.isotope.IsotopeStack;
import igentuman.nr.radiation.source.BlockRadSource;
import igentuman.nr.radiation.source.WorldSourceRegistry;
import igentuman.nr.radiation.storage.ChunkRadiationData;
import igentuman.nr.radiation.storage.NRAttachments;
import igentuman.nr.registry.Isotopes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.UUID;

public class GasScrubberBE extends UniversalProcessorBE {

    private static final int CLEAN_RADIUS_CHUNKS = 4;

    private BlockRadSource scrubberSource;
    private double registeredReductionBq;

    public GasScrubberBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    @Override
    public void serverTick() {
        boolean mayRun = name != null
                && ModEntries.isEnabled(name)
                && level != null
                && (redstoneMode != 1 || level.hasNeighborSignal(worldPosition));

        super.serverTick();

        if (!mayRun || !recipeInfo.active || !(level instanceof ServerLevel server)) {
            clearScrubberSource();
            return;
        }

        assertScrubberSource(server);
        clearAirContamination(server);
    }

    private void assertScrubberSource(ServerLevel server) {
        double reductionBq = Common.GAS_SCRUBBER_RADIATION_REDUCTION_BQ.get();
        if (reductionBq <= 0D) {
            clearScrubberSource();
            return;
        }

        WorldSourceRegistry registry = WorldSourceRegistry.get(server);
        if (scrubberSource != null
                && registry.atBlock(worldPosition) == scrubberSource
                && Double.compare(registeredReductionBq, reductionBq) == 0) {
            return;
        }

        clearScrubberSource();
        RadiationProfile profile = reductionProfile(reductionBq, server.getGameTime());
        if (profile.isEmpty()) return;

        scrubberSource = new BlockRadSource(
                UUID.randomUUID(), server.dimension(), worldPosition.immutable(),
                profile, server.getGameTime(), false);
        registeredReductionBq = reductionBq;
        registry.register(scrubberSource);
    }

    private void clearAirContamination(ServerLevel server) {
        ChunkPos center = new ChunkPos(worldPosition);
        long now = server.getGameTime();
        for (int dx = -CLEAN_RADIUS_CHUNKS; dx <= CLEAN_RADIUS_CHUNKS; dx++) {
            for (int dz = -CLEAN_RADIUS_CHUNKS; dz <= CLEAN_RADIUS_CHUNKS; dz++) {
                LevelChunk chunk = server.getChunkSource().getChunkNow(center.x + dx, center.z + dz);
                if (chunk == null) continue;
                ChunkRadiationData data = chunk.getData(NRAttachments.CHUNK_RADIATION.get());
                if (data.air().isEmpty()) continue;
                data.setAir(RadiationProfile.empty());
                data.setLastDecayTick(now);
                chunk.setUnsaved(true);
            }
        }
    }

    private static RadiationProfile reductionProfile(double reductionBq, long now) {
        RadiationProfile profile = new RadiationProfile();
        Isotope isotope = IsotopeRegistry.get(Isotopes.PA_91);
        if (isotope == null) return profile;
        double activityPerAtom = new IsotopeStack(isotope, 1D, now).currentActivityBq();
        if (activityPerAtom <= 0D) return profile;
        profile.put(new IsotopeStack(isotope, -reductionBq / activityPerAtom, now));
        return profile;
    }

    private void clearScrubberSource() {
        if (scrubberSource != null && level instanceof ServerLevel server) {
            WorldSourceRegistry.get(server).remove(scrubberSource.getId());
        }
        scrubberSource = null;
        registeredReductionBq = 0D;
    }

    @Override
    public void setRemoved() {
        clearScrubberSource();
        super.setRemoved();
    }
}
