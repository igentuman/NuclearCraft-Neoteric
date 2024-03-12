package igentuman.nc.item;

import igentuman.nc.radiation.data.PlayerRadiation;
import igentuman.nc.radiation.data.PlayerRadiationProvider;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.UUID;

public class DosimiterItem extends Item
{
	public DosimiterItem(Properties props)
	{
		super(props);
	}


	@NotNull
	@Override
	public ActionResult<ItemStack> use(@NotNull World world, PlayerEntity player, @NotNull Hand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!world.isClientSide()) {
			PlayerRadiation radiationCap = player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).orElse(null);
			if(radiationCap == null) return ActionResult.sidedSuccess(stack, world.isClientSide);
			int radiation = radiationCap.getRadiation();
			player.sendMessage(new TranslationTextComponent("message.nc.player_radiation_contamination", format(radiation)),  UUID.randomUUID());
			//CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, stack);
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
