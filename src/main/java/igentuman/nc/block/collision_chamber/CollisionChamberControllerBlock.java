package igentuman.nc.block.collision_chamber;

import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block.collision_chamber.entity.CollisionChamberControllerBE;
import igentuman.nc.container.CollisionChamberControllerContainer;
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
import static igentuman.nc.handler.config.AcceleratorConfig.COLLISION_CHAMBER_CONFIG;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.TARGET_CHAMBER_BE;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static igentuman.nc.util.TextUtils.__;

public class CollisionChamberControllerBlock extends MultiblockControllerBlock implements EntityBlock {

    public static final String NAME = "collision_chamber_controller";
    public CollisionChamberControllerBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(8f, 3600000f)
                .requiresCorrectToolForDrops());
    }
    public CollisionChamberControllerBlock(Properties pProperties) {
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
        return TARGET_CHAMBER_BE.get(NAME).get().create(pPos, pState);
    }

    @Override
    public InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, InteractionHand hand, BlockHitResult result) {

        if (!level.isClientSide()) {
            BlockEntity be = level.getExistingBlockEntity(pos);

            if (be instanceof CollisionChamberControllerBE)  {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return __(NAME);
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
                            return new CollisionChamberControllerContainer(windowId, pos, playerInventory);
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
                if (t instanceof CollisionChamberControllerBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof CollisionChamberControllerBE tile) {
                tile.tickServer();
            }
        };
    }

    public int maxSize() {
        return COLLISION_CHAMBER_CONFIG.MAX_SIZE.get();
    }
    public int minSize() {
        return COLLISION_CHAMBER_CONFIG.MIN_SIZE.get();
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        if(isGtLoaded() && isGTEUCapEnabled()) {
            list.add(__("tooltip.nc.energy_eu_tier", getTier(pStack)).withStyle(ChatFormatting.GOLD));
        }
        list.add(__("tooltip.structure.sizes", minSize()+"x"+minSize()+"x"+minSize(), maxSize()+"x"+maxSize()+"x"+maxSize()).withStyle(ChatFormatting.ITALIC));
    }


    private CommonConfig.GTCEUCompatibilityConfig.GTCEUTier getTier(ItemStack pStack) {
        return CommonConfig.GTCEUCompatibilityConfig.GTCEUTier.byId(GTCEU_CONFIG.ACCELERATORS_ENERGY_TIER.get().ordinal()+pStack.getOrCreateTag().getInt("upgrade_tier"));
    }
}