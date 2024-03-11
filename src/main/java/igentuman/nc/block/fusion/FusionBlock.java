package igentuman.nc.block.fusion;

import igentuman.nc.block.entity.fusion.FusionBE;
import igentuman.nc.multiblock.fusion.FusionReactor;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import javax.annotation.Nullable;

import java.util.List;

public class FusionBlock extends Block {

    public FusionBlock() {
        this(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(2.0f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }
    public FusionBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
        /*this.registerDefaultState(
                this.stateDefinition.any()
        );*/
        if(getCode().contains("glass")) {
            properties.noOcclusion();
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
        return super.getBlock().defaultBlockState();
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return FusionReactor.FUSION_BE.get(getCode()).get().create();
    }

    public String getCode()
    {
        return Registry.BLOCK.getKey(this).getPath();
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader level, BlockPos pos, BlockPos neighbor){
        ((FusionBE)level.getBlockEntity(pos)).onNeighborChange(state,  pos, neighbor);
    }

    @Override
    public void onBlockExploded(BlockState state, World level, BlockPos pos, Explosion explosion)
    {
        if(!level.isClientSide) {
            ((FusionBE)level.getBlockEntity(pos)).onBlockDestroyed(state, level, pos, explosion);
        }
        super.onBlockExploded(state, level, pos, explosion);
    }

    @Override
    public void playerDestroy(World level, PlayerEntity pPlayer, BlockPos pos, BlockState state, @Nullable TileEntity pBlockEntity, ItemStack pTool) {
        if(!level.isClientSide) {
            ((FusionBE)level.getBlockEntity(pos)).onBlockDestroyed(state, level, pos, null);
        }
        super.playerDestroy(level, pPlayer, pos, state, pBlockEntity, pTool);
    }



/*    @Override
    public void appendHoverText(ItemStack stack, World world, List<TextComponent> list, ITooltipFlag flag)
    {
        if (getCode().equals("fusion_reactor_connector")) {
            list.add(new TranslationTextComponent("tooltip.nc.fusion_connector.descr").withStyle(TextFormatting.YELLOW));
        } else {
            list.add(new TranslationTextComponent("tooltip.nc.fusion_casing.descr").withStyle(TextFormatting.YELLOW));
        }
    }*/
}
