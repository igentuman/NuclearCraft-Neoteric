package igentuman.nc.block.entity;

import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

import static igentuman.nc.block.RedstoneDimmerBlock.HORIZONTAL_FACING;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.MULTIBLOCK_BUILDER_BE;
import static net.minecraft.core.Direction.*;

public class MultiblockBuilderBE extends NuclearCraftBE {

    @NBTField
    public int output = 0;

    private Direction facing;

    public MultiblockBuilderBE(BlockPos pos, BlockState pBlockState) {
        super(MULTIBLOCK_BUILDER_BE.get(), pos, pBlockState);
    }

    public void tickClient() {
    }

    public void tickServer() {
        if (getLevel().getGameTime() % 2 == 0) {
            return;
        }

    }

    private int getRightSignal() {
        return getLevel().getSignal(getBlockPos().relative(facing.getCounterClockWise()), facing);
    }

    private int getLeftSignal() {
        return getLevel().getSignal(getBlockPos().relative(facing.getClockWise()), facing);
    }

    public static Vec3i getSize(HashMap<BlockPos, Block> blockMap) {
        if (blockMap.isEmpty()) {
            return new Vec3i(1, 1, 1);
        }

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : blockMap.keySet()) {
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        return new Vec3i(maxX, maxY, maxZ);
    }

    public void build(HashMap<BlockPos, Block> blockMap) {
        Vec3i size = getSize(blockMap);
        for(int x = 0; x <= size.getX()+1; x++) {
            for(int y = 0; y <= size.getY()+1; y++) {
                for(int z = 0; z <= size.getZ()+1; z++) {
                    if((x == 0 || x == size.getX()+1) || (y == 0 || y == size.getY()+1) || (z == 0 || z == size.getZ()+1)) {
                        if(x == 0 && z == 0 && y == 0) {
                            blockMap.put(new BlockPos(x, y, z), FISSION_BLOCKS.get("fission_reactor_controller").get());
                        } else if(isCorner(x, y, z, size.getX()+1, size.getY()+1, size.getZ()+1)) {
                            blockMap.put(new BlockPos(x, y, z), FISSION_BLOCKS.get("fission_reactor_casing").get());
                        } else {
                            blockMap.put(new BlockPos(x, y, z), FISSION_BLOCKS.get("fission_reactor_glass").get());
                        }
                    }
                }
            }
        }
        for(Map.Entry<BlockPos, Block> entry : blockMap.entrySet()) {
            BlockPos localPos = entry.getKey();
            BlockPos globalPos = getBlockPos().relative(UP, localPos.getY());
            switch (getFacing()) {
                case NORTH -> {
                    globalPos = globalPos.relative(SOUTH, -localPos.getZ()-1).relative(WEST, -localPos.getX()-1);
                }
                case SOUTH -> {
                    globalPos = globalPos.relative(NORTH, localPos.getZ()+1).relative(WEST, localPos.getX()+1);
                }
                case EAST -> {
                    globalPos = globalPos.relative(WEST, -localPos.getX()-1).relative(NORTH, -localPos.getZ()-1);
                }
                case WEST -> {
                    globalPos = globalPos.relative(EAST, localPos.getX()+1).relative(NORTH, localPos.getZ()+1);
                }
            }

            if(localPos.equals(BlockPos.ZERO)) {
                getLevel().setBlock(globalPos, entry.getValue().defaultBlockState().setValue(HORIZONTAL_FACING, getFacing()), 3);
            } else {
                getLevel().setBlock(globalPos, entry.getValue().defaultBlockState(), 3);
            }
        }
    }

    /**
     * Check if a position is a corner of the structure
     */
    private boolean isCorner(int x, int y, int z, int maxX, int maxY, int maxZ) {
        return (x == 0 || x == maxX) && (y == 0 || y == maxY) ||
               (x == 0 || x == maxX) && (z == 0 || z == maxZ) ||
               (y == 0 || y == maxY) && (z == 0 || z == maxZ);
    }

    private Direction getFacing() {
        if (facing == null) {
            facing = getBlockState().getValue(HORIZONTAL_FACING);
        }
        return facing;
    }
}
