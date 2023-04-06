package igentuman.nc.phosphophyllite.modular.api;


import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IModularBlock {
    default <Type> Type as(Class<Type> clazz) {
        //noinspection unchecked
        return (Type) this;
    }
    
    BlockModule<?> module(Class<? extends IModularBlock> interfaceClazz);
    
    default <T extends BlockModule<?>> T module(Class<? extends IModularBlock> interfaceClazz, Class<T> moduleType) {
        //noinspection unchecked
        return (T) module(interfaceClazz);
    }
    
    List<BlockModule<?>> modules();
}