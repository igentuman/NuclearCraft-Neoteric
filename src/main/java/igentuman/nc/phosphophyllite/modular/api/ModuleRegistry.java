package igentuman.nc.phosphophyllite.modular.api;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModuleRegistry {
    private static final LinkedHashMap<Class<? extends IModularTile>, Function<BlockEntity, TileModule<?>>> tileModuleRegistry = new LinkedHashMap<>();
    private static final ArrayList<BiConsumer<Class<? extends IModularTile>, Function<BlockEntity, TileModule<?>>>> externalTileRegistrars = new ArrayList<>();
    private static final LinkedHashMap<Class<? extends IModularBlock>, Function<Block, BlockModule<?>>> blockModuleRegistry = new LinkedHashMap<>();
    private static final ArrayList<BiConsumer<Class<? extends IModularBlock>, Function<Block, BlockModule<?>>>> externalBlockRegistrars = new ArrayList<>();
    
    /**
     * Registers an ITileModule and the interface the tile class will implement to signal to create an instance at tile creation
     * <p>
     * Also passes this value to any external registries registered below
     *
     * @param moduleInterface: Interface class that will be implemented by the tile.
     * @param constructor:     Creates an instance of an ITileModule for the given tile with the interface implemented
     */
    
    public synchronized static <T extends IModularTile> void registerTileModule(Class<T> moduleInterface, Function<T, TileModule<?>> constructor) {
        //noinspection unchecked
        final Function<BlockEntity, TileModule<?>> wrapped = tile -> constructor.apply((T) tile);
        tileModuleRegistry.put(moduleInterface, wrapped);
        externalTileRegistrars.forEach(c -> c.accept(moduleInterface, wrapped));
    }
    
    public synchronized static <B extends IModularBlock> void registerBlockModule(Class<B> moduleInterface, Function<B, BlockModule<?>> constructor) {
        //noinspection unchecked
        final Function<Block, BlockModule<?>> wrapped = block -> constructor.apply((B) block);
        blockModuleRegistry.put(moduleInterface, wrapped);
        externalBlockRegistrars.forEach(c -> c.accept(moduleInterface, wrapped));
    }
    
    /**
     * allows for external implementations of the ITileModule system, so extensions from PhosphophylliteTile and PhosphophylliteBlock aren't forced
     *
     * @param tileRegistrar  external ITileModule registration function
     * @param blockRegistrar external IBlockModule registration function
     */
    public synchronized static void registerExternalRegistrar(BiConsumer<Class<? extends IModularTile>, Function<BlockEntity, TileModule<?>>> tileRegistrar, BiConsumer<Class<? extends IModularBlock>, Function<Block, BlockModule<?>>> blockRegistrar) {
        externalTileRegistrars.add(tileRegistrar);
        tileModuleRegistry.forEach(tileRegistrar);
        externalBlockRegistrars.add(blockRegistrar);
        blockModuleRegistry.forEach(blockRegistrar);
    }
    
    public static void forEachTileModule(BiConsumer<Class<? extends IModularTile>, Function<BlockEntity, TileModule<?>>> callback) {
        tileModuleRegistry.forEach(callback);
    }
    
    public static void forEachBlockModule(BiConsumer<Class<? extends IModularBlock>, Function<Block, BlockModule<?>>> callback) {
        blockModuleRegistry.forEach(callback);
    }
}
