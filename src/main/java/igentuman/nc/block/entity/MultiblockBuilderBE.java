package igentuman.nc.block.entity;

import igentuman.nc.block.storage.entity.ContainerBE;
import igentuman.nc.handler.event.client.BlockOverlayHandler;
import igentuman.nc.util.TextUtils;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.builder.MultiblockRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static igentuman.nc.block.RedstoneDimmerBlock.HORIZONTAL_FACING;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.MULTIBLOCK_BUILDER_BE;
import static igentuman.nc.util.TextUtils.__;
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

    public boolean build(HashMap<BlockPos, Block> blockMap, Player player) {
        if (getLevel() == null || getLevel().isClientSide()) return false;

        Vec3i size = getSize(blockMap);
        fillShellBlocks(blockMap, size);

        int offset = switch (getFacing()) {
            case NORTH -> 1;
            case SOUTH -> size.getZ()+2;
            case EAST -> size.getX()+2;
            case WEST -> 1;
            default -> 0;
        };
        BlockPos globalPos = getBlockPos().relative(getFacing().getOpposite(), offset);

        Map<Item, Integer> required = new HashMap<>();
        for (Map.Entry<BlockPos, Block> entry : blockMap.entrySet()) {
            BlockPos placementPos = toGlobalPos(globalPos, entry.getKey());
            Block block = entry.getValue();
            BlockState existing = getLevel().getBlockState(placementPos);

            if (existing.getBlock() == block) continue;

            if (!existing.isAir() && !existing.getMaterial().isReplaceable()) {
                if (player != null) {
                    player.sendSystemMessage(TextUtils.applyFormat(
                            __("nc.multiblock_builder.area_blocked",
                                    placementPos.getX(), placementPos.getY(), placementPos.getZ()),
                            ChatFormatting.RED));
                }
                return false;
            }

            Item item = block.asItem();
            if (item != Items.AIR) {
                required.merge(item, 1, Integer::sum);
            }
        }

        if (required.isEmpty()) {
            placeBlocks(blockMap, globalPos);
            return true;
        }

        List<IItemHandler> containers = findConnectedContainers();
        if (containers.isEmpty()) {
            if (player != null) {
                player.sendSystemMessage(TextUtils.applyFormat(
                        __("nc.multiblock_builder.no_containers"), ChatFormatting.RED));
            }
            return false;
        }

        Map<Item, Integer> available = new HashMap<>();
        for (IItemHandler handler : containers) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    available.merge(stack.getItem(), stack.getCount(), Integer::sum);
                }
            }
        }

        boolean missing = false;
        for (Map.Entry<Item, Integer> e : required.entrySet()) {
            int have = available.getOrDefault(e.getKey(), 0);
            if (have < e.getValue()) {
                missing = true;
                if (player != null) {
                    player.sendSystemMessage(TextUtils.applyFormat(
                            __("nc.multiblock_builder.missing_blocks",
                                    new ItemStack(e.getKey()).getHoverName().getString(),
                                    e.getValue() - have),
                            ChatFormatting.RED));
                }
            }
        }
        if (missing) return false;

        for (Map.Entry<Item, Integer> e : required.entrySet()) {
            int toExtract = e.getValue();
            for (IItemHandler handler : containers) {
                for (int i = 0; i < handler.getSlots() && toExtract > 0; i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack.getItem() == e.getKey()) {
                        int ex = Math.min(toExtract, stack.getCount());
                        handler.extractItem(i, ex, false);
                        toExtract -= ex;
                    }
                }
            }
        }

        placeBlocks(blockMap, globalPos);

        if (player != null) {
            player.sendSystemMessage(TextUtils.applyFormat(
                    __("nc.multiblock_builder.build_success"), ChatFormatting.GREEN));
        }
        return true;
    }

    private void fillShellBlocks(HashMap<BlockPos, Block> blockMap, Vec3i size) {
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
    }

    private BlockPos toGlobalPos(BlockPos globalPos, BlockPos localPos) {
        BlockPos placementPos = globalPos.relative(UP, localPos.getY());
        switch (getFacing()) {
            case NORTH -> placementPos = placementPos.relative(SOUTH, localPos.getZ()).relative(WEST, -localPos.getX());
            case SOUTH -> placementPos = placementPos.relative(NORTH, -localPos.getZ()).relative(WEST, -localPos.getX());
            case EAST -> placementPos = placementPos.relative(WEST, -localPos.getX()).relative(NORTH, -localPos.getZ());
            case WEST -> placementPos = placementPos.relative(EAST, localPos.getX()).relative(SOUTH, localPos.getZ());
        }
        return placementPos;
    }

    private void placeBlocks(HashMap<BlockPos, Block> blockMap, BlockPos globalPos) {
        for (Map.Entry<BlockPos, Block> entry : blockMap.entrySet()) {
            BlockPos placementPos = toGlobalPos(globalPos, entry.getKey());
            Block block = entry.getValue();
            BlockState existing = getLevel().getBlockState(placementPos);
            if (existing.getBlock() == block) continue;

            if (entry.getKey().equals(BlockPos.ZERO)) {
                Direction controllerFacing = switch (getFacing()) {
                    case NORTH -> NORTH;
                    case SOUTH -> NORTH;
                    case EAST -> WEST;
                    case WEST -> WEST;
                    default -> UP;
                };
                getLevel().setBlock(placementPos, block.defaultBlockState().setValue(HORIZONTAL_FACING, controllerFacing), 3);
            } else {
                getLevel().setBlock(placementPos, block.defaultBlockState(), 3);
            }
        }
    }

    private List<IItemHandler> findConnectedContainers() {
        List<IItemHandler> handlers = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(getBlockPos());
        visited.add(getBlockPos());
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (visited.contains(neighbor)) continue;
                visited.add(neighbor);
                BlockEntity be = getLevel().getExistingBlockEntity(neighbor);
                if (be instanceof ContainerBE container) {
                    container.getItemHandler().ifPresent(handlers::add);
                    queue.add(neighbor);
                }
            }
        }
        return handlers;
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
