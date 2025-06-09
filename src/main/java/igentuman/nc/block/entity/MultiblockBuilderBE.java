package igentuman.nc.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.handler.event.client.BlockOverlayHandler;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.builder.MultiblockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;

import static igentuman.nc.block.RedstoneDimmerBlock.HORIZONTAL_FACING;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.MULTIBLOCK_BUILDER_BE;
import static net.minecraft.core.Direction.*;

public class MultiblockBuilderBE extends NuclearCraftBE {

    @NBTField
    public int output = 0;
    public HashMap<BlockPos, Block> blockMap = new HashMap<>();

    private Direction facing;

    public MultiblockBuilderBE(BlockPos pos, BlockState pBlockState) {
        super(MULTIBLOCK_BUILDER_BE.get(), pos, pBlockState);
    }

    public void tickClient() {
        if(isRemoved() || blockMap.isEmpty()) {
            return;
        }
        Vec3i size = MultiblockRenderer.getSize(blockMap);
        AABB boundingBox = new AABB(0,0,0, size.getX()+2, size.getY()+2, size.getZ()+2);
        int offset = switch (getFacing()) {
            case NORTH -> 1;
            case SOUTH -> size.getZ()+2;
            case EAST -> size.getX()+2;
            case WEST -> 1;
            default -> 0;
        };
        BlockOverlayHandler.addBoxToOutline(boundingBox,  0.5f, 0.9f, 0.9f, 0.8f, getBlockPos().relative(getFacing().getOpposite(), offset));
    }

    public void tickServer() {
        if (getLevel().getGameTime() % 2 == 0) {
            return;
        }
    }

    public void setBlockMap(HashMap<BlockPos, Block> blockMap) {
        if (getLevel().isClientSide()) {
            removeOverlayBox();
        }
        this.blockMap = blockMap;
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

        int offset = switch (getFacing()) {
            case NORTH -> 1;
            case SOUTH -> size.getZ()+2;
            case EAST -> size.getX()+2;
            case WEST -> 1;
            default -> 0;
        };
        BlockPos globalPos = getBlockPos().relative(getFacing().getOpposite(), offset);
        for(Map.Entry<BlockPos, Block> entry : blockMap.entrySet()) {
            BlockPos localPos = entry.getKey();
            BlockPos placementPos = globalPos.relative(UP, localPos.getY());

            switch (getFacing()) {
                case NORTH -> {
                    placementPos = placementPos.relative(SOUTH, localPos.getZ()).relative(WEST, -localPos.getX());
                }
                case SOUTH -> {
                    placementPos = placementPos.relative(NORTH, -localPos.getZ()).relative(WEST, -localPos.getX());
                }
                case EAST -> {
                    placementPos = placementPos.relative(WEST, -localPos.getX()).relative(NORTH, -localPos.getZ());
                }
                case WEST -> {
                    placementPos = placementPos.relative(EAST, localPos.getX()).relative(SOUTH, localPos.getZ());
                }
            }

            if(localPos.equals(BlockPos.ZERO)) {
                Direction controllerFacing = switch (getFacing()) {
                    case NORTH -> NORTH;
                    case SOUTH -> NORTH;
                    case EAST -> WEST;
                    case WEST -> WEST;
                    default -> UP; // Fallback, should not happen
                };
                getLevel().setBlock(placementPos, entry.getValue().defaultBlockState().setValue(HORIZONTAL_FACING, controllerFacing), 3);
            } else {
                getLevel().setBlock(placementPos, entry.getValue().defaultBlockState(), 3);
            }
        }
    }

    public void removeOverlayBox() {
        Vec3i size = MultiblockRenderer.getSize(blockMap);
        int offset = switch (getFacing()) {
            case NORTH -> 1;
            case SOUTH -> size.getZ()+2;
            case EAST -> size.getX()+2;
            case WEST -> 1;
            default -> 0;
        };
        BlockOverlayHandler.removeBoxFromOutline(getBlockPos().relative(getFacing().getOpposite(), offset));
    }

    @Override
    public void setRemoved() {
        if (getLevel().isClientSide()) {
            removeOverlayBox();
        }
        super.setRemoved();
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
