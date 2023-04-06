package igentuman.nc.phosphophyllite.multiblock2.validated;


import igentuman.nc.util.annotation.NonnullDefault;

@NonnullDefault
public interface IAssembledTickMultiblockModule {
    
    default void preTick() {
    }
    
    default void postTick() {
    }
    
    default void preDisassembledTick() {
    }
    
    default void postDisassembledTick() {
    }
}
