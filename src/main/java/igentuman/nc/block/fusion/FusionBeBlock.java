package igentuman.nc.block.fusion;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.block.fusion.entity.FusionCoreProxyBE;
import igentuman.nc.multiblock.fusion.FusionReactorRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class FusionBeBlock extends Block implements EntityBlock {

    public FusionBeBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(2.0f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }
    public FusionBeBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
        this.registerDefaultState(
                this.stateDefinition.any()
        );
        if(getCode().contains("glass")) {
            properties.noOcclusion();
        }
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return FusionReactorRegistration.FUSION_BE.get(getCode()).get().create(pPos, pState);
    }

    public String getCode()
    {
        return ForgeRegistries.BLOCKS.getKey(this).getPath();
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof FusionCoreProxyBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof FusionCoreProxyBE tile) {
                tile.tickServer();
            }
        };
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor){
        BlockEntity be = level.getExistingBlockEntity(pos);
        if(be instanceof MultiblockAttachable<?,?> mbAttachableBe) {
            mbAttachableBe.onNeighborChange(state, pos, neighbor);
        }
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion)
    {
        if(!level.isClientSide) {
            BlockEntity be = level.getExistingBlockEntity(pos);
            if(be instanceof MultiblockAttachable<?,?> mbAttachableBe) {
                mbAttachableBe.onBlockDestroyed(state, level, pos, explosion);
            }
        }
        super.onBlockExploded(state, level, pos, explosion);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid)
    {
        if(!level.isClientSide) {
            BlockEntity be = level.getExistingBlockEntity(pos);
            if(be instanceof MultiblockAttachable<?,?> mbAttachableBe) {
                mbAttachableBe.onBlockDestroyed(state, level, pos, null);
            }
        }
       return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable BlockGetter world, List<Component> list, TooltipFlag flag)
    {
        if (getCode().equals("fusion_reactor_connector")) {
            list.add(__("tooltip.nc.fusion_connector.descr").withStyle(ChatFormatting.YELLOW));
        } else {
            list.add(__("tooltip.nc.fusion_casing.descr").withStyle(ChatFormatting.YELLOW));
        }
    }
}
