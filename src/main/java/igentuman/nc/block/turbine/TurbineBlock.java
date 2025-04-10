package igentuman.nc.block.turbine;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.multiblock.MultiblockHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static igentuman.nc.multiblock.fission.FissionReactor.TRANSPARENT_BLOCKS;

public class TurbineBlock extends MultiblockBlock {

    public TurbineBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        list.add(Component.translatable("multiblock.build_in_chunk.advise").withStyle(ChatFormatting.GREEN));
    }
}
