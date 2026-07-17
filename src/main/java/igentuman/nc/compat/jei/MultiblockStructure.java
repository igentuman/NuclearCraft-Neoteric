package igentuman.nc.compat.jei;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static igentuman.nc.util.NcUtils.rlFromString;
import static net.minecraft.world.level.block.state.StateHolder.PROPERTIES_TAG;

public class MultiblockStructure {
    private final Map<BlockPos, BlockState> blocks = new HashMap<>();
    private int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
    
    public MultiblockStructure(CompoundTag nbt) {
        if (nbt.contains("blocks", Tag.TAG_LIST)) {
            ListTag blocksList = nbt.getList("blocks", Tag.TAG_COMPOUND);
            ListTag palette = nbt.getList("palette", Tag.TAG_COMPOUND);

            for (int i = 0; i < blocksList.size(); i++) {
                CompoundTag blockTag = blocksList.getCompound(i);
                CompoundTag state = palette.getCompound(blockTag.getInt("state"));
                if (blockTag.get("pos") instanceof ListTag posList && state.getString("Name") != null) {

                    if (posList.size() == 3) {
                        int x = posList.getInt(0);
                        int y = posList.getInt(1);
                        int z = posList.getInt(2);
                        
                        BlockPos pos = new BlockPos(x, y, z);
                        String blockId = state.getString("Name");
                        Block block = ForgeRegistries.BLOCKS.getValue(rlFromString(blockId));
                        
                        if (block != null) {
                            BlockState bs = block.defaultBlockState();
                            
                            // Handle block state properties if they exist
                            if (state.contains(PROPERTIES_TAG, Tag.TAG_COMPOUND)) {
                                CompoundTag properties = state.getCompound("Properties");
                                for(String pKey: state.getCompound("Properties").getAllKeys()) {
                                    for (net.minecraft.world.level.block.state.properties.Property<?> property : bs.getProperties()) {
                                        if (property.getName().equals(pKey)) {
                                            // Parse the string value to the appropriate property value
                                            String valueStr = properties.getString(pKey);
                                            Optional<?> value = property.getValue(valueStr);

                                            if (value.isPresent()) {
                                                bs = setPropertyValue(bs, property, value.get());
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            
                            blocks.put(pos, bs);
                            
                            minX = Math.min(minX, x);
                            minY = Math.min(minY, y);
                            minZ = Math.min(minZ, z);
                            maxX = Math.max(maxX, x);
                            maxY = Math.max(maxY, y);
                            maxZ = Math.max(maxZ, z);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <S extends BlockState, T extends Comparable<T>> S setPropertyValue(S blockState,
          net.minecraft.world.level.block.state.properties.Property<T> property, Object value) {
        return (S) blockState.setValue(property, (T) value);
    }

    public BlockState getBlockAt(BlockPos pos) {
        return blocks.get(pos);
    }
    
    public Map<BlockPos, BlockState> getBlocks() {
        return blocks;
    }
    
    public int getWidth() {
        return maxX - minX + 1;
    }
    
    public int getHeight() {
        return maxY - minY + 1;
    }
    
    public int getDepth() {
        return maxZ - minZ + 1;
    }
    
    public BlockPos getCenter() {
        return new BlockPos((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
    }
    
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }
}