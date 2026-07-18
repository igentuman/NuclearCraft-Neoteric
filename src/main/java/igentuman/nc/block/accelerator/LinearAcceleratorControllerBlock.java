package igentuman.nc.block.accelerator;

import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block.accelerator.entity.LinearAcceleratorControllerBE;
import igentuman.nc.container.LinearAcceleratorContainer;
import igentuman.nc.handler.config.CommonConfig;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.block.entity.NuclearCraftBE.isGTEUCapEnabled;
import static igentuman.nc.handler.config.AcceleratorConfig.ACCELERATOR_CONFIG;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BE;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static igentuman.nc.util.TextUtils.__;

public class LinearAcceleratorControllerBlock extends MultiblockControllerBlock implements EntityBlock {

    public static final String NAME = "linear_accelerator_controller";
    public LinearAcceleratorControllerBlock() {
        this(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(8f, 3600000f)
                .requiresCorrectToolForDrops());
    }
    public LinearAcceleratorControllerBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(HORIZONTAL_FACING, Direction.NORTH)
                        .setValue(POWERED, false)
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return ACCELERATOR_BE.get(NAME).get().create(pPos, pState);
    }

    @Override
    public InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, InteractionHand hand, BlockHitResult result) {

        if (!level.isClientSide()) {
            BlockEntity be = level.getExistingBlockEntity(pos);

            if (be instanceof LinearAcceleratorControllerBE)  {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return __(NAME);
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
                            return new LinearAcceleratorContainer(windowId, pos, playerInventory);
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
                if (t instanceof LinearAcceleratorControllerBE tile) {
                    tile.tickClient();
                    level.setBlock(pos, blockState.setValue(POWERED, tile.controllerEnabled), 3);
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof LinearAcceleratorControllerBE tile) {
                tile.tickServer();
            }
        };
    }

    public int maxDepth() {
        return switch (ACCELERATOR_CONFIG.SCALE.get()) {
            case 2 -> 1000;
            case 3 -> 10000;
            default -> 100;
        };
    }
    public int minDepth() {
        return switch (ACCELERATOR_CONFIG.SCALE.get()) {
            case 2 -> 60;
            case 3 -> 600;
            default -> 6;
        };
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        if(isGtLoaded() && isGTEUCapEnabled()) {
            list.add(__("tooltip.nc.energy_eu_tier", getTier(pStack)).withStyle(ChatFormatting.GOLD));
        }
        list.add(__("tooltip.structure.sizes", "5x5x"+minDepth(), "5x5x"+maxDepth()).withStyle(ChatFormatting.ITALIC));
    }

    private CommonConfig.GTCEUCompatibilityConfig.GTCEUTier getTier(ItemStack pStack) {
        return CommonConfig.GTCEUCompatibilityConfig.GTCEUTier.byId(GTCEU_CONFIG.ACCELERATORS_ENERGY_TIER.get().ordinal()+pStack.getOrCreateTag().getInt("upgrade_tier"));
    }
}
