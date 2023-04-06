package igentuman.nc.phosphophyllite.multiblock2.modular;


import igentuman.nc.util.annotation.NonnullDefault;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

@NonnullDefault
public class MultiblockControllerModuleRegistry {
    
    private static final LinkedHashMap<Class<? extends IModularMultiblockController<?, ?, ?>>, Function<IModularMultiblockController<?, ?, ?>, MultiblockControllerModule<?, ?, ?>>> moduleRegistry = new LinkedHashMap<>();
    
    
    public synchronized static <T extends IModularMultiblockController<?, ?, ?>> void registerModule(Class<T> moduleInterface, Function<T, MultiblockControllerModule<?, ?, ?>> constructor) {
        //noinspection unchecked
        final Function<IModularMultiblockController<?, ?, ?>, MultiblockControllerModule<?, ?, ?>> wrapped = controller -> constructor.apply((T) controller);
        moduleRegistry.put(moduleInterface, wrapped);
    }
    
    public static void forEach(BiConsumer<Class<? extends IModularMultiblockController<?, ?, ?>>, Function<IModularMultiblockController<?, ?, ?>, MultiblockControllerModule<?, ?, ?>>> callback) {
        moduleRegistry.forEach(callback);
    }
    
}
