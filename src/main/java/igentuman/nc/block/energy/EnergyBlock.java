package igentuman.nc.block.energy;

import com.mojang.serialization.MapCodec;
import igentuman.nc.block.TooltipBlock;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.block_entity.energy.AbstractEnergyBE;
import igentuman.nc.setup.ModEntries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static igentuman.nc.util.TextUtils.formatEnergy;

public class EnergyBlock extends BaseEntityBlock implements TooltipBlock {

    public static MapCodec<EnergyBlock> CODEC;
    protected final String name;

    public EnergyBlock(BlockBehaviour.Properties properties, String name) {
        super(properties);
        this.name = name;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        if (CODEC == null) {
            CODEC = simpleCodec(props -> new EnergyBlock(props, name));
        }
        return CODEC;
    }

    @Override
    public void addTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        String key = "block.nuclearcraft." + name + ".desc";
        tooltip.accept(Component.translatable(key).withStyle(ChatFormatting.GRAY));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModEntries.get(name).blockEntity().get().create(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AbstractEnergyBE be) {
            player.sendSystemMessage(Component.translatable("tooltip.nuclearcraft.energy_stored",
                    formatEnergy(be.energyStorage.getEnergyStored()),
                    formatEnergy(be.energyStorage.getMaxEnergyStored())).withStyle(ChatFormatting.BLUE));
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, st, be) -> {
                if (be instanceof GlobalBlockEntity gBE) gBE.clientTick();
            };
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof GlobalBlockEntity gBE) gBE.serverTick();
        };
    }
}
