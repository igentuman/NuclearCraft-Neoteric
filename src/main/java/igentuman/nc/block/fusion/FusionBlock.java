package igentuman.nc.block.fusion;

import igentuman.nc.block.entity.fusion.FusionBE;
import igentuman.nc.multiblock.fusion.FusionReactor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.world.level.material.Material;
import javax.annotation.Nullable;

import java.util.List;

public class FusionBlock extends Block implements EntityBlock {

    public FusionBlock() {
        this(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(2.0f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }
    public FusionBlock(Properties pProperties) {
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
        return FusionReactor.FUSION_BE.get(getCode()).get().create(pPos, pState);
    }

    public String getCode()
    {
        return Registry.BLOCK.getKey(this).getPath();
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof FusionBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof FusionBE tile) {
                tile.tickServer();
            }
        };
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor){
        ((FusionBE)level.getBlockEntity(pos)).onNeighborChange(state,  pos, neighbor);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion)
    {
        if(!level.isClientSide) {
            ((FusionBE)level.getBlockEntity(pos)).onBlockDestroyed(state, level, pos, explosion);
        }
        super.onBlockExploded(state, level, pos, explosion);
    }

    @Override
    public void playerDestroy(Level level, Player pPlayer, BlockPos pos, BlockState state, @Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        if(!level.isClientSide) {
            ((FusionBE)level.getBlockEntity(pos)).onBlockDestroyed(state, level, pos, null);
        }
        super.playerDestroy(level, pPlayer, pos, state, pBlockEntity, pTool);
    }


    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable BlockGetter world, List<Component> list, TooltipFlag flag)
    {
        if (getCode().equals("fusion_reactor_connector")) {
            list.add(new TranslatableComponent("tooltip.nc.fusion_connector.descr").withStyle(ChatFormatting.YELLOW));
        } else {
            list.add(new TranslatableComponent("tooltip.nc.fusion_casing.descr").withStyle(ChatFormatting.YELLOW));
        }
    }
}
