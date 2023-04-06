package igentuman.nc.phosphophyllite.multiblock2.validated;

import igentuman.nc.util.annotation.NonnullDefault;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;

@NonnullDefault
public interface IAssemblyStateTileModule {
    @Contract(pure = true)
    default BlockState assembledBlockState(BlockState state) {
        return state;
    }
    
    @Contract(pure = true)
    default BlockState disassembledBlockState(BlockState state) {
        return state;
    }
}
