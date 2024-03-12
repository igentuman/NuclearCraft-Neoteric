package igentuman.nc.block;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.TextUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.stats.Stats;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import javax.annotation.Nullable;
import javax.xml.soap.Text;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ProcessorBlock extends HorizontalBlock {
    public static final DirectionProperty HORIZONTAL_FACING = FACING;
    public static final BooleanProperty ACTIVE = BlockStateProperties.POWERED;
    public ProcessorBlock() {
        this(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(2.0f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }

    public ProcessorBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(HORIZONTAL_FACING, Direction.NORTH)
                        .setValue(ACTIVE, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(BlockStateProperties.HORIZONTAL_FACING)
                .add(BlockStateProperties.POWERED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return  NCProcessors.PROCESSORS_BE.get(processorCode()).get().create();
    }


    public String processorCode()
    {
        return asItem().toString();
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);

        if (stack.hasTag()) {
            NCProcessorBE tileEntity = (NCProcessorBE) world.getBlockEntity(pos);
            CompoundNBT nbtData = stack.getTag();
            tileEntity.load(state, nbtData);
        }
    }

    @Override
    public void playerDestroy(World pLevel, PlayerEntity pPlayer, BlockPos pPos, BlockState pState, @javax.annotation.Nullable TileEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
        pPlayer.causeFoodExhaustion(0.005F);
        NCProcessorBE processorBe = (NCProcessorBE) pBlockEntity;
        CompoundNBT data = processorBe.getTagForStack();

        ItemStack drop = new ItemStack(this);
        drop.setTag(data);
        if (!pLevel.isClientSide()) {
            ItemEntity itemEntity = new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), drop);
            itemEntity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itemEntity);
        }
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (!level.isClientSide()) {
            TileEntity be = level.getBlockEntity(pos);
            if (be instanceof NCProcessorBE)  {
                INamedContainerProvider containerProvider = new INamedContainerProvider() {

                    @Override
                    public TextComponent getDisplayName() {
                        return new TranslationTextComponent(processorCode());
                    }

                    @Override
                    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                        try {
                            return (Container) Processors.all()
                                    .get(processorCode()).getContainerConstructor()
                                    .newInstance(windowId, pos, playerInventory, playerEntity, processorCode());
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) { }
                        return null;
                    }
                };
                NetworkHooks.openGui((ServerPlayerEntity) player, containerProvider, be.getBlockPos());
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable IBlockReader pLevel, List<ITextComponent> list, ITooltipFlag pFlag) {
        if(asItem().toString().contains("empty") || this.asItem().equals(Items.AIR)) return;
        list.add(TextUtils.applyFormat(new TranslationTextComponent("processor.description."+processorCode()), TextFormatting.AQUA));
    }

}
