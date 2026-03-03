package igentuman.nc.block;

import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.block.entity.NuclearCraftBE.isGTEUCapEnabled;
import static igentuman.nc.compat.gregtech.GTUtils.*;
import static igentuman.nc.handler.config.CommonConfig.ENERGY_GENERATION;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

public class DecayGeneratorBlock extends Block implements EntityBlock {
    public DecayGeneratorBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(5.0f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }
    public DecayGeneratorBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return NCEnergyBlocks.ENERGY_BE.get(code()).get().create(pPos, pState);
    }

    public String code()
    {
        return asItem().toString();
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof NCEnergy tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof NCEnergy tile) {
                tile.tickServer();
            }
        };
    }

    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> list, TooltipFlag pFlag)
    {
        if(isGtLoaded() && isGTEUCapEnabled()) {
            list.add(__("tooltip.nc.energy_eu_tier", formatEUTier(ENERGY_GENERATION.DECAY_GENERATOR.get())).withStyle(ChatFormatting.GOLD));
        }
        list.add(TextUtils.applyFormat(__("decay_generator.fe_generation"), ChatFormatting.GOLD));
    }
}
