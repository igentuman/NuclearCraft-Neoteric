package igentuman.nc.block.kugelblitz;

import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block.kugelblitz.entity.ChamberTerminalBE;
import igentuman.nc.compat.gregtech.GTUtils;
import igentuman.nc.container.ChamberTerminalContainer;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.multiblock.MultiblockHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.block.entity.NuclearCraftBE.isGTEUCapEnabled;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BE;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

public class ChamberTerminalBlock extends MultiblockControllerBlock implements EntityBlock {

    public static final String NAME = "chamber_terminal";
    public ChamberTerminalBlock() {
        this(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(8f, 3600000f)
                .requiresCorrectToolForDrops());
    }
    public ChamberTerminalBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(HORIZONTAL_FACING, Direction.NORTH)
                        .setValue(POWERED, false)
        );
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING)
                .add(BlockStateProperties.POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return KUGELBLITZ_BE.get(NAME).get().create(pPos, pState);
    }

    @Override
    public InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, InteractionHand hand, BlockHitResult result) {

        if (!level.isClientSide()) {
            BlockEntity be = level.getExistingBlockEntity(pos);

            if (be instanceof ChamberTerminalBE)  {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return __(NAME);
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
                            return new ChamberTerminalContainer(windowId, pos, playerInventory);
                    }
                };
                NetworkHooks.openScreen((ServerPlayer) player, containerProvider, be.getBlockPos());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof ChamberTerminalBE tile) {
                    tile.tickClient();
                    level.setBlock(pos, blockState.setValue(POWERED, tile.controllerEnabled), 3);
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof ChamberTerminalBE tile) {
                tile.tickServer();
            }
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if(level.isClientSide() || newState.is(this)) return;
        MultiblockHandler.get(level.dimension()).onControllerRemoved(pos);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        if(isGtLoaded() && isGTEUCapEnabled()) {
            list.add(__("tooltip.nc.energy_eu_tier", getTier(pStack)).withStyle(ChatFormatting.GOLD));
        }
        if(isGtLoaded() && GTCEU_CONFIG.COMPATIBILITY.get() == CommonConfig.GTCEUCompatibilityConfig.GTCEUCompatibility.GTCEU_AND_FE && GTCEU_CONFIG.LIMIT_FE_OUTPUT.get()) {
            list.add(__("tooltip.nc.max_fe_extract_per_tick", formatEnergy(GTUtils.getMaxOutputFE(GTCEU_CONFIG.KUGELBLITZ_ENERGY_TIER.get()))).withStyle(ChatFormatting.GOLD));
        }
        list.add(__("multiblock.build_in_chunk.advise").withStyle(ChatFormatting.GREEN));
        list.add(__("tooltip.structure.sizes", "11x11x11", "11x11x11").withStyle(ChatFormatting.ITALIC));
    }

    private CommonConfig.GTCEUCompatibilityConfig.GTCEUTier getTier(ItemStack pStack) {
        return CommonConfig.GTCEUCompatibilityConfig.GTCEUTier.byId(GTCEU_CONFIG.KUGELBLITZ_ENERGY_TIER.get().ordinal()+pStack.getOrCreateTag().getInt("upgrade_tier"));
    }
}
