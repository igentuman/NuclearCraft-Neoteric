package igentuman.nc.item;


import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;

import net.minecraft.item.ItemStack;

import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import org.antlr.v4.runtime.misc.NotNull;;

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
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		World world = context.getLevel();
		BlockPos pos = context.getClickedPos().above();
		if(!context.getClickedFace().equals(Direction.UP)) return ActionResultType.FAIL;
		if (!canPlaceAt(world, pos)) return ActionResultType.FAIL;
		ItemUseContext cont = new ItemUseContext(world, context.getPlayer(), context.getHand(), stack,
				new BlockRayTraceResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside()));
		return super.onItemUseFirst(stack, cont);
	}

	private boolean canPlaceAt(World world, BlockPos blockPos) {
		for(int x = -1; x < 2; x++) {
			for(int y = 0; y < 3; y++) {
				for(int z = -1; z < 2; z++) {
					BlockState st = world.getBlockState(blockPos.offset(x, y, z));
					if(!st.getMaterial().isReplaceable()) return false;
				}
			}
		}
		return true;
	}
}
