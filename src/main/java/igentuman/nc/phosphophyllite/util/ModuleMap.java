package igentuman.nc.phosphophyllite.util;

import igentuman.nc.phosphophyllite.modular.api.IModularTile;
import igentuman.nc.phosphophyllite.modular.api.TileModule;
import igentuman.nc.util.joml.Vector3i;
import igentuman.nc.util.joml.Vector3ic;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModuleMap<ModuleType extends TileModule<TileType>, TileType extends BlockEntity & IModularTile> {
    
    private final ObjectArrayList<ModuleType> modules;
    @SuppressWarnings("unchecked")
    private final ObjectArrayList<TileType> tiles = ObjectArrayList.wrap((TileType[]) new BlockEntity[0]);
    private final LongArrayList poses = new LongArrayList();
    private final Long2IntLinkedOpenHashMap indexMap = new Long2IntLinkedOpenHashMap();
    
    {
        indexMap.defaultReturnValue(-1);
    }
    
    public ModuleMap(ModuleType[] moduleArray) {
        modules = ObjectArrayList.wrap(moduleArray);
    }
    
    public boolean addModule(final ModuleType module) {
        final TileType tile = module.iface;
        final long posLong = tile.getBlockPos().asLong();
        final int previousIndex = indexMap.put(posLong, modules.size());
        // dont duplicate positions, just overwrite it
        if (previousIndex != -1) {
            indexMap.put(posLong, previousIndex);
            final var oldModule = modules.set(previousIndex, module);
            tiles.set(previousIndex, tile);
            poses.set(previousIndex, posLong);
            return oldModule != module;
        }
        modules.add(module);
        tiles.add(tile);
        poses.add(tile.getBlockPos().asLong());
        return true;
    }
    
    public void addAll(ModuleMap<ModuleType, TileType> otherMap) {
        otherMap.forEachModule(this::addModule);
    }
    
    public boolean removeModule(final ModuleType module) {
        final TileType tile = module.iface;
        final long posLong = tile.getBlockPos().asLong();
        final int index = indexMap.remove(posLong);
        if (index == -1) {
            return false;
        }
        final var previousEndModule = modules.remove(modules.size() - 1);
        final var previousEndTile = tiles.remove(tiles.size() - 1);
        final var previousEndPos = poses.removeLong(poses.size() - 1);
        if (index != modules.size()) {
            // shuffle the end to our current position
            indexMap.put(previousEndPos, index);
            modules.set(index, previousEndModule);
            tiles.set(index, previousEndTile);
            poses.set(index, previousEndPos);
        }
        return true;
    }
    
    public boolean containsTile(TileType tile) {
        return containsPos(tile.getBlockPos());
    }
    
    public boolean containsModule(ModuleType module) {
        return containsPos(module.iface.getBlockPos());
    }
    
    public boolean containsPos(BlockPos pos) {
        return getModule(pos) != null;
    }
    
    public boolean containsPos(Vector3i pos) {
        return getModule(pos) != null;
    }
    
    @Nullable
    public ModuleType getModule(int x, int y, int z) {
        int index = indexMap.get(BlockPos.asLong(x, y, z));
        if (index == -1) {
            return null;
        }
        return modules.get(index);
    }
    
    @Nullable
    public ModuleType getModule(BlockPos pos) {
        return getModule(pos.getX(), pos.getY(), pos.getZ());
    }
    
    @Nullable
    public ModuleType getModule(Vector3ic pos) {
        return getModule(pos.x(), pos.y(), pos.z());
    }
    
    @Nullable
    public TileType getTile(int x, int y, int z) {
        int index = indexMap.get(BlockPos.asLong(x, y, z));
        if (index == -1) {
            return null;
        }
        return tiles.get(index);
    }
    
    @Nullable
    public TileType getTile(BlockPos pos) {
        return getTile(pos.getX(), pos.getY(), pos.getZ());
    }
    
    @Nullable
    public TileType getTile(Vector3ic pos) {
        return getTile(pos.x(), pos.y(), pos.z());
        
    }
    
    public void forEachPosAndModule(BiConsumer<BlockPos, ModuleType> consumer) {
        for (int i = 0; i < modules.size(); i++) {
            final var module = modules.get(i);
            consumer.accept(module.iface.getBlockPos(), module);
        }
    }
    
    public void forEachPosAndTile(BiConsumer<BlockPos, TileType> consumer) {
        forEachTile((tile) -> consumer.accept(tile.getBlockPos(), tile));
    }
    
    public void forEachTile(Consumer<TileType> consumer) {
        forEachModule(module -> consumer.accept(module.iface));
    }
    
    public void forEachModule(Consumer<ModuleType> consumer) {
        for (int i = 0; i < modules.size(); i++) {
            consumer.accept(modules.get(i));
        }
    }
    
    public void forEachTileAndModule(BiConsumer<TileType, ModuleType> consumer) {
        for (int i = 0; i < modules.size(); i++) {
            consumer.accept(tiles.get(i), modules.get(i));
        }
    }
    
    public interface TileModulePosLong<ModuleType extends TileModule<TileType>, TileType extends BlockEntity & IModularTile> {
        void accept(TileType entity, ModuleType module, long pos);
    }
    
    public void forEachTileAndModuleAndPosLong(TileModulePosLong<ModuleType, TileType> consumer) {
        final int size = modules.size();
        final TileType[] tileElements = tiles.elements();
        final ModuleType[] moduleElements = modules.elements();
        final var posElements = poses.elements();
        if (tileElements.length < size || moduleElements.length < size || posElements.length < size) {
            throw new IllegalStateException("Arrays too short");
        }
        for (int i = 0; i < size; i++) {
            consumer.accept(tileElements[i], moduleElements[i], posElements[i]);
        }
    }
    
    public void forEachPos(Consumer<BlockPos> consumer) {
        forEachModule((module) -> consumer.accept(module.iface.getBlockPos()));
    }
    
    public boolean isEmpty() {
        return modules.isEmpty();
    }
    
    public int size() {
        return modules.size();
    }
    
    public TileType[] tileElements() {
        return tiles.elements();
    }
    
    public ModuleType[] moduleElements() {
        return modules.elements();
    }
    
    public long[] posElements() {
        return poses.elements();
    }
    
    @Nullable
    public ModuleType getOne() {
        return modules.get(0);
    }
    
    public void clear() {
        modules.clear();
        poses.clear();
        tiles.clear();
        indexMap.clear();
    }
}
