package igentuman.nc.block_entity;

import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.FissionShellBuilder;
import igentuman.nc.util.TextUtils;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static igentuman.nc.util.TextUtils.__;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.WEST;
import static net.minecraft.core.Direction.UP;

public class MultiblockBuilderBE extends BlockEntity {

    public HashMap<BlockPos, Block> blockMap = new HashMap<>();
    private Direction facing;

    public MultiblockBuilderBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state);
    }

    public static Vec3i getSize(HashMap<BlockPos, Block> blockMap) {
        return FissionShellBuilder.getSize(blockMap);
    }

    public record PlanStatus(Map<BlockPos, BlockState> preview, List<BlockPos> blocked) {}

    public PlanStatus getPlanStatus() {
        if (blockMap.isEmpty() || getLevel() == null) return new PlanStatus(Map.of(), List.of());

        HashMap<BlockPos, Block> planMap = shiftToInterior(blockMap);
        Vec3i size = getSize(planMap);
        fillShellBlocks(planMap, size);
        BlockPos globalPos = computeGlobalOrigin(planMap);

        Map<BlockPos, BlockState> preview = new HashMap<>();
        List<BlockPos> blocked = new ArrayList<>();

        for (Map.Entry<BlockPos, Block> entry : planMap.entrySet()) {
            BlockPos placementPos = toGlobalPos(globalPos, entry.getKey());
            BlockState existing = getLevel().getBlockState(placementPos);
            BlockState state = stateFor(entry);
            if (existing.getBlock() == state.getBlock()) continue;

            BlockPos offset = placementPos.subtract(getBlockPos());
            if (!existing.isAir() && !existing.canBeReplaced()) {
                blocked.add(offset);
            } else {
                preview.put(offset, state);
            }
        }

        return blocked.isEmpty() ? new PlanStatus(preview, blocked) : new PlanStatus(Map.of(), blocked);
    }

    private BlockPos computeGlobalOrigin(HashMap<BlockPos, Block> blockMap) {
        Vec3i size = getSize(blockMap);
        int offset = switch (getFacing()) {
            case NORTH -> 1;
            case SOUTH -> size.getZ() + 2;
            case EAST -> size.getX() + 2;
            case WEST -> 1;
            default -> 0;
        };
        return getBlockPos().relative(getFacing().getOpposite(), offset);
    }

    private BlockState stateFor(Map.Entry<BlockPos, Block> entry) {
        Block block = entry.getValue();
        if (entry.getKey().equals(BlockPos.ZERO)) {
            Direction controllerFacing = switch (getFacing()) {
                case NORTH, SOUTH -> NORTH;
                case EAST, WEST -> WEST;
                default -> UP;
            };
            return block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, controllerFacing);
        }
        return block.defaultBlockState();
    }

    public boolean build(HashMap<BlockPos, Block> blockMap, Player player) {
        if (getLevel() == null || getLevel().isClientSide()) return false;

        HashMap<BlockPos, Block> planMap = shiftToInterior(blockMap);
        Vec3i size = getSize(planMap);
        fillShellBlocks(planMap, size);
        BlockPos globalPos = computeGlobalOrigin(planMap);

        Map<Item, Integer> required = new HashMap<>();
        for (Map.Entry<BlockPos, Block> entry : planMap.entrySet()) {
            BlockPos placementPos = toGlobalPos(globalPos, entry.getKey());
            Block block = entry.getValue();
            BlockState existing = getLevel().getBlockState(placementPos);

            if (existing.getBlock() == block) continue;

            if (!existing.isAir() && !existing.canBeReplaced()) {
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
            placeBlocks(planMap, globalPos);
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

        placeBlocks(planMap, globalPos);

        if (player != null) {
            player.sendSystemMessage(TextUtils.applyFormat(
                    __("nc.multiblock_builder.build_success"), ChatFormatting.GREEN));
        }
        return true;
    }

    private HashMap<BlockPos, Block> shiftToInterior(HashMap<BlockPos, Block> blockMap) {
        return FissionShellBuilder.shiftToInterior(blockMap);
    }

    private void fillShellBlocks(HashMap<BlockPos, Block> blockMap, Vec3i size) {
        FissionShellBuilder.fillShellBlocks(blockMap, size);
    }

    private BlockPos toGlobalPos(BlockPos globalPos, BlockPos localPos) {
        BlockPos placementPos = globalPos.relative(UP, localPos.getY());
        switch (getFacing()) {
            case NORTH -> placementPos = placementPos.relative(SOUTH, localPos.getZ()).relative(WEST, -localPos.getX());
            case SOUTH -> placementPos = placementPos.relative(NORTH, -localPos.getZ()).relative(WEST, -localPos.getX());
            case EAST -> placementPos = placementPos.relative(WEST, -localPos.getX()).relative(NORTH, -localPos.getZ());
            case WEST -> placementPos = placementPos.relative(EAST, localPos.getX()).relative(SOUTH, localPos.getZ());
            default -> {}
        }
        return placementPos;
    }

    private void placeBlocks(HashMap<BlockPos, Block> blockMap, BlockPos globalPos) {
        for (Map.Entry<BlockPos, Block> entry : blockMap.entrySet()) {
            BlockPos placementPos = toGlobalPos(globalPos, entry.getKey());
            BlockState state = stateFor(entry);
            BlockState existing = getLevel().getBlockState(placementPos);
            if (existing.getBlock() == state.getBlock()) continue;

            getLevel().setBlock(placementPos, state, 3);
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
                IItemHandler handler = getLevel().getCapability(Capabilities.ItemHandler.BLOCK, neighbor, null);
                if (handler != null) {
                    handlers.add(handler);
                    queue.add(neighbor);
                }
            }
        }
        return handlers;
    }

    private Direction getFacing() {
        if (facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
    }
}
