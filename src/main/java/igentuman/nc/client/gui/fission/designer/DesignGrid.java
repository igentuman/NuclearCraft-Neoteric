package igentuman.nc.client.gui.fission.designer;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DesignGrid {

    public int sizeX;
    public int sizeY;
    public int sizeZ;
    public HashMap<BlockPos, Block> cells = new HashMap<>();
    public Set<BlockPos> invalidCells = new HashSet<>();

    public DesignGrid(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ;
    }

    public Block get(int x, int y, int z) {
        return cells.get(new BlockPos(x, y, z));
    }

    public void set(int x, int y, int z, Block block) {
        if (!inBounds(x, y, z)) {
            return;
        }
        if (block == null) {
            clear(x, y, z);
            return;
        }
        cells.put(new BlockPos(x, y, z), block);
    }

    public void clear(int x, int y, int z) {
        cells.remove(new BlockPos(x, y, z));
    }

    public void resize(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        cells.clear();
        invalidCells.clear();
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("sizeX", sizeX);
        tag.putInt("sizeY", sizeY);
        tag.putInt("sizeZ", sizeZ);
        ListTag list = new ListTag();
        for (var entry : cells.entrySet()) {
            BlockPos pos = entry.getKey();
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(entry.getValue());
            if (id == null) {
                continue;
            }
            CompoundTag cell = new CompoundTag();
            cell.putInt("x", pos.getX());
            cell.putInt("y", pos.getY());
            cell.putInt("z", pos.getZ());
            cell.putString("block", id.toString());
            list.add(cell);
        }
        tag.put("cells", list);
        return tag;
    }

    public static DesignGrid fromTag(CompoundTag tag) {
        DesignGrid grid = new DesignGrid(
                Math.max(1, tag.getInt("sizeX")),
                Math.max(1, tag.getInt("sizeY")),
                Math.max(1, tag.getInt("sizeZ")));
        ListTag list = tag.getList("cells", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag cell = list.getCompound(i);
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(cell.getString("block")));
            if (block != null) {
                grid.set(cell.getInt("x"), cell.getInt("y"), cell.getInt("z"), block);
            }
        }
        return grid;
    }
}
