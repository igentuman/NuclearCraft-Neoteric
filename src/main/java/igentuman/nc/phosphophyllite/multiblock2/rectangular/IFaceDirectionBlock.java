package igentuman.nc.phosphophyllite.multiblock2.rectangular;

import igentuman.nc.util.annotation.NonnullDefault;
import igentuman.nc.util.annotation.OnModLoad;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import igentuman.nc.phosphophyllite.modular.api.BlockModule;
import igentuman.nc.phosphophyllite.modular.api.IModularBlock;
import igentuman.nc.phosphophyllite.modular.api.ModuleRegistry;
import igentuman.nc.phosphophyllite.util.BlockStates;

@NonnullDefault
public interface IFaceDirectionBlock extends IModularBlock {
    class Module extends BlockModule<IFaceDirectionBlock> {
    
        public Module(IFaceDirectionBlock iface) {
            super(iface);
        }
    
        @Override
        public void buildStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(BlockStates.FACING);
        }
    
        @Override
        public BlockState buildDefaultState(BlockState state) {
            return state.setValue(BlockStates.FACING, Direction.UP);
        }
    
        @OnModLoad
        static void onModLoad() {
            ModuleRegistry.registerBlockModule(IFaceDirectionBlock.class, Module::new);
        }
    }
}
