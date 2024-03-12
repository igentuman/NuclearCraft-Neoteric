package igentuman.nc.item;

import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.radiation.data.WorldRadiation;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.UUID;

public class GeigerCounterItem extends Item
{
	public GeigerCounterItem(Properties props)
	{
		super(props);
	}

	@NotNull
	@Override
	public ActionResult<ItemStack> use(@NotNull World world, PlayerEntity player, @NotNull Hand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!world.isClientSide()) {
			WorldRadiation worldRadiation = RadiationManager.get(world).getWorldRadiation();
			int radiation = worldRadiation.getChunkRadiation(player.xChunk, player.zChunk);
			player.sendMessage(new TranslationTextComponent("message.nc.geiger_radiation_measure", format(radiation)), UUID.randomUUID());
			//CriteriaTriggers.USING_ITEM.trigger((ServerPlayer) player, stack);
		}
		return ActionResult.sidedSuccess(stack, world.isClientSide);
	}

	private static String format(int radiation) {
		if(radiation >= 1000000) {
			return String.format("%.2f", (float)radiation/1000000)+" Rad";
		}
		if(radiation >= 1000) {
			return String.format("%.2f", (float)radiation/1000)+" mRad";
		}
		return String.format("%.2f", (float)radiation)+" uRad";
	}
}
