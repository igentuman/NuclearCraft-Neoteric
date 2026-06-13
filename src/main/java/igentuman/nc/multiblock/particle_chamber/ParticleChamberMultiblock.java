package igentuman.nc.multiblock.particle_chamber;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.HashSet;

import static igentuman.nc.NuclearCraft.debugLog;

/**
 * Shared base for particle chamber multiblock validators.
 * Target chamber, decay chamber, collision chamber all extend this.
 */
public abstract class ParticleChamberMultiblock extends AbstractMultiblock {

    public final HashMap<Long, BlockEntity> beamPorts = new HashMap<>();
    public double efficiency = 0;
    public int power = 0;

    protected ParticleChamberMultiblock(HashSet<Block> validOuterBlocks, HashSet<Block> validInnerBlocks, MultiblockController controller) {
        super(validOuterBlocks, validInnerBlocks, controller);
    }

    /**
     * All particle chambers must form an odd-sided cube and use the controller's facing direction.
     */
    @Override
    public void validateOuter() {
        debugLog("Particle chamber outer validation for " + getClass().getSimpleName());
        super.validateOuter();
        if (!validationResult.isValid) {
            debugLog("Base outer validation failed: " + validationResult);
            return;
        }

        if (requireCubeShape()) {
            if (height() % 2 == 0 || width() % 2 == 0 || depth() % 2 == 0
                    || height() != width() || height() != depth() || width() != depth()) {
                debugLog("Cube proportion check failed: " + width() + "x" + height() + "x" + depth());
                validationResult = ValidationResult.WRONG_PROPORTIONS;
                outerValid = false;
                return;
            }
        }

        outerValid = true;
        validationResult = ValidationResult.VALID;
    }

    /** Override to false for non-cube chamber shapes (e.g. asymmetric collision). */
    protected boolean requireCubeShape() {
        return true;
    }

    @Override
    protected Direction getControllerDirection() {
        if (controllerBE() != null && controllerBE().getBlockState() != null) {
            try {
                return controllerBE().getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
            } catch (Exception ignored) { }
        }
        return null;
    }

    @Override
    public void removeFromCacheIfChanged(BlockPos pos) {
        long packedPos = pos.asLong();
        if (beCache.containsKey(packedPos)) {
            BlockEntity be = getLevel().getExistingBlockEntity(pos);
            if (be != beCache.get(packedPos) || (be != null && be.isRemoved())) {
                beCache.remove(packedPos);
            }
        }
        if (bsCache.containsKey(packedPos)) {
            net.minecraft.world.level.block.state.BlockState bs = getLevel().getBlockState(pos);
            net.minecraft.world.level.block.state.BlockState cachedState = bsCache.get(packedPos);
            if (cachedState == null || !bs.is(cachedState.getBlock())) {
                bsCache.remove(packedPos);
                onCachedBlockRemoved(packedPos);
            }
        }
    }

    /** Subclasses can drop additional tracked positions (detectors, ports, etc). */
    protected void onCachedBlockRemoved(long packedPos) {
    }

    /** Push an output particle into the i-th output beam port. Default no-op. */
    public void extractParticle(int outputIndex, ParticleStack stack) {
    }

    @Override
    public void clearStats() {
        efficiency = 0;
        power = 0;
        beamPorts.clear();
    }
}
