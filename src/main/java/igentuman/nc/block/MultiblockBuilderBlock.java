package igentuman.nc.block;

import com.mojang.serialization.MapCodec;
import igentuman.nc.block_entity.MultiblockBuilderBE;
import igentuman.nc.container.MultiblockBuilderContainer;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.__;

/** Facing block that opens the multiblock builder GUI (loads a plan and builds it in the world). */
public class MultiblockBuilderBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final MapCodec<MultiblockBuilderBlock> CODEC = simpleCodec(MultiblockBuilderBlock::new);

    public MultiblockBuilderBlock() {
        this(Properties.of().sound(SoundType.METAL).strength(4f).requiresCorrectToolForDrops());
    }

    public MultiblockBuilderBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModEntries.get("multiblock_builder").blockEntity().get().create(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MultiblockBuilderBE) {
                MenuProvider provider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return __("block." + MODID + ".multiblock_builder");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player p) {
                        return new MultiblockBuilderContainer(windowId, inv, pos);
                    }
                };
                serverPlayer.openMenu(provider, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
        list.add(TextUtils.applyFormat(__("nc.multiblock_builder.description"), ChatFormatting.AQUA));
    }
}
