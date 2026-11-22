package igentuman.nc.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

import static igentuman.nc.NuclearCraft.rlFromString;
import static net.minecraft.world.level.block.Blocks.AIR;
import static net.minecraft.world.level.block.state.StateHolder.PROPERTIES_TAG;

/** Parsed structure-template NBT exposing its block layout, bounds, and required materials. */
public class MultiblockStructure {
    private final Map<BlockPos, BlockState> blocks = new HashMap<>();
    private CompoundTag nbt;
    private String name;
    private int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
    private Identifier location;

    public MultiblockStructure(CompoundTag nbt) {
        this.nbt = nbt;
        if (nbt.getList("blocks").isPresent()) {
            ListTag blocksList = nbt.getListOrEmpty("blocks");
            ListTag palette = nbt.getListOrEmpty("palette");

            for (int i = 0; i < blocksList.size(); i++) {
                CompoundTag blockTag = blocksList.getCompoundOrEmpty(i);
                CompoundTag state = palette.getCompoundOrEmpty(blockTag.getIntOr("state", 0));
                if (blockTag.get("pos") instanceof ListTag posList) {

                    if (posList.size() == 3) {
                        int x = posList.getIntOr(0, 0);
                        int y = posList.getIntOr(1, 0);
                        int z = posList.getIntOr(2, 0);
                        
                        BlockPos pos = new BlockPos(x, y, z);
                        String blockId = state.getStringOr("Name", "");
                        Identifier rl = rlFromString(blockId);
                        if (rl != null && BuiltInRegistries.BLOCK.getOptional(rl).isPresent()) {
                            Block block = BuiltInRegistries.BLOCK.getOptional(rl).get();
                            BlockState bs = block.defaultBlockState();
                            
                            // Handle block state properties if they exist
                            if (state.getCompound(PROPERTIES_TAG).isPresent()) {
                                CompoundTag properties = state.getCompoundOrEmpty(PROPERTIES_TAG);
                                for(String pKey: properties.keySet()) {
                                    for (net.minecraft.world.level.block.state.properties.Property<?> property : bs.getProperties()) {
                                        if (property.getName().equals(pKey)) {
                                            // Parse the string value to the appropriate property value
                                            String valueStr = properties.getStringOr(pKey, "");
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

    public MultiblockStructure(Identifier rl, CompoundTag nbt, String file) {
        this(nbt);
        location = rl;
        name = file;
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

    public Identifier getId() {
        return location;
    }

    public String getName() {
        if(name != null && !name.isEmpty()) {
            if(!name.contains("mbtool")) {
                return "mbtool.structure." + name.replace(".nbt", "");
            }
            return name.replace(".nbt", "");
        }
        return name;
    }

    public CompoundTag getStructureNbt() {
        return nbt;
    }

    public void setStructureNbt(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public List<ItemStack> getNeededItems() {
        List<ItemStack> outputs = new ArrayList<>();
        List<Block> blockTypes = new ArrayList<>();
        for(BlockPos pos : getBlocks().keySet()) {
            if(getBlocks().get(pos).is(AIR)) {
                continue;
            }
            Block block = getBlocks().get(pos).getBlock();
            if (!blockTypes.contains(block)) {
                blockTypes.add(block);
                outputs.add(new ItemStack(block));
            }
        }
        for(ItemStack stackItem :outputs) {
            for(Map.Entry<BlockPos, BlockState> block : getBlocks().entrySet()) {
                if(stackItem.is(block.getValue().getBlock().asItem())) {
                    stackItem.setCount(stackItem.getCount() + 1);
                }
            }
            stackItem.setCount(stackItem.getCount() - 1);
        }
        return outputs;
    }

    /**
     * Filters out all air blocks from the structure NBT.
     * This is useful for reducing packet size and ensuring air blocks are not placed.
     * 
     * @param nbt The structure NBT to filter
     * @return A new CompoundTag with air blocks removed
     */
    public static CompoundTag filterAirBlocks(CompoundTag nbt) {
        if (nbt == null || nbt.getList("blocks").isEmpty() || nbt.getList("palette").isEmpty()) {
            return nbt;
        }

        CompoundTag filteredNbt = nbt.copy();
        ListTag blocksList = nbt.getListOrEmpty("blocks");
        ListTag palette = nbt.getListOrEmpty("palette");
        
        // Find air block indices in the palette
        Set<Integer> airIndices = new HashSet<>();
        for (int i = 0; i < palette.size(); i++) {
            CompoundTag paletteEntry = palette.getCompoundOrEmpty(i);
            String blockName = paletteEntry.getStringOr("Name", "");
            if (blockName != null && (blockName.equals("minecraft:air") || 
                                      blockName.equals("minecraft:cave_air") || 
                                      blockName.equals("minecraft:void_air"))) {
                airIndices.add(i);
            }
        }
        
        // Filter out blocks that reference air palette entries
        ListTag filteredBlocksList = new ListTag();
        for (int i = 0; i < blocksList.size(); i++) {
            CompoundTag blockTag = blocksList.getCompoundOrEmpty(i);
            int stateIndex = blockTag.getIntOr("state", 0);
            if (!airIndices.contains(stateIndex)) {
                filteredBlocksList.add(blockTag);
            }
        }
        
        filteredNbt.put("blocks", filteredBlocksList);
        return filteredNbt;
    }
}