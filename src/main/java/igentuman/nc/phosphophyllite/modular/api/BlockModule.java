package igentuman.nc.phosphophyllite.modular.api;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockModule<InterfaceType extends IModularBlock> {
    
    @Nonnull
    public final InterfaceType iface;
    
    public BlockModule(IModularBlock iface) {
        //noinspection unchecked
        this.iface = (InterfaceType) iface;
    }
    
    public BlockState buildDefaultState(BlockState state) {
        return state;
    }
    
    public void buildStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    }
    
    public void onNeighborChange(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
    }
    
    public void onPlaced(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    }
    
    public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }
}
