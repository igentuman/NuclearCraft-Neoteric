package igentuman.nc.api.multiblock;

import igentuman.nc.multiblock.MultiblockDebug;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMultiblockValidator<C extends AbstractMultiblockCache> implements IMultiblockValidator {

    public enum Outcome {
        VALID,
        INVALID,
        WAITING
    }

    public record Result(Outcome outcome,
                         @Nullable String errorKey,
                         @Nullable BlockPos errorPos,
                         @Nullable ResourceLocation expected,
                         @Nullable ResourceLocation actual) {

        public static final Result VALID = new Result(Outcome.VALID, null, null, null, null);

        public boolean valid() {
            return outcome == Outcome.VALID;
        }
    }

    private String errorKey;
    private BlockPos errorPos;
    private ResourceLocation expected;
    private ResourceLocation actual;

    public final Result run(C cache) {
        errorKey = null;
        errorPos = null;
        expected = null;
        actual = null;
        cache.resetValidationData();
        try {
            if (!findBounds(cache)) return invalidResult();
            BlockPos min = cache.workingMin();
            BlockPos max = cache.workingMax();
            if (min == null || max == null) return invalidResult();
            MultiblockDebug.bounds(min, max);
            prefetchBounds(cache, min, max);
            if (!validateShell(cache)) return invalidResult();
            if (!validateInterior(cache)) return invalidResult();
            if (!validateRelations(cache)) return invalidResult();
            calculateStatistics(cache);
            return Result.VALID;
        } catch (AbstractMultiblockCache.UnloadedInputException unloaded) {
            return new Result(Outcome.WAITING, "multiblock.validation.unloaded", unloaded.position(), null, null);
        } finally {
            cache.dropSectionInputs();
        }
    }

    @SuppressWarnings("unchecked")
    public final Result runUnchecked(AbstractMultiblockCache cache) {
        return run((C) cache);
    }

    protected void prefetchBounds(C cache, BlockPos min, BlockPos max) {
        cache.prefetch(min, max);
    }

    protected abstract boolean findBounds(C cache);

    protected abstract boolean validateShell(C cache);

    protected abstract boolean validateInterior(C cache);

    protected boolean validateRelations(C cache) {
        return true;
    }

    protected void calculateStatistics(C cache) {
    }

    protected final boolean fail(String key, BlockPos pos, @Nullable ResourceLocation expectedId, BlockState found) {
        return fail(key, pos, expectedId, BuiltInRegistries.BLOCK.getKey(found.getBlock()));
    }

    protected final boolean fail(String key, BlockPos pos, @Nullable ResourceLocation expectedId,
                                 @Nullable ResourceLocation actualId) {
        errorKey = key;
        errorPos = pos.immutable();
        expected = expectedId;
        actual = actualId;
        MultiblockDebug.fail(key, pos, expectedId, actualId);
        return false;
    }

    private Result invalidResult() {
        return new Result(Outcome.INVALID, errorKey == null ? "multiblock.validation.invalid" : errorKey,
                errorPos, expected, actual);
    }

    @Nullable
    public static Block blockOf(String name) {
        ModEntry entry = ModEntries.get(name);
        if (entry == null || entry.block() == null) return null;
        Block block = entry.block().get();
        return block == null || block == Blocks.AIR ? null : block;
    }

    protected static BlockPos worldPos(AbstractMultiblockCache cache, int dx, int dy, int dz) {
        Direction facing = cache.facing();
        return cache.controllerPos().offset(rotate(dx, dz, facing, true), dy, rotate(dx, dz, facing, false));
    }

    private static int rotate(int dx, int dz, Direction facing, boolean returnX) {
        return switch (facing) {
            case SOUTH -> returnX ? -dx : -dz;
            case WEST -> returnX ? dz : -dx;
            case EAST -> returnX ? -dz : dx;
            default -> returnX ? dx : dz;
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean validate(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache) {
        if (!(cache instanceof AbstractMultiblockCache abstractCache)) return false;
        abstractCache.bind(controllerPos, facing);
        return run((C) abstractCache).valid();
    }
}
