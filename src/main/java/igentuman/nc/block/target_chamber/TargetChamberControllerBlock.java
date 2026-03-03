package igentuman.nc.block.target_chamber;

import igentuman.api.platform.NCItemStacks;
import igentuman.api.platform.NCLevels;
import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.container.TargetChamberControllerContainer;
import igentuman.nc.handler.config.CommonConfig;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.block.entity.NuclearCraftBE.isGTEUCapEnabled;
import static igentuman.nc.handler.config.AcceleratorConfig.PARTICLE_CHAMBER_CONFIG;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration.TARGET_CHAMBER_BE;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static igentuman.nc.util.TextUtils.__;

public class TargetChamberControllerBlock extends MultiblockControllerBlock implements EntityBlock {

    public static final String NAME = "target_chamber_controller";
    public TargetChamberControllerBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(8f, 3600000f)
                .requiresCorrectToolForDrops());
    }
    public TargetChamberControllerBlock(Properties pProperties) {
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {

        if (!level.isClientSide()) {
            BlockEntity be = NCLevels.getExistingBlockEntity(level, pos);

            if (be instanceof TargetChamberControllerBE)  {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return __(NAME);
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
                            return new TargetChamberControllerContainer(windowId, pos, playerInventory);
                    }
                };
                ((ServerPlayer) player).openMenu(containerProvider, be.getBlockPos());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof TargetChamberControllerBE tile) {
                    tile.tickClient();
                    level.setBlock(pos, blockState.setValue(POWERED, tile.controllerEnabled), 3);
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof TargetChamberControllerBE tile) {
                tile.tickServer();
            }
        };
    }

    public int maxSize() {
        return PARTICLE_CHAMBER_CONFIG.MAX_SIZE.get();
    }
    public int minSize() {
        return PARTICLE_CHAMBER_CONFIG.MIN_SIZE.get();
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> list, TooltipFlag pFlag) {
        if(isGtLoaded() && isGTEUCapEnabled()) {
            list.add(__("tooltip.nc.energy_eu_tier", getTier(pStack)).withStyle(ChatFormatting.GOLD));
        }
        list.add(__("tooltip.structure.sizes", minSize()+"x"+minSize()+"x"+minSize(), maxSize()+"x"+maxSize()+"x"+maxSize()).withStyle(ChatFormatting.ITALIC));
    }


    private CommonConfig.GTCEUCompatibilityConfig.GTCEUTier getTier(ItemStack pStack) {
        return CommonConfig.GTCEUCompatibilityConfig.GTCEUTier.byId(GTCEU_CONFIG.ACCELERATORS_ENERGY_TIER.get().ordinal()+NCItemStacks.getInt(pStack, "upgrade_tier"));
    }
}
