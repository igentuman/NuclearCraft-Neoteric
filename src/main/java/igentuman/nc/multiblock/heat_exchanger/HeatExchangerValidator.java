package igentuman.nc.multiblock.heat_exchanger;

import igentuman.nc.api.impl.CubicMultiblockValidator;
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

import java.util.Set;

public class HeatExchangerValidator implements IMultiblockValidator {

    private final CubicMultiblockValidator shape;

    private Block casing = Blocks.AIR;
    private Block interior = Blocks.AIR;
    private Block radiator = Blocks.AIR;

    public HeatExchangerValidator() {
        int min = Multiblocks.hxMinSize;
        int max = Multiblocks.hxMaxSize;

        Block controller = blockOf("heat_exchanger_controller");
        Block casingBlock = blockOf("heat_exchanger_casing");
        Block radiatorBlock = blockOf("heat_exchanger_radiator");
        Block hotPort = blockOf("heat_exchanger_hot_coolant_port");
        Block coldPort = blockOf("heat_exchanger_cold_coolant_port");
        Block interiorBlock = blockOf("heat_exchanger");
        Set<Block> shellBlocks = Set.of(controller, casingBlock, radiatorBlock, hotPort, coldPort);

        this.shape = new CubicMultiblockValidator(
                (state, be) -> state.is(controller),
                (state, be) -> shellBlocks.contains(state.getBlock()),
                (state, be) -> state.isAir() || state.is(interiorBlock),
                min, max, min, max, min, max);
    }

    @Override
    public boolean validate(Level level, BlockPos controllerPos, Direction facing, IMultiblockCache cache) {
        if (!(cache instanceof HeatExchangerCache hc)) {
            return shape.validate(level, controllerPos, facing, cache);
        }
        if (!shape.validate(level, controllerPos, facing, hc)) {
            hc.resetStats();
            return false;
        }
        hc.resetStats();
        casing = blockOf("heat_exchanger_casing");
        interior = blockOf("heat_exchanger");
        radiator = blockOf("heat_exchanger_radiator");

        if (!cornersAreCasing(level, hc)) {
            hc.getStructurePositions().clear();
            return false;
        }

        int n = 0;
        int rad = 0;
        for (long key : hc.getStructurePositions()) {
            Block b = hc.getBlockState(level, BlockPos.of(key)).getBlock();
            if (b == interior) n++;
            else if (b == radiator) rad++;
        }
        hc.heatExchangers = n;
        hc.radiators = rad;
        return true;
    }

    private boolean cornersAreCasing(Level level, HeatExchangerCache hc) {
        boolean first = true;
        int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
        for (long key : hc.getStructurePositions()) {
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
        if (first) return false;
        int[] xs = {minX, maxX};
        int[] ys = {minY, maxY};
        int[] zs = {minZ, maxZ};
        for (int x : xs) {
            for (int y : ys) {
                for (int z : zs) {
                    if (hc.getBlockState(level, new BlockPos(x, y, z)).getBlock() != casing) return false;
                }
            }
        }
        return true;
    }

    private static Block blockOf(String name) {
        ModEntry entry = ModEntries.get(name);
        if (entry == null || entry.block() == null) return Blocks.AIR;
        Block b = entry.block().get();
        return b == null ? Blocks.AIR : b;
    }
}
