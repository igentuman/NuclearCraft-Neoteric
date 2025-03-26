package igentuman.nc.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class FusionCoreItem extends BlockItem
{

	public FusionCoreItem(Block block, Properties props)
	{
		super(block, props);
	}

	@Override
	public boolean isRepairable(@Nonnull ItemStack stack)
	{
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return false;
	}

	@NotNull
	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos().above();
		if(!context.getClickedFace().equals(Direction.UP)) return InteractionResult.FAIL;
		if (!canPlaceAt(world, pos)) return InteractionResult.FAIL;
		UseOnContext cont = new UseOnContext(world, context.getPlayer(), context.getHand(), stack,
				new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside()));
		return super.onItemUseFirst(stack, cont);
	}

	private boolean canPlaceAt(Level world, BlockPos blockPos) {
		for(int x = -1; x < 2; x++) {
			for(int y = 0; y < 3; y++) {
				for(int z = -1; z < 2; z++) {
					BlockState st = world.getBlockState(blockPos.offset(x, y, z));
					if(!st.canBeReplaced()) return false;
				}
			}
		}
		return true;
	}
}
