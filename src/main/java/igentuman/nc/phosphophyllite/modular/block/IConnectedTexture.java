package igentuman.nc.phosphophyllite.modular.block;

import igentuman.nc.util.annotation.OnModLoad;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import igentuman.nc.phosphophyllite.modular.api.BlockModule;
import igentuman.nc.phosphophyllite.modular.api.IModularBlock;
import igentuman.nc.phosphophyllite.modular.api.ModuleRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IConnectedTexture extends IModularBlock {
    
    default boolean connectToBlock(Block block) {
        return block == as(Block.class);
    }
    
    final class Module extends BlockModule<IConnectedTexture> {
        
        public static final BooleanProperty TOP_CONNECTED_PROPERTY = BooleanProperty.create("top_connected");
        public static final BooleanProperty BOTTOM_CONNECTED_PROPERTY = BooleanProperty.create("bottom_connected");
        public static final BooleanProperty NORTH_CONNECTED_PROPERTY = BooleanProperty.create("north_connected");
        public static final BooleanProperty SOUTH_CONNECTED_PROPERTY = BooleanProperty.create("south_connected");
        public static final BooleanProperty EAST_CONNECTED_PROPERTY = BooleanProperty.create("east_connected");
        public static final BooleanProperty WEST_CONNECTED_PROPERTY = BooleanProperty.create("west_connected");
        
        private Module(IConnectedTexture iface) {
            super(iface);
        }
        
        @Override
        public void buildStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(TOP_CONNECTED_PROPERTY);
            builder.add(BOTTOM_CONNECTED_PROPERTY);
            builder.add(NORTH_CONNECTED_PROPERTY);
            builder.add(SOUTH_CONNECTED_PROPERTY);
            builder.add(EAST_CONNECTED_PROPERTY);
            builder.add(WEST_CONNECTED_PROPERTY);
        }
        
        @Override
        public BlockState buildDefaultState(BlockState state) {
            state = state.setValue(TOP_CONNECTED_PROPERTY, false);
            state = state.setValue(BOTTOM_CONNECTED_PROPERTY, false);
            state = state.setValue(NORTH_CONNECTED_PROPERTY, false);
            state = state.setValue(SOUTH_CONNECTED_PROPERTY, false);
            state = state.setValue(EAST_CONNECTED_PROPERTY, false);
            state = state.setValue(WEST_CONNECTED_PROPERTY, false);
            return state;
        }
        
        private void updateConnectedTextureState(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
            state = state.setValue(TOP_CONNECTED_PROPERTY, iface.connectToBlock(level.getBlockState(pos.relative(Direction.UP)).getBlock()));
            state = state.setValue(BOTTOM_CONNECTED_PROPERTY, iface.connectToBlock(level.getBlockState(pos.relative(Direction.DOWN)).getBlock()));
            state = state.setValue(NORTH_CONNECTED_PROPERTY, iface.connectToBlock(level.getBlockState(pos.relative(Direction.NORTH)).getBlock()));
            state = state.setValue(SOUTH_CONNECTED_PROPERTY, iface.connectToBlock(level.getBlockState(pos.relative(Direction.SOUTH)).getBlock()));
            state = state.setValue(EAST_CONNECTED_PROPERTY, iface.connectToBlock(level.getBlockState(pos.relative(Direction.EAST)).getBlock()));
            state = state.setValue(WEST_CONNECTED_PROPERTY, iface.connectToBlock(level.getBlockState(pos.relative(Direction.WEST)).getBlock()));
            level.setBlock(pos, state, 2);
        }
        
        @Override
        public void onPlaced(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            updateConnectedTextureState(level, pos, state);
        }
        
        @Override
        public void onNeighborChange(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
            updateConnectedTextureState(level, pos, state);
        }
        
        @OnModLoad
        static void onModLoad() {
            ModuleRegistry.registerBlockModule(IConnectedTexture.class, Module::new);
        }
    }
}
