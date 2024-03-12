package igentuman.nc.block.fission;

import igentuman.nc.block.entity.fission.FissionPortBE;
import igentuman.nc.container.FissionPortContainer;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.util.TextUtils;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.fml.network.NetworkHooks;
import javax.annotation.Nullable;

import java.util.List;


public class FissionPort extends HorizontalBlock {
    public static final DirectionProperty HORIZONTAL_FACING = FACING;
    public FissionPort() {
        this(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops());
    }
    public FissionPort(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(HORIZONTAL_FACING, Direction.NORTH)
        );
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING)
                .add(BlockStateProperties.POWERED);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return FissionReactor.FISSION_BE.get("fission_reactor_port").get().create();
    }


    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {

        if (!level.isClientSide()) {
            TileEntity be = level.getBlockEntity(pos);

            if (be instanceof FissionPortBE)  {
                INamedContainerProvider containerProvider = new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return new TranslationTextComponent("block.nuclearcraft.fission_reactor_port");
                    }

                    @Override
                    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                        return new FissionPortContainer(windowId, pos, playerInventory);
                    }
                };
                NetworkHooks.openGui((ServerPlayerEntity) player, containerProvider, be.getBlockPos());
            }
        }
        return ActionResultType.SUCCESS;
    }

    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable IBlockReader pLevel, List<ITextComponent> list, ITooltipFlag pFlag)
    {
        list.add(TextUtils.applyFormat(new TranslationTextComponent("fission_port.descr"), TextFormatting.GOLD));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pBlockState, World pLevel, BlockPos pPos) {
        return pLevel.getBlockEntity(pPos) instanceof FissionPortBE ? ((FissionPortBE)pLevel.getBlockEntity(pPos)).analogSignal : 0;
    }
}
