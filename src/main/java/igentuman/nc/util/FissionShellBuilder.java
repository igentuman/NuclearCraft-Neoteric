package igentuman.nc.util;

import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

/** Wraps a fission reactor block layout in casing/glass shell blocks with a single controller corner. */
public class FissionShellBuilder {

    private static Block block(String name) {
        return ModEntries.get(name).block().get();
    }

    public static HashMap<BlockPos, Block> shiftToInterior(HashMap<BlockPos, Block> blockMap) {
        HashMap<BlockPos, Block> shifted = new HashMap<>();
        for (Map.Entry<BlockPos, Block> entry : blockMap.entrySet()) {
            shifted.put(entry.getKey().offset(1, 1, 1), entry.getValue());
        }
        return shifted;
    }

    public static Vec3i getSize(HashMap<BlockPos, Block> blockMap) {
        if (blockMap.isEmpty()) {
            return new Vec3i(1, 1, 1);
        }
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : blockMap.keySet()) {
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return new Vec3i(maxX, maxY, maxZ);
    }

    public static void fillShellBlocks(HashMap<BlockPos, Block> blockMap, Vec3i size) {
        for (int x = 0; x <= size.getX() + 1; x++) {
            for (int y = 0; y <= size.getY() + 1; y++) {
                for (int z = 0; z <= size.getZ() + 1; z++) {
                    if ((x == 0 || x == size.getX() + 1) || (y == 0 || y == size.getY() + 1) || (z == 0 || z == size.getZ() + 1)) {
                        if (x == 0 && z == 0 && y == 0) {
                            blockMap.put(new BlockPos(x, y, z), block("fission_reactor_controller"));
                        } else if (isCorner(x, y, z, size.getX() + 1, size.getY() + 1, size.getZ() + 1)) {
                            blockMap.put(new BlockPos(x, y, z), block("fission_reactor_casing"));
                        } else {
                            blockMap.put(new BlockPos(x, y, z), block("fission_reactor_glass"));
                        }
                    }
                }
            }
        }
    }

    public static boolean isCorner(int x, int y, int z, int maxX, int maxY, int maxZ) {
        return (x == 0 || x == maxX) && (y == 0 || y == maxY) ||
               (x == 0 || x == maxX) && (z == 0 || z == maxZ) ||
               (y == 0 || y == maxY) && (z == 0 || z == maxZ);
    }

    public static CompoundTag toStructureNbt(HashMap<BlockPos, Block> blockMap) {
        ListTag blocksList = new ListTag();
        ListTag palette = new ListTag();
        Map<Block, Integer> paletteMap = new HashMap<>();

        for (Map.Entry<BlockPos, Block> entry : blockMap.entrySet()) {
            Block block = entry.getValue();
            if (block == null) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null) {
                continue;
            }
            Integer paletteIndex = paletteMap.get(block);
            if (paletteIndex == null) {
                paletteIndex = palette.size();
                CompoundTag paletteEntry = new CompoundTag();
                paletteEntry.putString("Name", id.toString());
                palette.add(paletteEntry);
                paletteMap.put(block, paletteIndex);
            }

            BlockPos pos = entry.getKey();
            ListTag posList = new ListTag();
            posList.add(IntTag.valueOf(pos.getX()));
            posList.add(IntTag.valueOf(pos.getY()));
            posList.add(IntTag.valueOf(pos.getZ()));

            CompoundTag blockTag = new CompoundTag();
            blockTag.put("pos", posList);
            blockTag.putInt("state", paletteIndex);
            blocksList.add(blockTag);
        }

        CompoundTag nbt = new CompoundTag();
        nbt.put("blocks", blocksList);
        nbt.put("palette", palette);
        return nbt;
    }
}
