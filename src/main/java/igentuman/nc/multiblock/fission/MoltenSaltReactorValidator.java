package igentuman.nc.multiblock.fission;

import igentuman.nc.api.impl.CubicMultiblockValidator;
import igentuman.nc.api.multiblock.BlockPredicate;
import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.IMultiblockValidator;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class MoltenSaltReactorValidator implements IMultiblockValidator {

    private final CubicMultiblockValidator shape;

    private Block fuelCell = Blocks.AIR;

    public MoltenSaltReactorValidator() {
        int min = Multiblocks.msrMinSize;
        int max = Multiblocks.msrMaxSize;
        this.shape = new CubicMultiblockValidator(
                (state, be) -> state.is(blockOf("msr_controller")),
                BlockPredicate.ofTag(FissionTags.CASING),
                (state, be) -> state.is(blockOf("msr_fuel_cell")),
                min, max, min, max, min, max);
    }

    @Override
    public boolean validate(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache) {
        if (!(cache instanceof MsrCache mc)) {
            return shape.validate(level, controllerPos, facing, cache);
        }
        if (!shape.validate(level, controllerPos, facing, mc)) {
            mc.resetStats();
            return false;
        }
        mc.resetStats();
        fuelCell = blockOf("msr_fuel_cell");
        classify(level, mc);
        computeDimensions(mc);
        mc.fuelCellCount = mc.fuelCells.size();
        return true;
    }

    private void classify(Level level, MsrCache mc) {
        for (long key : mc.getStructurePositions()) {
            BlockPos p = BlockPos.of(key);
            if (mc.getBlockState(level, p).getBlock() == fuelCell) {
                mc.fuelCells.add(key);
            }
        }
    }

    private void computeDimensions(MsrCache mc) {
        boolean first = true;
        int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
        for (long key : mc.getStructurePositions()) {
            BlockPos p = BlockPos.of(key);
            if (first) {
                minX = maxX = p.getX();
                minY = maxY = p.getY();
                minZ = maxZ = p.getZ();
                first = false;
            } else {
                minX = Math.min(minX, p.getX());
                minY = Math.min(minY, p.getY());
                minZ = Math.min(minZ, p.getZ());
                maxX = Math.max(maxX, p.getX());
                maxY = Math.max(maxY, p.getY());
                maxZ = Math.max(maxZ, p.getZ());
            }
        }
        mc.width = maxX - minX + 1;
        mc.height = maxY - minY + 1;
        mc.depth = maxZ - minZ + 1;
    }

    private static Block blockOf(String name) {
        ModEntry entry = ModEntries.get(name);
        if (entry == null || entry.block() == null) return Blocks.AIR;
        Block b = entry.block().get();
        return b == null ? Blocks.AIR : b;
    }
}
