package igentuman.nc.block;

import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.content.energy.SolarPanels;
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
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.block.entity.NuclearCraftBE.isGTEUCapEnabled;
import static igentuman.nc.compat.gregtech.GTUtils.*;
import static igentuman.nc.setup.registration.NCEnergyBlocks.ENERGY_BE;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

public class SolarPanelBlock extends Block implements EntityBlock {
    public SolarPanelBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(2.0f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }
    public SolarPanelBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {

    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return ENERGY_BE.get(code()).get().create(pPos, pState);
    }

    public String code()
    {
        return SolarPanels.getCode(asItem().toString());
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
        int generation = SolarPanels.all().get(asItem().toString().replace("solar_panel_","")).getActualGeneration();
        if(isGtLoaded() && isGTEUCapEnabled()) {
            list.add(__("tooltip.nc.energy_eu_generation", formatEUEnergy(generation)).withStyle(ChatFormatting.GOLD));
            list.add(__("tooltip.nc.energy_eu_tier", SolarPanels.all().get(asItem().toString().replace("solar_panel_","")).getEnergyTier()).withStyle(ChatFormatting.GOLD));
        }
        if(!isGtLoaded() || !isOnlyGTCEUCapEnabled()) {
            list.add(TextUtils.applyFormat(__("solar_panel.fe_generation", TextUtils.numberFormat(generation)), ChatFormatting.BLUE));
        }
    }

    public boolean registered() {
        return SolarPanels.registered().containsKey(code().replace("solar_panel/",""));
    }
}
